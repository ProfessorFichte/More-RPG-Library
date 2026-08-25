package com.mrpg_lib.neoforge;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import com.mrpg_lib.neoforge.network.MRPGCNetworkingImpl;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.client.render.MobBeamTracker;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;
import net.more_rpg_classes.item.MRPGCItemGroups;
import net.more_rpg_classes.item.MRPGCItems;
import net.more_rpg_classes.network.MRPGCNetworking;
import net.more_rpg_classes.network.MobBeamPacket;
import net.more_rpg_classes.util.loot.MRPGCLootTableEntityModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MRPGCMod.MOD_ID)
public final class NeoForgeMod {
    public NeoForgeMod(IEventBus modBus) {
        MRPGCNetworking.setSender(MRPGCNetworkingImpl::sendToPlayer);
        MRPGCMod.init();
        modBus.addListener(RegisterEvent.class, NeoForgeMod::register);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, NeoForgeMod::buildTabContents);
        modBus.addListener(RegisterPayloadHandlersEvent.class, NeoForgeMod::registerPayloads);
        NeoForge.EVENT_BUS.addListener(NeoForgeMod::onLootTableLoad);
    }

    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.LOOT_FUNCTION_TYPE, reg -> {
            MRPGCMod.registerLootFunction();
        });
        event.register(RegistryKeys.SOUND_EVENT, reg -> {
            MRPGCMod.registerSounds();
        });
        event.register(RegistryKeys.ITEM, reg -> {
            MRPGCMod.registerItems();
        });
        event.register(RegistryKeys.STATUS_EFFECT, reg -> {
            MRPGCMod.registerEffects();
        });
        event.register(RegistryKeys.PARTICLE_TYPE, reg -> {
            MoreParticles.register();
        });
        event.register(RegistryKeys.ENTITY_TYPE, reg -> {
            MRPGCMod.registerEntities();
        });
        event.register(RegistryKeys.STRUCTURE_TYPE, reg -> {
            MRPGCMod.registerStructures();
        });
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(MobBeamPacket.ID, MobBeamPacket.CODEC, (payload, context) ->
                context.enqueueWork(() -> MobBeamTracker.handle(payload, context.player().getWorld())));
    }

    private static void onLootTableLoad(LootTableLoadEvent event) {
        MRPGCMod.modifyLootTable(event.getRegistries(), event.getKey(), pool -> event.getTable().addPool(pool.build()));
        MRPGCLootTableEntityModifiers.modifyLootEntityTables(event.getKey(), pool -> event.getTable().addPool(pool.build()));
    }

    private static void buildTabContents(BuildCreativeModeTabContentsEvent event) {
        var tabKey = event.getTabKey();
        if (tabKey.equals(ItemGroups.INGREDIENTS)) {
            for (Item item : MRPGCItems.INGREDIENTS_GROUP_ITEMS) {
                event.insertAfter(new ItemStack(Items.RABBIT_HIDE), new ItemStack(item), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            }
        } else if (tabKey.equals(ItemGroups.COMBAT)) {
            for (Item item : MRPGCItems.COMBAT_GROUP_ITEMS) {
                event.add(item);
            }
        } else if (tabKey.equals(MRPGCItemGroups.ARMORY_KEY)) {
            for (Item item : SmithingIngredients.armoryGroupItems()) {
                event.insertFirst(new ItemStack(item), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}
