package net.more_rpg_classes.util.loot;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;

public class LootInjector {
    public static void configure(RegistryWrapper.WrapperLookup registries, Identifier id, LootPoolAdder adder) {
        var config = MRPGCMod.lootConfig.value;
        var tableId = id.toString();
        var pool = config.entries.get(tableId);
        if (pool == null) {
            return;
        }

        var rolls = pool.rolls() > 0 ? pool.rolls() : 1F;
        LootPool.Builder lootPoolBuilder = LootPool.builder();

        var attempts = Math.ceil(rolls);
        var chance = pool.rolls() / attempts;
        lootPoolBuilder.rolls(BinomialLootNumberProvider.create((int) attempts, (float) chance));
        lootPoolBuilder.bonusRolls(ConstantLootNumberProvider.create(0));

        for (var entry : pool.items()) {
            var entryId = entry.itemId();
            var weight = entry.weight();
            var minAmount = entry.minAmount();
            var maxAmount = entry.maxAmount();
            if (entryId == null || entryId.isEmpty()) {
                continue;
            }
            var item = Registries.ITEM.get(Identifier.of(entryId));
            if (item == null) {
                continue;
            }
            var lootEntry = ItemEntry.builder(item)
                    .weight(weight);
            lootPoolBuilder.with(lootEntry);
            lootPoolBuilder.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(minAmount, maxAmount)));
        }
        adder.addPool(lootPoolBuilder);
    }

    private static LootNumberProvider numberProvider(float min, float max) {
        if (max <= min) {
            return ConstantLootNumberProvider.create(min);
        } else {
            return UniformLootNumberProvider.create(min, max);
        }
    }
}
