package com.mrpg_lib.fabric.client;

import com.mrpg_lib.client.MoreRPGClassesClient;
import com.mrpg_lib.client.model.CustomModelHelper;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.player_animator.client.MobAnimationClientNetwork;
import com.mrpg_lib.compat.player_animator.network.MobAnimationPacket;
import com.mrpg_lib.compat.spell_engine.client.render.MobBeamTracker;
import com.mrpg_lib.compat.spell_engine.client.render.MobSpinTracker;
import com.mrpg_lib.compat.spell_engine.client.render.MobBeamWorldRenderer;
import com.mrpg_lib.compat.spell_engine.network.MobBeamPacket;
import com.mrpg_lib.compat.spell_engine.network.MobSpinPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public final class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MoreRPGClassesClient.init();

        ModelLoadingPlugin.register(context -> CustomModelHelper.modelIds().forEach(context::addModels));
        CustomModelHelper.setLookup(id -> MinecraftClient.getInstance().getBakedModelManager().getModel(id));

        MoreRPGClassesClient.registerEntityRenderers(new MoreRPGClassesClient.EntityRendererRegistrar() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererFactory<T> factory) {
                EntityRendererRegistry.register(type, factory);
            }
        });

        MoreRPGClassesClient.registerParticleAppearances(new MoreRPGClassesClient.ParticleFactoryRegistrar() {
            @Override
            public <T extends ParticleEffect> void register(ParticleType<T> type, ParticleFactory<T> factory) {
                ParticleFactoryRegistry.getInstance().register(type, factory);
            }

            @Override
            public <T extends ParticleEffect> void registerSpriteAware(ParticleType<T> type, MoreRPGClassesClient.SpriteAwareParticleFactory<T> factory) {
                ParticleFactoryRegistry.getInstance().register(type, factory::create);
            }
        });

        if (MrpgCompat.PLAYER_ANIMATOR) {
            ClientPlayNetworking.registerGlobalReceiver(MobAnimationPacket.ID, (payload, context) ->
                    context.client().execute(() -> MobAnimationClientNetwork.handle(payload, context.client().world)));
        }

        if (MrpgCompat.SPELL_ENGINE) {
            ClientPlayNetworking.registerGlobalReceiver(MobBeamPacket.ID, (payload, context) ->
                    context.client().execute(() -> MobBeamTracker.handle(payload, context.client().world)));
            ClientPlayNetworking.registerGlobalReceiver(MobSpinPacket.ID, (payload, context) ->
                    context.client().execute(() -> MobSpinTracker.handle(payload, context.client().world)));

            WorldRenderEvents.AFTER_TRANSLUCENT.register(context ->
                    MobBeamWorldRenderer.render(context.matrixStack(), context.camera(), context.tickCounter().getTickDelta(true)));
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MobBeamWorldRenderer.onDisconnect());
        }
    }
}
