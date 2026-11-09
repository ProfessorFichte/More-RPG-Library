package com.mrpg_lib.client.particle;

import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;

public class ColoredExplosionEmitterParticle extends NoRenderParticle {
    private final int color;
    private final float explosionScale;

    protected ColoredExplosionEmitterParticle(ClientWorld world, double x, double y, double z, int color, float explosionScale) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.color = color;
        this.explosionScale = explosionScale;
        this.maxAge = 8;
    }

    @Override
    public void tick() {
        var effect = ColoredParticles.EXPLOSION.of(color, explosionScale);
        for (int i = 0; i < 6; i++) {
            double offsetX = x + (random.nextDouble() - random.nextDouble()) * 4.0;
            double offsetY = y + (random.nextDouble() - random.nextDouble()) * 4.0;
            double offsetZ = z + (random.nextDouble() - random.nextDouble()) * 4.0;
            world.addParticle(effect, offsetX, offsetY, offsetZ, (float) age / maxAge, 0.0, 0.0);
        }
        if (++age == maxAge) {
            markDead();
        }
    }

    public static class Factory implements ParticleFactory<ColoredParticleEffect> {
        @Override
        public Particle createParticle(ColoredParticleEffect effect, ClientWorld world, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new ColoredExplosionEmitterParticle(world, x, y, z, effect.color(), effect.scale());
        }
    }
}
