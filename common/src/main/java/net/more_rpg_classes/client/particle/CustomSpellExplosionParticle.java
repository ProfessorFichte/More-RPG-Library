package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class CustomSpellExplosionParticle extends ExplosionLargeParticle {
    protected CustomSpellExplosionParticle(ClientWorld world, double x, double y, double z, double d, SpriteProvider spriteProvider) {
        super(world, x, y, z, d, spriteProvider);
    }
    public ParticleTextureSheet getType() {

        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {

        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            var particle = new CustomSpellExplosionParticle(clientWorld, d, e, f, g, this.spriteProvider);
            particle.scale = 0.8F;
            particle.red = 1F;
            particle.green = 1F;
            particle.blue = 1F;
            particle.maxAge = 12;
            return particle;
        }
    }
}
