package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.fx.ParticleHelper;

public class SoakedParticles implements CustomParticleStatusEffect.Spawner{
    private final ParticleBatch particles;


    public SoakedParticles() {
        this.particles =
        new ParticleBatch(
                "more_rpg_classes:splash",
                ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                null, 20, 0.1F, 0.3F, 360);
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = new ParticleBatch(particles);
        scaledParticles.count *= (1);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
