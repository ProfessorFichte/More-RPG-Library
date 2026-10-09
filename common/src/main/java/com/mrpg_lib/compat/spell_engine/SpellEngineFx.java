package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.compat.CompatHooks;
import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_engine.api.spell.fx.ParticleGroupBuilder;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.fx.SpellEngineParticles;

import java.util.List;

public final class SpellEngineFx implements CompatHooks.Fx {
    private static final ParticleGroup LIFESTEAL_PARTICLES = ParticleGroupBuilder
            .magic(SpellEngineParticles.magic_stripe, ParticleGroup.Motion.FLOAT, Color.RED)
            .batch(b -> b.shape(ParticleGroup.Shape.PIPE).widthFactor(2F)
                    .verticalOrigin(0.1F).count(20).speed(0.18F, 0.5F).angle(0));
    private static final ParticleGroup FUSE_PARTICLES = ParticleGroupBuilder
            .magic(SpellEngineParticles.magic_spell, ParticleGroup.Motion.BURST, Color.FROST)
            .batch(b -> b.shape(ParticleGroup.Shape.SPHERE).count(20).speed(0.3F, 0.8F).angle(0));
    private static final ParticleGroup BLEEDING_PARTICLES = ParticleGroupBuilder
            .of(SpellEngineParticles.dripping_blood).color(Color.RED)
            .batch(b -> b.shape(ParticleGroup.Shape.SPHERE).count(20).speed(0.3F, 0.8F).angle(0));
    private static final ParticleGroup POISON_PARTICLES = ParticleGroupBuilder
            .magic(SpellEngineParticles.magic_skull, ParticleGroup.Motion.FLOAT, Color.GREEN)
            .batch(b -> b.shape(ParticleGroup.Shape.SPHERE).count(20).speed(0.3F, 0.8F).angle(0));
    private static final ParticleGroup FREEZE_PARTICLES = ParticleGroupBuilder
            .magic(SpellEngineParticles.magic_frost, ParticleGroup.Motion.BURST, Color.FROST)
            .batch(b -> b.shape(ParticleGroup.Shape.SPHERE).count(20).speed(0.3F, 0.8F).angle(0));

    @Override
    public void lifesteal(LivingEntity entity) {
        ParticleHelper.sendBatches(entity, List.of(LIFESTEAL_PARTICLES));
    }

    @Override
    public void fuse(LivingEntity target, int color) {
        var fuseParticles = FUSE_PARTICLES.copy();
        fuseParticles.appearance.color(Color.from(color).toRGBA());
        ParticleHelper.sendBatches(target, List.of(fuseParticles));
    }

    @Override
    public void freeze(LivingEntity target) {
        ParticleHelper.sendBatches(target, List.of(FREEZE_PARTICLES));
    }

    @Override
    public void poison(LivingEntity target) {
        ParticleHelper.sendBatches(target, List.of(POISON_PARTICLES));
    }

    @Override
    public void bleed(LivingEntity target) {
        ParticleHelper.sendBatches(target, List.of(BLEEDING_PARTICLES));
    }
}
