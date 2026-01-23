package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RageParticle extends SpriteBillboardParticle {

    RageParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z);

        float offsetX = (this.random.nextFloat() - 0.5F) * 0.6F;
        float offsetY = this.random.nextFloat() * 0.4F;
        float offsetZ = (this.random.nextFloat() - 0.5F) * 0.6F;

        this.x = x + offsetX;
        this.y = y + offsetY;
        this.z = z + offsetZ;

        this.scale(1.5F);
        this.setBoundingBoxSpacing(0.25F, 0.25F);
        this.maxAge = this.random.nextInt(15) + 25;

        this.velocityY = 0.03F + this.random.nextFloat() * 0.02F;
        this.velocityX = (this.random.nextFloat() - 0.5) * 0.01;
        this.velocityZ = (this.random.nextFloat() - 0.5) * 0.01;

        this.gravityStrength = 0.001F;

        this.red = 0.85F + this.random.nextFloat() * 0.15F;
        this.green = 0.1F + this.random.nextFloat() * 0.1F;
        this.blue = 0.1F + this.random.nextFloat() * 0.1F;
        this.alpha = 1.0F;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        this.velocityY -= this.gravityStrength;

        this.velocityX += (this.random.nextFloat() - 0.5) * 0.002;
        this.velocityZ += (this.random.nextFloat() - 0.5) * 0.002;

        this.velocityX *= 0.95;
        this.velocityZ *= 0.95;

        this.move(this.velocityX, this.velocityY, this.velocityZ);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            RageParticle particle = new RageParticle(world, x, y, z, velocityX, velocityY, velocityZ);
            particle.setSprite(this.spriteProvider);
            return particle;
        }
    }
}
