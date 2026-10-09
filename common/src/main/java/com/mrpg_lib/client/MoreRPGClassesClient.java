package com.mrpg_lib.client;

import com.mrpg_lib.client.heart.HeartRegistry;
import com.mrpg_lib.client.heart.HeartTypes;
import com.mrpg_lib.client.particle.*;
import com.mrpg_lib.client.render.CustomCloudRenderer;
import com.mrpg_lib.client.render.FriendlyLightningEntityRenderer;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.spell_engine.client.SpellEngineClient;
import com.mrpg_lib.entity.MRPGCEntities;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class MoreRPGClassesClient{

    public interface EntityRendererRegistrar {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererFactory<T> factory);
    }

    public interface SpriteAwareParticleFactory<T extends ParticleEffect> {
        ParticleFactory<T> create(SpriteProvider provider);
    }

    public interface ParticleFactoryRegistrar {
        <T extends ParticleEffect> void register(ParticleType<T> type, ParticleFactory<T> factory);
        <T extends ParticleEffect> void registerSpriteAware(ParticleType<T> type, SpriteAwareParticleFactory<T> factory);
    }

    public static void init(){
        HeartTypes.getHeartTypes().forEach(HeartRegistry::register);
        if (MrpgCompat.SPELL_ENGINE) {
            SpellEngineClient.init();
        }
    }

    public static void registerEntityRenderers(EntityRendererRegistrar registrar) {
        registrar.register(MRPGCEntities.FRIENDLY_LIGHTNING, FriendlyLightningEntityRenderer::new);
        registrar.register(MRPGCEntities.CUSTOM_CLOUD, CustomCloudRenderer::new);
    }

    public static void registerParticleAppearances(ParticleFactoryRegistrar registrar) {
        if (MrpgCompat.SPELL_ENGINE) {
            SpellEngineClient.registerParticleAppearances(registrar);
        }

        registrar.registerSpriteAware(MoreParticles.RAINBOW_MUSIC_NOTE, RainbowMusicNoteParticle.Factory::new);
        registrar.registerSpriteAware(MoreParticles.FADING_MOTE, FadingMoteParticle.Factory::new);

        registrar.register(MoreParticles.POPUP, new PopupParticle.Factory());
    }
}
