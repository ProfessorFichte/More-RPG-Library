package net.more_rpg_classes.util.loot;

import net.minecraft.loot.LootPool;

@FunctionalInterface
public interface LootPoolAdder {
    void addPool(LootPool.Builder pool);
}
