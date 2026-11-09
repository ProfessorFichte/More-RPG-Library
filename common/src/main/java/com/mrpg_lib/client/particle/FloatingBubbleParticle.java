package com.mrpg_lib.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

public class FloatingBubbleParticle extends SpriteBillboardParticle {
    private static final float FADE_OUT = 0.25F;
    private final SpriteProvider spriteProvider;
    private final float baseScale;

    protected FloatingBubbleParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.spriteProvider = spriteProvider;
        this.velocityX = vx;
        this.velocityY = vy + 0.004;
        this.velocityZ = vz;
        this.velocityMultiplier = 0.94F;
        this.gravityStrength = -0.015F;
        this.collidesWithWorld = true;
        this.maxAge = 30 + this.random.nextInt(20);
        this.baseScale = 0.1F + this.random.nextFloat() * 0.08F;
        this.scale = this.baseScale;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.dead) return;
        this.setSpriteForAge(this.spriteProvider);
        float progress = (float) this.age / (float) this.maxAge;
        this.alpha = MathHelper.clamp((1.0F - progress) / FADE_OUT, 0.0F, 1.0F);
        this.velocityX += (this.random.nextFloat() - 0.5F) * 0.004F;
        this.velocityZ += (this.random.nextFloat() - 0.5F) * 0.004F;
    }

    @Override
    protected int getBrightness(float tint) {
        return 0xF000F0;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new FloatingBubbleParticle(world, x, y, z, vx, vy, vz, this.spriteProvider);
        }
    }
}
