package com.mrpg_lib.forge.client;

import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.client.MoreRPGClassesClient;
import net.more_rpg_classes.client.render.MobBeamWorldRenderer;

@Mod.EventBusSubscriber(modid = MRPGCMod.MOD_ID, value = Dist.CLIENT)
public class ForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MoreRPGClassesClient.init();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        MoreRPGClassesClient.registerEntityRenderers(new MoreRPGClassesClient.EntityRendererRegistrar() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererFactory<T> factory) {
                event.registerEntityRenderer(type, factory);
            }
        });
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        MoreRPGClassesClient.registerParticleAppearances(new MoreRPGClassesClient.ParticleFactoryRegistrar() {
            @Override
            public <T extends ParticleEffect> void register(ParticleType<T> type, ParticleFactory<T> factory) {
                event.registerSpecial(type, factory);
            }

            @Override
            public <T extends ParticleEffect> void registerSpriteAware(ParticleType<T> type, MoreRPGClassesClient.SpriteAwareParticleFactory<T> factory) {
                event.registerSpriteSet(type, factory::create);
            }
        });
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        MobBeamWorldRenderer.render(event.getPoseStack(), event.getCamera(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        MobBeamWorldRenderer.onDisconnect();
    }
}
