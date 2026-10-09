package com.mrpg_lib.fabric;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.client.particle.MoreParticles;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.armory_rpgs.SmithingIngredients;
import com.mrpg_lib.compat.player_animator.api.MobAnimations;
import com.mrpg_lib.compat.player_animator.command.MobAnimationCommand;
import com.mrpg_lib.command.MobGoalCommand;
import com.mrpg_lib.compat.player_animator.network.MobAnimationPacket;
import com.mrpg_lib.compat.spell_engine.network.MobBeamPacket;
import com.mrpg_lib.compat.spell_engine.network.MobSpinPacket;
import com.mrpg_lib.compat.spell_engine.particle.MoreSpellParticles;
import com.mrpg_lib.fabric.network.MRPGCNetworkingImpl;
import com.mrpg_lib.item.MRPGCItemGroups;
import com.mrpg_lib.item.MRPGCItems;
import com.mrpg_lib.network.MRPGCNetworking;
import com.mrpg_lib.platform.MrpgPlatform;
import com.mrpg_lib.util.loot.MRPGCLootTableEntityModifiers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        MrpgPlatform.setUtil(new FabricPlatformUtil());
        MRPGCNetworking.setSender(MRPGCNetworkingImpl::sendToPlayer);
        if (MrpgCompat.SPELL_ENGINE) {
            PayloadTypeRegistry.playS2C().register(MobBeamPacket.ID, MobBeamPacket.CODEC);
            PayloadTypeRegistry.playS2C().register(MobSpinPacket.ID, MobSpinPacket.CODEC);
        }
        MRPGCNetworking.setTrackingSender(MRPGCNetworkingImpl::sendToTracking);
        if (MrpgCompat.PLAYER_ANIMATOR) {
            PayloadTypeRegistry.playS2C().register(MobAnimationPacket.ID, MobAnimationPacket.CODEC);
            EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
                if (entity instanceof LivingEntity living) {
                    MobAnimations.resync(living, player);
                }
            });
        }
        if (MrpgPlatform.isDevelopmentEnvironment()) {
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                MobAnimationCommand.register(dispatcher);
                MobGoalCommand.register(dispatcher);
            });
        }

        MRPGCMod.init();

        MRPGCMod.registerLootFunction();
        MoreParticles.register();
        if (MrpgCompat.SPELL_ENGINE) {
            MoreSpellParticles.register();
        }
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
