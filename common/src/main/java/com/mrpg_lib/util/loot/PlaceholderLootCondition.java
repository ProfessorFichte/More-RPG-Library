package com.mrpg_lib.util.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;

public final class PlaceholderLootCondition implements LootCondition {
    private final LootConditionType type;

    private PlaceholderLootCondition(LootConditionType type) {
        this.type = type;
    }

    public static LootConditionType createType() {
        PlaceholderLootCondition[] instance = new PlaceholderLootCondition[1];
        LootConditionType type = new LootConditionType(MapCodec.unit(() -> instance[0]));
        instance[0] = new PlaceholderLootCondition(type);
        return type;
    }

    @Override
    public LootConditionType getType() {
        return type;
    }

    @Override
    public boolean test(LootContext context) {
        return false;
    }
}
