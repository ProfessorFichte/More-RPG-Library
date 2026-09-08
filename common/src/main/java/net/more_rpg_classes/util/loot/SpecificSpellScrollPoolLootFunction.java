package net.more_rpg_classes.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
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
import net.spell_engine.item.ScrollItem;
import net.spell_engine.item.SpellEngineItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class SpecificSpellScrollPoolLootFunction extends ConditionalLootFunction {
    public static final String NAME = "specific_spell_scroll_pool";
    public static final Identifier ID = new Identifier(MOD_ID, NAME);

    public static final LootFunctionType TYPE = new LootFunctionType(new Serializer());

    @Nullable private final List<String> pools;
    @Nullable private final List<String> blacklist;
    private final LootNumberProvider tierMin;
    private final LootNumberProvider tierMax;
    @Nullable private final LootNumberProvider count;

    private SpecificSpellScrollPoolLootFunction(
            LootCondition[] conditions,
            List<String> pools,
            LootNumberProvider tierMin,
            LootNumberProvider tierMax,
            LootNumberProvider count,
            List<String> blacklist) {
        super(conditions);
        this.pools = pools;
        this.tierMin = tierMin;
        this.tierMax = tierMax;
        this.count = count;
        this.blacklist = blacklist;
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
        if (pools == null || pools.isEmpty()) return new PoolFilters(List.of(), List.of());
        List<TagKey<Spell>> tags = new ArrayList<>();
        List<Identifier> exact = new ArrayList<>();
        for (String entry : pools) {
            if (entry.startsWith("#")) {
                tags.add(TagKey.of(SpellRegistry.KEY, new Identifier(entry.substring(1))));
            } else {
                exact.add(new Identifier(entry));
            }
        }
        return new PoolFilters(tags, exact);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        int a = this.tierMin.nextInt(context);
        int b = this.tierMax.nextInt(context);
        final int min = Math.min(a, b);
        final int max = Math.max(a, b);

        @Nullable var existingContainer = SpellContainerHelper.containerFromItemStack(stack);
        final List<Identifier> alreadyPresentSpells = existingContainer != null
                ? existingContainer.spell_ids().stream().map(Identifier::new).toList()
                : List.of();
        var poolFilters = getPools();
        final Set<Identifier> blacklistIds = blacklist != null
                ? blacklist.stream().map(Identifier::new).collect(Collectors.toSet())
                : Set.of();

        var spells = SpellRegistry.stream(context.getWorld())
                .filter(entry -> {
                    var id = entry.getKey().get().getValue();
                    int tier = entry.value().tier;

                    boolean inPool = poolFilters.tags().isEmpty() && poolFilters.exact().isEmpty()
                            || poolFilters.tags().stream().anyMatch(entry::isIn)
                            || poolFilters.exact().contains(id);

                    return (tier >= min && tier <= max)
                            && inPool
                            && !blacklistIds.contains(id)
                            && !alreadyPresentSpells.contains(id);
                })
                .toList();

        ArrayList<RegistryEntry<Spell>> selectedSpells = new ArrayList<>();
        @Nullable SpellContainer.ContentType selectedContentType = existingContainer != null ? existingContainer.access() : null;

        if (!spells.isEmpty()) {
            var selectedCount = this.count != null ? this.count.nextInt(context) : 1;
            var retryAttempts = 3;
            for (int i = 0; i < selectedCount; i++) {
                var entry = spells.get(context.getRandom().nextInt(spells.size()));
                while ((retryAttempts > 0) && selectedSpells.contains(entry)) {
                    entry = spells.get(context.getRandom().nextInt(spells.size()));
                    retryAttempts -= 1;
                }
                selectedSpells.add(entry);
            }
        }

        if (!selectedSpells.isEmpty()) {
            var newContainer = existingContainer != null ? existingContainer : SpellContainer.EMPTY
                    .withContentType(selectedContentType != null ? selectedContentType : SpellContainer.ContentType.MAGIC);

            var newSpellIds = selectedSpells.stream()
                    .map(entry -> entry.getKey().get().getValue().toString())
                    .toList();

            newContainer = newContainer.withAdditionalSpell(newSpellIds);
            var sortedSpellIds = SpellContainerHelper.sortedSpells(context.getWorld(), newContainer.spell_ids());
            newContainer = newContainer.copyWith(sortedSpellIds);

            SpellItemData.setSpellContainer(stack, newContainer);

            if (stack.getItem() == SpellEngineItems.SCROLL.get()) {
                var first = selectedSpells.get(0);
                ScrollItem.onSpellAdded(stack, first,
                        ScrollItem.resolveSpellPool(context.getWorld(), first));
            }
        } else {
            if (stack.getItem() == SpellEngineItems.SCROLL.get()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    public static Builder<?> builder(
            List<String> pools,
            LootNumberProvider tierMin,
            LootNumberProvider tierMax,
            LootNumberProvider count,
            List<String> blacklist) {
        return builder(conditions ->
                new SpecificSpellScrollPoolLootFunction(conditions, pools, tierMin, tierMax, count, blacklist));
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<SpecificSpellScrollPoolLootFunction> {
        @Override
        public void toJson(JsonObject json, SpecificSpellScrollPoolLootFunction function, JsonSerializationContext context) {
            super.toJson(json, function, context);
            json.add("spell_pools", context.serialize(function.pools));
            json.add("spell_tier_min", context.serialize(function.tierMin));
            json.add("spell_tier_max", context.serialize(function.tierMax));
            json.add("count", context.serialize(function.count));
            json.add("blacklist_spells", context.serialize(function.blacklist));
        }

        @Override
        public SpecificSpellScrollPoolLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new SpecificSpellScrollPoolLootFunction(
                    conditions,
                    stringList(json, "spell_pools"),
                    JsonHelper.deserialize(json, "spell_tier_min", context, LootNumberProvider.class),
                    JsonHelper.deserialize(json, "spell_tier_max", context, LootNumberProvider.class),
                    JsonHelper.deserialize(json, "count", context, LootNumberProvider.class),
                    stringList(json, "blacklist_spells")
            );
        }

        private static List<String> stringList(JsonObject json, String key) {
            List<String> values = new ArrayList<>();
            if (json.has(key)) {
                for (var element : JsonHelper.getArray(json, key)) {
                    values.add(element.getAsString());
                }
            }
            return values;
        }
    }
}
