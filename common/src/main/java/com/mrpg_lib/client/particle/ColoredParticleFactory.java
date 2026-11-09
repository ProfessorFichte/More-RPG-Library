package com.mrpg_lib.client.particle;

import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public final class ColoredParticleFactory implements ParticleFactory<ColoredParticleEffect> {
    private final ParticleFactory<SimpleParticleType> vanilla;

    public ColoredParticleFactory(ParticleFactory<SimpleParticleType> vanilla) {
        this.vanilla = vanilla;
    }

    @Nullable
    @Override
    public Particle createParticle(ColoredParticleEffect effect, ClientWorld world, double x, double y, double z,
                                   double velocityX, double velocityY, double velocityZ) {
        var particle = vanilla.createParticle(effect.type().vanilla(), world, x, y, z, velocityX, velocityY, velocityZ);
        if (particle != null) {
            tint(particle, effect.color(), world.random.nextFloat());
            if (effect.scale() != 1.0F) {
                particle.scale(effect.scale());
            }
        }
        return particle;
    }

    static void tint(Particle particle, int color, float shadeRoll) {
        float shade = 0.85F + shadeRoll * 0.15F;
        int shaded = ((int) (((color >> 16) & 0xFF) * shade) << 16) | ((int) (((color >> 8) & 0xFF) * shade) << 8) | (int) ((color & 0xFF) * shade);
        if (particle instanceof AnimatedParticle animated) {
            animated.setColor(shaded);
            animated.setTargetColor(shaded);
        } else {
            particle.setColor(((shaded >> 16) & 0xFF) / 255.0F, ((shaded >> 8) & 0xFF) / 255.0F, (shaded & 0xFF) / 255.0F);
        }
    }
}
