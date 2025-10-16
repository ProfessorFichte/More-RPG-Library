package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.fx.ParticleHelper;

public class MoltenArmorParticles implements CustomParticleStatusEffect.Spawner{
    private final ParticleBatch particles;

    public MoltenArmorParticles(int particleCount) {
        this.particles = new ParticleBatch(
                "falling_lava",
                ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                null, particleCount, 0.05F, 0.08F, 360);
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = new ParticleBatch(particles);
        scaledParticles.count *= (1);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
