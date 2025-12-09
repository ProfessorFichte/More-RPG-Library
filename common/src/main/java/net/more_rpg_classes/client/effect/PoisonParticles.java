package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.fx.ParticleHelper;

public class PoisonParticles implements CustomParticleStatusEffect.Spawner{
    private final ParticleBatch particles;

    public PoisonParticles(int particleCount) {
        this.particles = new ParticleBatch(
                "more_rpg_classes:fatal_poison",
                ParticleBatch.Shape.CONE, ParticleBatch.Origin.CENTER,
                null, particleCount, 0.2F, 0.2F, 180);
    }

    private int tickDelay = 0;

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        if (++tickDelay % 15 != 0) return;

        var scaledParticles = new ParticleBatch(particles);
        scaledParticles.count *= (1);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
