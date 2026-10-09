package com.mrpg_lib.neoforge;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.client.particle.MoreParticles;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.armory_rpgs.SmithingIngredients;
import com.mrpg_lib.compat.player_animator.api.MobAnimations;
import com.mrpg_lib.compat.player_animator.client.MobAnimationClientNetwork;
import com.mrpg_lib.compat.player_animator.command.MobAnimationCommand;
import com.mrpg_lib.command.MobGoalCommand;
import com.mrpg_lib.compat.player_animator.network.MobAnimationPacket;
import com.mrpg_lib.compat.spell_engine.client.render.MobBeamTracker;
import com.mrpg_lib.compat.spell_engine.client.render.MobSpinTracker;
import com.mrpg_lib.compat.spell_engine.network.MobBeamPacket;
import com.mrpg_lib.compat.spell_engine.network.MobSpinPacket;
import com.mrpg_lib.compat.spell_engine.particle.MoreSpellParticles;
import com.mrpg_lib.item.MRPGCItemGroups;
import com.mrpg_lib.item.MRPGCItems;
import com.mrpg_lib.neoforge.network.MRPGCNetworkingImpl;
import com.mrpg_lib.network.MRPGCNetworking;
import com.mrpg_lib.platform.MrpgPlatform;
import com.mrpg_lib.util.loot.MRPGCLootTableEntityModifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MRPGCMod.MOD_ID)
public final class NeoForgeMod {
    public NeoForgeMod(IEventBus modBus) {
        MrpgPlatform.setUtil(new NeoForgePlatformUtil());
        MRPGCNetworking.setSender(MRPGCNetworkingImpl::sendToPlayer);
        MRPGCNetworking.setTrackingSender(MRPGCNetworkingImpl::sendToTracking);
        MRPGCMod.init();
        modBus.addListener(RegisterEvent.class, NeoForgeMod::register);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, NeoForgeMod::buildTabContents);
        modBus.addListener(RegisterPayloadHandlersEvent.class, NeoForgeMod::registerPayloads);
        NeoForge.EVENT_BUS.addListener(NeoForgeMod::onLootTableLoad);
        if (MrpgCompat.PLAYER_ANIMATOR) {
            NeoForge.EVENT_BUS.addListener(NeoForgeMod::onStartTracking);
        }
        if (MrpgPlatform.isDevelopmentEnvironment()) {
            NeoForge.EVENT_BUS.addListener(NeoForgeMod::onRegisterCommands);
        }
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && event.getEntity() instanceof ServerPlayerEntity player) {
            MobAnimations.resync(living, player);
        }
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        MobAnimationCommand.register(event.getDispatcher());
        MobGoalCommand.register(event.getDispatcher());
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
            if (MrpgCompat.SPELL_ENGINE) {
                MoreSpellParticles.register();
            }
        });
        event.register(RegistryKeys.ENTITY_TYPE, reg -> {
            MRPGCMod.registerEntities();
        });
        event.register(RegistryKeys.STRUCTURE_TYPE, reg -> {
            MRPGCMod.registerStructures();
        });
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        if (MrpgCompat.SPELL_ENGINE) {
            var registrar = event.registrar("1");
            registrar.playToClient(MobBeamPacket.ID, MobBeamPacket.CODEC, (payload, context) ->
                    context.enqueueWork(() -> MobBeamTracker.handle(payload, context.player().getWorld())));
            registrar.playToClient(MobSpinPacket.ID, MobSpinPacket.CODEC, (payload, context) ->
                    context.enqueueWork(() -> MobSpinTracker.handle(payload, context.player().getWorld())));
        }
        if (MrpgCompat.PLAYER_ANIMATOR) {
            var registrar = event.registrar("1").optional();
            registrar.playToClient(MobAnimationPacket.ID, MobAnimationPacket.CODEC, (payload, context) ->
                    context.enqueueWork(() -> MobAnimationClientNetwork.handle(payload, context.player().getWorld())));
        }
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
