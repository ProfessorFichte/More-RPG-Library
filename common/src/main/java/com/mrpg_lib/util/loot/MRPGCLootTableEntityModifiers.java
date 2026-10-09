package com.mrpg_lib.util.loot;

import com.mrpg_lib.item.MRPGCItems;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;

public class MRPGCLootTableEntityModifiers {

    public static void modifyLootEntityTables(RegistryKey<LootTable> key, LootPoolAdder adder) {
        if (EntityType.POLAR_BEAR.getLootTableId().equals(key)) {
            LootPool.Builder poolBuilder = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(1.0f))
                    .with(ItemEntry.builder(MRPGCItems.POLAR_BEAR_FUR))
                    .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 1.0f)));
            adder.addPool(poolBuilder);
        }
        else if (EntityType.RAVAGER.getLootTableId().equals(key)) {
            LootPool.Builder poolBuilder = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(1.0f))
                    .with(ItemEntry.builder(MRPGCItems.HARDENED_LEATHER))
                    .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 1.0f)));
            adder.addPool(poolBuilder);
        }
        else if (EntityType.WOLF.getLootTableId().equals(key)) {
            LootPool.Builder poolBuilder = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(1.0f))
                    .with(ItemEntry.builder(MRPGCItems.WOLF_FUR))
                    .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 1.0f)));
            adder.addPool(poolBuilder);
        }
    }

}
