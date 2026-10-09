package com.mrpg_lib.util.loot;

import net.minecraft.loot.LootPool;

@FunctionalInterface
public interface LootPoolAdder {
    void addPool(LootPool.Builder pool);
}
