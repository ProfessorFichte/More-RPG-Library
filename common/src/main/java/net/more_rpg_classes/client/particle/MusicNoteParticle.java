package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.spell_engine.client.particle.TemplateParticleType;

@Environment(EnvType.CLIENT)
public class MusicNoteParticle extends SpriteBillboardParticle {
    private final float initialVelocityY;

    MusicNoteParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z);
        this.scale(1.25F);
        this.setBoundingBoxSpacing(0.25F, 0.25F);
        this.maxAge = this.random.nextInt(20) + 40;

        this.initialVelocityY = 0.06F + this.random.nextFloat() * 0.02F;
        this.velocityY = this.initialVelocityY;

        this.velocityX = velocityX * 0.1 + (this.random.nextFloat() - 0.5) * 0.02;
        this.velocityZ = velocityZ * 0.1 + (this.random.nextFloat() - 0.5) * 0.02;

        this.gravityStrength = 0.002F;

        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
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
    public static class MusicNoteFactory implements ParticleFactory<TemplateParticleType> {
        private final SpriteProvider spriteProvider;

        public MusicNoteFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(TemplateParticleType templateParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            MusicNoteParticle particle = new MusicNoteParticle(clientWorld, d, e, f, g, h, i);

            TemplateParticleType.apply(templateParticleType, particle);

            var appearance = templateParticleType.getAppearance();
            if (appearance != null) {
                if (appearance.color != null) {
                    particle.alpha *= appearance.color.alpha();
                }
                particle.scale *= appearance.scale;
            }

            particle.setSprite(this.spriteProvider);
            return particle;
        }
    }
}
