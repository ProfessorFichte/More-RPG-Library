package com.mrpg_lib.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

public class FadingMoteParticle extends SpriteBillboardParticle {
    private static final float FADE_IN = 0.2F;
    private static final float FADE_OUT = 0.5F;
    private final float baseScale;
    private final SpriteProvider spriteProvider;

    protected FadingMoteParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.spriteProvider = spriteProvider;
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        this.velocityMultiplier = 0.98F;
        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.maxAge = 40 + this.random.nextInt(30);
        this.baseScale = 0.08F + this.random.nextFloat() * 0.05F;
        this.scale = this.baseScale;
        this.alpha = 0.0F;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.dead) {
            this.setSpriteForAge(this.spriteProvider);
        }
        float progress = (float) this.age / (float) this.maxAge;
        float fadeIn = MathHelper.clamp(progress / FADE_IN, 0.0F, 1.0F);
        float fadeOut = MathHelper.clamp((1.0F - progress) / FADE_OUT, 0.0F, 1.0F);
        this.alpha = fadeIn * fadeOut;
        this.scale = this.baseScale * (0.6F + 0.4F * fadeIn);
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale;
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
            return new FadingMoteParticle(world, x, y, z, vx, vy, vz, this.spriteProvider);
        }
    }
}
