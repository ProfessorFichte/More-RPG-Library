package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.fx.ParticleHelper;

public class IgnitedParticles implements CustomParticleStatusEffect.Spawner {
    private final ParticleBatch particles;

    public IgnitedParticles(int particleCount) {
        this.particles = new ParticleBatch(
                "minecraft:flame",
                ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                null, particleCount, 0.1F, 0.15F, 360
        );
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = new ParticleBatch(particles);
        scaledParticles.count *= (1 + amplifier * 0.2);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
