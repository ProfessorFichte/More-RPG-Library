package com.mrpg_lib.compat.spell_engine.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;

import java.util.List;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public record SpellsAvailableLootCondition(List<String> pools, int tierMin, int tierMax) implements LootCondition {
    public static final Identifier ID = Identifier.of(MOD_ID, "spells_available");

    public static final MapCodec<SpellsAvailableLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.listOf().optionalFieldOf("spell_pools", List.of()).forGetter(SpellsAvailableLootCondition::pools),
                    Codec.INT.optionalFieldOf("spell_tier_min", 0).forGetter(SpellsAvailableLootCondition::tierMin),
                    Codec.INT.optionalFieldOf("spell_tier_max", Integer.MAX_VALUE).forGetter(SpellsAvailableLootCondition::tierMax)
            ).apply(instance, SpellsAvailableLootCondition::new)
    );

    public static final LootConditionType TYPE = new LootConditionType(CODEC);

    public static LootCondition.Builder builder(List<String> pools, int tierMin, int tierMax) {
        return () -> new SpellsAvailableLootCondition(pools, tierMin, tierMax);
    }

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        return SpellRegistry.stream(context.getWorld()).anyMatch(entry -> {
            int tier = entry.value().tier;
            if (tier < tierMin || tier > tierMax) return false;
            if (pools.isEmpty()) return true;
            for (String pool : pools) {
                if (pool.startsWith("#")) {
                    Identifier tag = Identifier.tryParse(pool.substring(1));
                    if (tag != null && entry.isIn(TagKey.of(SpellRegistry.KEY, tag))) return true;
                } else if (entry.getKey().map(key -> key.getValue().toString().equals(pool)).orElse(false)) {
                    return true;
                }
            }
            return false;
        });
    }
}
