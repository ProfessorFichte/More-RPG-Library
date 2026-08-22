package net.more_rpg_classes.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.particle.*;
import net.more_rpg_classes.client.effect.*;
import net.more_rpg_classes.client.heart.HeartRegistry;
import net.more_rpg_classes.client.heart.HeartTypes;
import net.more_rpg_classes.client.particle.*;
import net.more_rpg_classes.client.render.FriendlyLightningEntityRenderer;
import net.more_rpg_classes.client.render.MobBeamTracker;
import net.more_rpg_classes.client.render.MobBeamWorldRenderer;
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

    public static void init(){
        MobBeamTracker.register();
        MobBeamWorldRenderer.setup();
        for (var entry: MrpgLibSpells.entries) {
            if (entry.mutator() != null) {
                SpellTooltip.addDescriptionMutator(entry.id(), entry.mutator());
            }
        }
        // Register heart types
        HeartTypes.getHeartTypes().forEach(HeartRegistry::register);
        // Register entity renderers
        EntityRendererRegistry.register(MRPGCEntities.FRIENDLY_LIGHTNING, FriendlyLightningEntityRenderer::new);
        // Register Status Effect Renderers
        CustomModelRegistry.modelIds.add(FrozenSolidRenderer.modelId);
        CustomModelRegistry.modelIds.add(DuelistsFocusRenderer.OWNER_MODEL);
        CustomModelRegistry.modelIds.add(DuelistsFocusRenderer.TARGET_MODEL);
        CustomModelStatusEffect.register(MRPGCEffects.DUELISTS_FOCUS_OWNER.effect, new DuelistsFocusRenderer(DuelistsFocusRenderer.OWNER_MODEL));
        CustomModelStatusEffect.register(MRPGCEffects.DUELISTS_FOCUS_TARGET.effect, new DuelistsFocusRenderer(DuelistsFocusRenderer.TARGET_MODEL));
        registerEffectParticles();

        ParticleFactoryRegistry.getInstance().register(MoreParticles.POPUP, new PopupParticle.Factory());

        CustomParticleStatusEffect.register(MRPGCEffects.MOLTEN_ARMOR.effect, new MoltenArmorParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.BLEEDING.effect, new BleedingParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.FROSTED.effect, new FrostedParticles(10));
        CustomModelStatusEffect.register(MRPGCEffects.FROZEN_SOLID.effect, new FrozenSolidRenderer());
        CustomParticleStatusEffect.register(MRPGCEffects.SOAKED.effect, new SoakedParticles());
        CustomParticleStatusEffect.register(MRPGCEffects.FATAL_POISON.effect, new PoisonParticles(2));
        CustomParticleStatusEffect.register(MRPGCEffects.IGNITED.effect, new IgnitedParticles(3));
    }
    public static void registerParticleAppearances() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();

        // One generic factory for every entry this mod owns: SpellParticle resolves the
        // entry's defaults against the per-spawn ParticleGroup.Appearance payload.
        for (var entry: MoreParticles.entries()) {
            if (entry == MoreParticles.MUSIC_NOTE || entry == MoreParticles.STAR) {
                continue; // Bound to their own factories below.
            }
            registry.register(entry.type(), provider -> new SpellParticle.Factory(provider, entry));
        }

        // Kept on their own factories - behaviour Appearance cannot express.
        registry.register(MoreParticles.RAINBOW_MUSIC_NOTE, RainbowMusicNoteParticle.Factory::new);
        // Still an Entry (so its colour payload resolves like any other), but its motion is
        // hand-written: a fixed gentle rise, a random walk, and a fade over the last 15 ticks.
        registry.register(MoreParticles.MUSIC_NOTE.type(),
                provider -> new MusicNoteParticle.Factory(provider, MoreParticles.MUSIC_NOTE));
        // Same story as the note: a self-set rise the batch cannot override, X/Z-only
        // damping, and a fade over the last 20 ticks.
        registry.register(MoreParticles.STAR.type(),
                provider -> new StarParticle.Factory(provider, MoreParticles.STAR));
    }

    private static void registerEffectParticles() {
        // V1 chained .extent() onto the batch object itself; in 1.10 extent is a Batch
        // field on the returned ParticleGroup.
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
