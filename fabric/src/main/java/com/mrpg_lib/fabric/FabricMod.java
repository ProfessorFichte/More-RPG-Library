package com.mrpg_lib.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import com.mrpg_lib.fabric.network.MRPGCNetworkingImpl;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;
import net.more_rpg_classes.item.MRPGCItemGroups;
import net.more_rpg_classes.item.MRPGCItems;
import net.more_rpg_classes.network.MRPGCNetworking;
import net.more_rpg_classes.network.MobBeamPacket;
import net.more_rpg_classes.util.loot.MRPGCLootTableEntityModifiers;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        MRPGCNetworking.setSender(MRPGCNetworkingImpl::sendToPlayer);
        PayloadTypeRegistry.playS2C().register(MobBeamPacket.ID, MobBeamPacket.CODEC);

        MRPGCMod.init();

        MRPGCMod.registerLootFunction();
        MoreParticles.register();
        MRPGCMod.registerSounds();
        MRPGCMod.registerItems();
        MRPGCMod.registerEffects();
        MRPGCMod.registerEntities();
        MRPGCMod.registerStructures();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            for (var item : MRPGCItems.INGREDIENTS_GROUP_ITEMS) {
                entries.addAfter(Items.RABBIT_HIDE, item);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            for (var item : MRPGCItems.COMBAT_GROUP_ITEMS) {
                entries.add(item);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(MRPGCItemGroups.ARMORY_KEY).register(entries -> {
            for (var item : SmithingIngredients.armoryGroupItems()) {
                entries.prepend(item);
            }
        });

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            MRPGCMod.modifyLootTable(registries, key, tableBuilder::pool);
            MRPGCLootTableEntityModifiers.modifyLootEntityTables(key, tableBuilder::pool);
        });
    }
}
