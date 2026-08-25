package com.mrpg_lib.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.more_rpg_classes.client.MoreRPGClassesClient;
import net.more_rpg_classes.client.render.MobBeamTracker;
import net.more_rpg_classes.client.render.MobBeamWorldRenderer;
import net.more_rpg_classes.network.MobBeamPacket;

public final class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MoreRPGClassesClient.init();

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

        ClientPlayNetworking.registerGlobalReceiver(MobBeamPacket.ID, (payload, context) ->
                context.client().execute(() -> MobBeamTracker.handle(payload, context.client().world)));

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context ->
                MobBeamWorldRenderer.render(context.matrixStack(), context.camera(), context.tickCounter().getTickDelta(true)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MobBeamWorldRenderer.onDisconnect());
    }
}
