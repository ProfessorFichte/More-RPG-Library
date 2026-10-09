package com.mrpg_lib.neoforge.client;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.client.MoreRPGClassesClient;
import com.mrpg_lib.client.model.CustomModelHelper;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.spell_engine.client.render.MobBeamWorldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MRPGCMod.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MoreRPGClassesClient.init();
        CustomModelHelper.setLookup(id -> MinecraftClient.getInstance().getBakedModelManager().getModel(new ModelIdentifier(id, "standalone")));
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (Identifier id : CustomModelHelper.modelIds()) {
            event.register(ModelIdentifier.standalone(id));
        }
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
        if (!MrpgCompat.SPELL_ENGINE || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        MobBeamWorldRenderer.render(event.getPoseStack(), event.getCamera(), event.getPartialTick().getTickDelta(true));
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (!MrpgCompat.SPELL_ENGINE) return;
        MobBeamWorldRenderer.onDisconnect();
    }
}
