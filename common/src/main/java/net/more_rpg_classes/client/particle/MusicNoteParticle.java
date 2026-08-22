package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.spell_engine.client.particle.SpellParticle;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.ParticleGroupType;
import net.spell_engine.fx.SpellEngineParticles;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MusicNoteParticle extends SpriteBillboardParticle {

    MusicNoteParticle(ClientWorld world, double x, double y, double z,
                      double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z);
        this.scale(1.25F);
        this.setBoundingBoxSpacing(0.25F, 0.25F);
        this.maxAge = this.random.nextInt(20) + 40;

        this.velocityY = 0.06F + this.random.nextFloat() * 0.02F;

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
    public static class Factory implements ParticleFactory<ParticleGroupType> {
        private final SpriteProvider spriteProvider;
        private final SpellEngineParticles.Entry entry;

        public Factory(SpriteProvider spriteProvider, SpellEngineParticles.Entry entry) {
            this.spriteProvider = spriteProvider;
            this.entry = entry;
        }

        @Nullable
        @Override
        public Particle createParticle(ParticleGroupType type, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            var particle = new MusicNoteParticle(world, x, y, z, velocityX, velocityY, velocityZ);

            var appearance = SpellParticle.Factory.resolve(entry, type.payload());
            if (appearance.color >= 0) {
                var color = Color.fromRGBA(appearance.color);
                particle.setColor(color.red(), color.green(), color.blue());
                particle.alpha *= color.alpha();
            }
            particle.alpha *= appearance.opacity;
            particle.scale *= appearance.scale;

            particle.setSprite(this.spriteProvider);
            return particle;
        }
    }
}
