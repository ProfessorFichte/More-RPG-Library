package net.more_rpg_classes.client.effect;

import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_engine.api.spell.fx.ParticleGroupBuilder;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.fx.SpellEngineParticles;

public class IgnitedParticles implements CustomParticleStatusEffect.Spawner {

    private final ParticleGroup particles;

    /// V1 used the raw id "minecraft:flame"; Spell Engine's flame entry draws the same
    /// vanilla texture but honours the appearance payload.
    public IgnitedParticles(int particleCount) {
        this.particles = ParticleGroupBuilder.of(SpellEngineParticles.flame)
                .batch(b -> b.shape(ParticleGroup.Shape.SPHERE).count(particleCount).speed(0.1F, 0.15F));
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = particles.copy();
        scaledParticles.batch.count *= (1);
        ParticleHelper.play(livingEntity.getWorld(), livingEntity, scaledParticles);
    }
}
