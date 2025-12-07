package com.mrpg_lib.neoforge;

import net.minecraft.registry.RegistryKeys;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MRPGCMod.MOD_ID)
public final class NeoForgeMod {
    public NeoForgeMod(IEventBus modBus) {
        MRPGCMod.init();
        modBus.addListener(RegisterEvent.class, NeoForgeMod::register);
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
    }
}
