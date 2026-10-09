package com.mrpg_lib.compat.spell_engine.client;

import com.mrpg_lib.client.MoreRPGClassesClient;
import com.mrpg_lib.compat.spell_engine.MrpgLibSpells;
import com.mrpg_lib.compat.spell_engine.SpellBuilderHelper;
import com.mrpg_lib.compat.spell_engine.client.effect.*;
import com.mrpg_lib.compat.spell_engine.client.particle.MusicNoteParticle;
import com.mrpg_lib.compat.spell_engine.client.particle.StarParticle;
import com.mrpg_lib.compat.spell_engine.particle.MoreSpellParticles;
import com.mrpg_lib.effect.MRPGCEffects;
import net.spell_engine.api.effect.CustomModelStatusEffect;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.render.BuffParticleSpawner;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_engine.client.particle.SpellParticle;
import net.spell_engine.client.render.CustomModelRegistry;
import net.spell_engine.fx.SpellEngineParticles;

public final class SpellEngineClient {
    private SpellEngineClient() {
    }

    public static void init() {
        for (var entry: MrpgLibSpells.entries) {
            if (entry.mutator() != null) {
                SpellTooltip.addDescriptionMutator(entry.id(), entry.mutator());
            }
        }
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

    public static void registerParticleAppearances(MoreRPGClassesClient.ParticleFactoryRegistrar registrar) {
        for (var entry: MoreSpellParticles.entries()) {
            if (entry == MoreSpellParticles.MUSIC_NOTE || entry == MoreSpellParticles.STAR) {
                continue;
            }
            registrar.registerSpriteAware(entry.type(), provider -> new SpellParticle.Factory(provider, entry));
        }

        registrar.registerSpriteAware(MoreSpellParticles.MUSIC_NOTE.type(),
                provider -> new MusicNoteParticle.Factory(provider, MoreSpellParticles.MUSIC_NOTE));
        registrar.registerSpriteAware(MoreSpellParticles.STAR.type(),
                provider -> new StarParticle.Factory(provider, MoreSpellParticles.STAR));
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
