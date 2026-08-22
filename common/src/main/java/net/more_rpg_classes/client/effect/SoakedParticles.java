package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_engine.api.spell.fx.ParticleGroupBuilder;
import net.spell_engine.fx.ParticleHelper;
import net.more_rpg_classes.client.particle.MoreParticles;

public class SoakedParticles implements CustomParticleStatusEffect.Spawner {

    private final ParticleGroup particles;

    public SoakedParticles() {
        this.particles = ParticleGroupBuilder.of(MoreParticles.SPLASH)
                .batch(b -> b.shape(ParticleGroup.Shape.SPHERE).count(20).speed(0.1F, 0.3F));
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = particles.copy();
        scaledParticles.batch.count *= (1);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
