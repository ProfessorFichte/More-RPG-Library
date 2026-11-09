package com.mrpg_lib.util.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;

public final class PlaceholderLootFunction implements LootFunction {
    private final LootFunctionType<PlaceholderLootFunction> type;

    private PlaceholderLootFunction(LootFunctionType<PlaceholderLootFunction> type) {
        this.type = type;
    }

    public static LootFunctionType<PlaceholderLootFunction> createType() {
        PlaceholderLootFunction[] instance = new PlaceholderLootFunction[1];
        LootFunctionType<PlaceholderLootFunction> type = new LootFunctionType<>(MapCodec.unit(() -> instance[0]));
        instance[0] = new PlaceholderLootFunction(type);
        return type;
    }

    @Override
    public LootFunctionType<PlaceholderLootFunction> getType() {
        return type;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        return stack;
    }
}
