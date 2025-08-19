package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.spell_engine.client.particle.ShiftedParticle;

import java.util.ArrayList;
import java.util.List;

public class IceTrapParticle extends ShiftedParticle {
    private final SpriteProvider spriteProvider;
    private final List<Sprite> frames;

    protected IceTrapParticle(ClientWorld world, double x, double y, double z,
                              double velocityX, double velocityY, double velocityZ,
                              SpriteProvider spriteProvider,
                              List<Sprite> frames) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
        this.frames = frames;
        this.spriteProvider = spriteProvider;
        this.gravityStrength = 0.225f;
        this.velocityMultiplier = 1.0f;
        this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * (double) 0.05f;
        this.scale = 0.4F;
        this.maxAge = 40;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();
        this.velocityX *= 0.95f;
        this.velocityY *= 0.9f;
        this.velocityZ *= 0.95f;

        // Example: grow slightly in first few ticks
        if (this.age < 10) {
            this.scale += 0.01f;
        }

        final int frameRate = 3;
        int frame = (this.age / frameRate);

        if (frame < 7) {
            this.setSprite(frames.get(frame));
        } else {
            int loopFrame = 7 + ((frame - 7) % 5);
            this.setSprite(frames.get(loopFrame));
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class IceTrapParticleFactory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public IceTrapParticleFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            List<Sprite> sprites = new ArrayList<>(12);
            for (int i = 0; i < 12; i++) {
                sprites.add(spriteProvider.getSprite(i, 12));
            }
            return new IceTrapParticle(world, x, y, z, vx, vy, vz, spriteProvider, sprites);
        }
    }
}
