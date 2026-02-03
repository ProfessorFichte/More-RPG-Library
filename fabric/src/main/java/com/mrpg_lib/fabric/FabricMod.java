package com.mrpg_lib.fabric;

import net.fabricmc.api.ModInitializer;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.client.particle.MoreParticles;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        MRPGCMod.init();

        MRPGCMod.registerLootFunction();
        MoreParticles.register();
        MRPGCMod.registerSounds();
        MRPGCMod.registerItems();
        MRPGCMod.registerEffects();
        MRPGCMod.registerEntities();
        MRPGCMod.registerStructures();
    }
}
