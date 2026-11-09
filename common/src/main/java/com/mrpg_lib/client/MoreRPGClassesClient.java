package com.mrpg_lib.client;

import com.mrpg_lib.client.fx.FxClient;
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
import net.minecraft.particle.SimpleParticleType;

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
        FxClient.init();
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
        registrar.registerSpriteAware(MoreParticles.FLOATING_BUBBLE, FloatingBubbleParticle.Factory::new);

        registrar.register(MoreParticles.POPUP, new PopupParticle.Factory());

        colored(registrar, ColoredParticles.EXPLOSION, ExplosionLargeParticle.Factory::new);
        colored(registrar, ColoredParticles.POOF, ExplosionSmokeParticle.Factory::new);
        colored(registrar, ColoredParticles.SMOKE, FireSmokeParticle.Factory::new);
        colored(registrar, ColoredParticles.LARGE_SMOKE, LargeFireSmokeParticle.Factory::new);
        colored(registrar, ColoredParticles.CLOUD, CloudParticle.CloudFactory::new);
        colored(registrar, ColoredParticles.END_ROD, EndRodParticle.Factory::new);
        colored(registrar, ColoredParticles.FIREWORK, FireworksSparkParticle.ExplosionFactory::new);
        colored(registrar, ColoredParticles.FLASH, FireworksSparkParticle.FlashFactory::new);
        colored(registrar, ColoredParticles.WITCH, SpellParticle.WitchFactory::new);
        colored(registrar, ColoredParticles.CRIT, DamageParticle.Factory::new);
        colored(registrar, ColoredParticles.ENCHANTED_HIT, DamageParticle.EnchantedHitFactory::new);
        colored(registrar, ColoredParticles.SNOWFLAKE, SnowflakeParticle.Factory::new);
        registrar.register(ColoredParticles.EXPLOSION_EMITTER, new ColoredExplosionEmitterParticle.Factory());
    }

    private static void colored(ParticleFactoryRegistrar registrar, ColoredParticleType type,
                                java.util.function.Function<SpriteProvider, ParticleFactory<SimpleParticleType>> vanilla) {
        registrar.registerSpriteAware(type, provider -> new ColoredParticleFactory(vanilla.apply(provider)));
    }
}
