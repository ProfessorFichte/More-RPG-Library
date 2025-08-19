package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.fx.ParticleHelper;

public class FrostedParticles implements CustomParticleStatusEffect.Spawner{
    private final ParticleBatch particles;

    public FrostedParticles(int particleCount) {
        this.particles = new ParticleBatch(
                "spell_engine:magic_frost_impact_burst",
                ParticleBatch.Shape.CONE, ParticleBatch.Origin.CENTER ,
                null, particleCount, 0.2F, 0.8F, 360);
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = new ParticleBatch(particles);
        scaledParticles.count *= (1);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
