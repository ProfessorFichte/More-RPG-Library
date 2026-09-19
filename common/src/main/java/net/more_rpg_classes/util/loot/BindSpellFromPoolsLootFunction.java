package net.more_rpg_classes.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.item.SpellItemData;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.spell_engine.api.spell.registry.SpellRegistry;

import java.util.*;

public class BindSpellFromPoolsLootFunction extends ConditionalLootFunction {
    public static final String NAME = "bind_spell_from_pools";
    public static final Identifier ID = new Identifier("more_rpg_classes", NAME);
    private final LootNumberProvider chance;

    public static final LootFunctionType TYPE = new LootFunctionType(new Serializer());

    private final List<String> pools;
    private final LootNumberProvider count;

    public BindSpellFromPoolsLootFunction(
            LootCondition[] conditions,
            List<String> pools,
            LootNumberProvider count,
            Optional<LootNumberProvider> chance) {
        super(conditions);
        this.pools = pools;
        this.count = count;
        this.chance = chance.orElse(null);
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Set.of();
    }

    private record PoolFilters(List<TagKey<Spell>> tags, List<Identifier> exact) {}

    private PoolFilters getPools() {
        if (pools == null || pools.isEmpty())
            return new PoolFilters(List.of(), List.of());

        List<TagKey<Spell>> tags = new ArrayList<>();
        List<Identifier> exact = new ArrayList<>();

        for (String entry : pools) {
            if (entry.startsWith("#")) {
                Identifier id = new Identifier(entry.substring(1));
                tags.add(TagKey.of(SpellRegistry.KEY, id));
            } else {
                exact.add(new Identifier(entry));
            }
        }
        return new PoolFilters(tags, exact);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (chance != null && context.getRandom().nextFloat() >= chance.nextFloat(context)) {
            return stack;
        }
        @Nullable SpellContainer existing = SpellContainerHelper.containerFromItemStack(stack);
        if (existing == null) {
            existing = SpellContainer.EMPTY;
        }

        List<Identifier> alreadyPresentSpells = existing.spell_ids().stream()
                .map(Identifier::new)
                .toList();

        var poolFilters = getPools();

        List<RegistryEntry.Reference<Spell>> poolSpells = SpellRegistry.stream(context.getWorld())
                .filter(entry -> {
                    Identifier id = entry.getKey().get().getValue();

                    boolean inPool =
                            (poolFilters.tags().isEmpty() && poolFilters.exact().isEmpty()) ||
                                    poolFilters.tags().stream().anyMatch(entry::isIn) ||
                                    poolFilters.exact().contains(id);

                    return inPool && !alreadyPresentSpells.contains(id);
                })
                .toList();

        if (poolSpells.isEmpty()) return stack;

        int selectedCount = this.count.nextInt(context);
        if (selectedCount < 1) selectedCount = 1;

        var random = context.getRandom();
        List<RegistryEntry<Spell>> selected = new ArrayList<>();
        SpellContainer.ContentType contentType = existing.access();

        for (int i = 0; i < selectedCount; i++) {
            RegistryEntry<Spell> entry = poolSpells.get(random.nextInt(poolSpells.size()));
            int attempts = 3;
            while (attempts > 0 && selected.contains(entry)) {
                entry = poolSpells.get(random.nextInt(poolSpells.size()));
                attempts--;
            }
            selected.add(entry);
        }

        SpellContainer container = existing.withContentType(
                contentType != null ? contentType : SpellContainer.ContentType.MAGIC
        );

        List<String> newSpellIds = selected.stream()
                .map(e -> e.getKey().get().getValue().toString())
                .toList();

        container = container.withAdditionalSpell(newSpellIds);

        var sorted = SpellContainerHelper.sortedSpells(context.getWorld(), container.spell_ids());
        container = container.copyWith(sorted);

        SpellItemData.setSpellContainer(stack, container);
        return stack;
    }



    public static ConditionalLootFunction.Builder<?> builder(
            List<String> pools, LootNumberProvider count, @Nullable LootNumberProvider chance) {
        return builder(conditions -> new BindSpellFromPoolsLootFunction(conditions, pools, count, Optional.ofNullable(chance)));
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<BindSpellFromPoolsLootFunction> {
        @Override
        public void toJson(JsonObject json, BindSpellFromPoolsLootFunction function, JsonSerializationContext context) {
            super.toJson(json, function, context);
            json.add("spell_pools", context.serialize(function.pools));
            json.add("count", context.serialize(function.count));
            if (function.chance != null) {
                json.add("chance", context.serialize(function.chance));
            }
        }

        @Override
        public BindSpellFromPoolsLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            List<String> pools = new ArrayList<>();
            if (json.has("spell_pools")) {
                for (var element : JsonHelper.getArray(json, "spell_pools")) {
                    pools.add(element.getAsString());
                }
            }
            LootNumberProvider count = json.has("count")
                    ? JsonHelper.deserialize(json, "count", context, LootNumberProvider.class)
                    : ConstantLootNumberProvider.create(1);
            Optional<LootNumberProvider> chance = json.has("chance")
                    ? Optional.of(JsonHelper.deserialize(json, "chance", context, LootNumberProvider.class))
                    : Optional.empty();
            return new BindSpellFromPoolsLootFunction(conditions, pools, count, chance);
        }
    }
}
