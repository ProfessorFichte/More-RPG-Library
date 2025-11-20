package net.more_rpg_classes.util.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.SpellDataComponents;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.spell_engine.api.spell.registry.SpellRegistry;

import java.util.*;

public class BindSpellFromPoolsLootFunction extends ConditionalLootFunction {
    public static final String NAME = "bind_spell_from_pools";
    public static final Identifier ID = Identifier.of("more_rpg_classes", NAME);
    private final LootNumberProvider chance;

    public static final MapCodec<BindSpellFromPoolsLootFunction> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    addConditionsField(instance)
                            .and(
                                    instance.group(
                                            Codec.STRING.listOf()
                                                    .fieldOf("spell_pools")
                                                    .orElse(List.of())
                                                    .forGetter(f -> f.pools),
                                            LootNumberProviderTypes.CODEC
                                                    .fieldOf("count")
                                                    .orElse(ConstantLootNumberProvider.create(1))
                                                    .forGetter(f -> f.count),
                                            LootNumberProviderTypes.CODEC
                                                    .optionalFieldOf("chance")
                                                    .forGetter(f -> Optional.ofNullable(f.chance))
                                    )
                            )
                            .apply(instance, BindSpellFromPoolsLootFunction::new)
            );

    public static final LootFunctionType<BindSpellFromPoolsLootFunction> TYPE =
            new LootFunctionType<>(CODEC);

    private final List<String> pools;
    private final LootNumberProvider count;

    public BindSpellFromPoolsLootFunction(
            List<LootCondition> conditions,
            List<String> pools,
            LootNumberProvider count,
            Optional<LootNumberProvider> chance) {
        super(conditions);
        this.pools = pools;
        this.count = count;
        this.chance = chance.orElse(null);
    }

    @Override
    public LootFunctionType<BindSpellFromPoolsLootFunction> getType() {
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
                Identifier id = Identifier.of(entry.substring(1));
                tags.add(TagKey.of(SpellRegistry.KEY, id));
            } else {
                exact.add(Identifier.of(entry));
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
                .map(Identifier::of)
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
        SpellContainer.ContentType contentType = existing.content();

        for (int i = 0; i < selectedCount; i++) {
            RegistryEntry<Spell> entry = poolSpells.get(random.nextInt(poolSpells.size()));
            int attempts = 3;
            while (attempts > 0 && (
                    selected.contains(entry) ||
                            (contentType != null &&
                                    Objects.equals(
                                            SpellContainerHelper.contentTypeForSpell(entry.value()),
                                            contentType
                                    ))
            )) {
                entry = poolSpells.get(random.nextInt(poolSpells.size()));
                attempts--;
            }
            selected.add(entry);
            contentType = SpellContainerHelper.contentTypeForSpell(entry.value());
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

        stack.set(SpellDataComponents.SPELL_CONTAINER, container);
        return stack;
    }



    public static ConditionalLootFunction.Builder<?> builder(
            List<String> pools, LootNumberProvider count, @Nullable LootNumberProvider chance) {
        return builder(conditions -> new BindSpellFromPoolsLootFunction(conditions, pools, count, Optional.ofNullable(chance)));
    }
}
