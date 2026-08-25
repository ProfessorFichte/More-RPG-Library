package net.more_rpg_classes.client;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.more_rpg_classes.client.effect.*;
import net.more_rpg_classes.client.heart.HeartRegistry;
import net.more_rpg_classes.client.heart.HeartTypes;
import net.more_rpg_classes.client.particle.*;
import net.more_rpg_classes.client.render.FriendlyLightningEntityRenderer;
import net.more_rpg_classes.custom.MrpgLibSpells;
import net.more_rpg_classes.custom.SpellBuilderHelper;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.MRPGCEntities;
import net.spell_engine.api.effect.CustomModelStatusEffect;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.render.BuffParticleSpawner;
import net.spell_engine.client.render.CustomModelRegistry;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_engine.client.particle.SpellParticle;
import net.spell_engine.fx.SpellEngineParticles;

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
        for (var entry: MrpgLibSpells.entries) {
            if (entry.mutator() != null) {
                SpellTooltip.addDescriptionMutator(entry.id(), entry.mutator());
            }
        }
        HeartTypes.getHeartTypes().forEach(HeartRegistry::register);
        CustomModelRegistry.modelIds.add(FrozenSolidRenderer.modelId);
        CustomModelRegistry.modelIds.add(DuelistsFocusRenderer.OWNER_MODEL);
        CustomModelRegistry.modelIds.add(DuelistsFocusRenderer.TARGET_MODEL);
        CustomModelStatusEffect.register(MRPGCEffects.DUELISTS_FOCUS_OWNER.effect, new DuelistsFocusRenderer(DuelistsFocusRenderer.OWNER_MODEL));
        CustomModelStatusEffect.register(MRPGCEffects.DUELISTS_FOCUS_TARGET.effect, new DuelistsFocusRenderer(DuelistsFocusRenderer.TARGET_MODEL));
        registerEffectParticles();

        CustomParticleStatusEffect.register(MRPGCEffects.MOLTEN_ARMOR.effect, new MoltenArmorParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.BLEEDING.effect, new BleedingParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.FROSTED.effect, new FrostedParticles(10));
        CustomModelStatusEffect.register(MRPGCEffects.FROZEN_SOLID.effect, new FrozenSolidRenderer());
        CustomParticleStatusEffect.register(MRPGCEffects.SOAKED.effect, new SoakedParticles());
        CustomParticleStatusEffect.register(MRPGCEffects.FATAL_POISON.effect, new PoisonParticles(2));
        CustomParticleStatusEffect.register(MRPGCEffects.IGNITED.effect, new IgnitedParticles(3));
    }

    public static void registerEntityRenderers(EntityRendererRegistrar registrar) {
        registrar.register(MRPGCEntities.FRIENDLY_LIGHTNING, FriendlyLightningEntityRenderer::new);
    }

    public static void registerParticleAppearances(ParticleFactoryRegistrar registrar) {
        for (var entry: MoreParticles.entries()) {
            if (entry == MoreParticles.MUSIC_NOTE || entry == MoreParticles.STAR) {
                continue;
            }
            registrar.registerSpriteAware(entry.type(), provider -> new SpellParticle.Factory(provider, entry));
        }

        registrar.registerSpriteAware(MoreParticles.RAINBOW_MUSIC_NOTE, RainbowMusicNoteParticle.Factory::new);
        registrar.registerSpriteAware(MoreParticles.MUSIC_NOTE.type(),
                provider -> new MusicNoteParticle.Factory(provider, MoreParticles.MUSIC_NOTE));
        registrar.registerSpriteAware(MoreParticles.STAR.type(),
                provider -> new StarParticle.Factory(provider, MoreParticles.STAR));

        registrar.register(MoreParticles.POPUP, new PopupParticle.Factory());
    }

    private static void registerEffectParticles() {
        var sirensTearParticles = BuffParticleSpawner.defaultBatch(
                SpellEngineParticles.magic_spark.id().toString(),
                4,
                SpellBuilderHelper.BRIGHT_CYAN.toRGBA());
        sirensTearParticles.batch.extent(0.5F);
        CustomParticleStatusEffect.register(
                MRPGCEffects.SIRENS_TEAR.effect,
                new BuffParticleSpawner(sirensTearParticles)
                        .invertFrequency().withFrequency(20).scaleWithAmplifier(false)
        );
    }
}
