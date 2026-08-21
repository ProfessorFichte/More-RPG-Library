package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_engine.api.spell.fx.ParticleGroupBuilder;
import net.spell_engine.fx.ParticleHelper;
import net.more_rpg_classes.client.particle.MoreParticles;

public class PoisonParticles implements CustomParticleStatusEffect.Spawner {

    private final ParticleGroup particles;

    public PoisonParticles(int particleCount) {
        this.particles = ParticleGroupBuilder.of(MoreParticles.FATAL_POISON)
                .batch(b -> b.shape(ParticleGroup.Shape.CONE).count(particleCount).speed(0.2F, 0.2F).angle(180));
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = particles.copy();
        scaledParticles.batch.count *= (1 + amplifier * 0.2);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
