package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.spell_engine.client.particle.TemplateParticleType;

@Environment(EnvType.CLIENT)
public class StarParticle extends SpriteBillboardParticle {

    StarParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z);
        this.scale(1.0F);
        this.setBoundingBoxSpacing(0.2F, 0.2F);
        this.maxAge = this.random.nextInt(30) + 50;

        this.velocityX = velocityX * 0.05 + (this.random.nextFloat() - 0.5) * 0.012;
        this.velocityY = 0.012F + this.random.nextFloat() * 0.008F;
        this.velocityZ = velocityZ * 0.05 + (this.random.nextFloat() - 0.5) * 0.012;

        this.gravityStrength = 0.0F;

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

        this.velocityX *= 0.97;
        this.velocityZ *= 0.97;

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        if (this.age >= this.maxAge - 20 && this.alpha > 0.01F) {
            this.alpha -= 0.05F;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<TemplateParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(TemplateParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            StarParticle particle = new StarParticle(world, x, y, z, vx, vy, vz);

            TemplateParticleType.apply(type, particle);

            var appearance = type.getAppearance();
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
