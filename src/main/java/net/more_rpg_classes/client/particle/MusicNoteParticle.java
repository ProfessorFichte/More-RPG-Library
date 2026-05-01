package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class MusicNoteParticle extends SpriteBillboardParticle {
    private final float initialVelocityY;

    MusicNoteParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float r, float g, float b, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.scale(1.25F);
        this.setBoundingBoxSpacing(0.25F, 0.25F);
        this.maxAge = this.random.nextInt(20) + 40;

        this.initialVelocityY = 0.06F + this.random.nextFloat() * 0.02F;
        this.velocityY = this.initialVelocityY;

        this.velocityX = velocityX * 0.1 + (this.random.nextFloat() - 0.5) * 0.02;
        this.velocityZ = velocityZ * 0.1 + (this.random.nextFloat() - 0.5) * 0.02;

        this.gravityStrength = 0.002F;

        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = 1.0F;

        this.setSprite(spriteProvider);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge || this.alpha <= 0.0F) {
            this.markDead();
            return;
        }

        this.velocityY -= this.gravityStrength;

        this.velocityX += (this.random.nextFloat() - 0.5) * 0.001;
        this.velocityZ += (this.random.nextFloat() - 0.5) * 0.001;

        this.velocityX *= 0.98;
        this.velocityZ *= 0.98;

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        if (this.age >= this.maxAge - 15 && this.alpha > 0.01F) {
            this.alpha -= 0.06F;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class WhiteFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public WhiteFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 1.0F, 1.0F, 1.0F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class RedFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public RedFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 1.0F, 0.2F, 0.2F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class PurpleFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public PurpleFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.7F, 0.1F, 1.0F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class GreenFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public GreenFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.2F, 1.0F, 0.3F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class YellowFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public YellowFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 1.0F, 0.9F, 0.1F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class BlueFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public BlueFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.1F, 0.5F, 1.0F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class CyanFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public CyanFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.0F, 1.0F, 1.0F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class GoldFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public GoldFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 1.0F, 0.843F, 0.0F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class BrightGreenFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public BrightGreenFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.557F, 0.996F, 0.631F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class YellowGreenFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public YellowGreenFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.686F, 0.996F, 0.051F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class MagentaFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public MagentaFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.486F, 0.216F, 0.961F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class BrightMagentaFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public BrightMagentaFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.867F, 0.416F, 0.969F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class MidMagentaFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public MidMagentaFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.627F, 0.51F, 0.808F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ArcaneFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public ArcaneFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 1.0F, 0.4F, 1.0F, spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class RageFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public RageFactory(SpriteProvider spriteProvider) { this.spriteProvider = spriteProvider; }
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new MusicNoteParticle(world, x, y, z, vx, vy, vz, 0.749F, 0.251F, 0.251F, spriteProvider);
        }
    }
}
