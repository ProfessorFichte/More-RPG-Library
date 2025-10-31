package com.mrpg_lib.neoforge.client;

import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.client.MoreRPGClassesClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = MRPGCMod.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MoreRPGClassesClient.init();
    }
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        MoreRPGClassesClient.registerParticleAppearances();
    }
}
