package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_engine.api.spell.fx.ParticleGroupBuilder;
import net.spell_engine.fx.ParticleHelper;

public class MoltenArmorParticles implements CustomParticleStatusEffect.Spawner {

    private final ParticleGroup particles;

    public MoltenArmorParticles(int particleCount) {
        this.particles = ParticleGroupBuilder.of("falling_lava")
                .batch(b -> b.shape(ParticleGroup.Shape.CIRCLE).count(particleCount).speed(0.05F, 0.08F));
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = particles.copy();
        scaledParticles.batch.count *= (1);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
