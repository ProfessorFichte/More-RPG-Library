package com.mrpg_lib.neoforge.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.client.MoreRPGClassesClient;
import net.more_rpg_classes.client.effect.DuelistsFocusRenderer;
import net.more_rpg_classes.client.effect.FrozenSolidRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.List;

@EventBusSubscriber(modid = MRPGCMod.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModelLoadingPlugin.register(ctx -> ctx.addModels(List.of(
                FrozenSolidRenderer.modelId,
                DuelistsFocusRenderer.OWNER_MODEL,
                DuelistsFocusRenderer.TARGET_MODEL
        )));
        MoreRPGClassesClient.init();
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        MoreRPGClassesClient.registerParticleAppearances();
    }
}
