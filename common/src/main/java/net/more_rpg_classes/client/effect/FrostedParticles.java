package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_engine.api.spell.fx.ParticleGroupBuilder;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.SpellEngineParticles;

public class FrostedParticles implements CustomParticleStatusEffect.Spawner {

    private final ParticleGroup particles;

    public FrostedParticles(int particleCount) {
        this.particles = ParticleGroupBuilder.magic(SpellEngineParticles.magic_frost, ParticleGroup.Motion.BURST, Color.FROST)
                .batch(b -> b.shape(ParticleGroup.Shape.CONE).count(particleCount).speed(0.2F, 0.8F));
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = particles.copy();
        scaledParticles.batch.count *= (1);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
