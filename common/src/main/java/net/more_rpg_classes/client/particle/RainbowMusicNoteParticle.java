package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RainbowMusicNoteParticle extends SpriteBillboardParticle {
    private static final float[][] COLOR_GRADIENT = {
        {0.0f, 1.0f, 0.357f, 0.357f},     // Red
        {0.13f, 1.0f, 0.859f, 0.0f},      // Yellow
        {0.31f, 0.514f, 1.0f, 0.0f},      // Green
        {0.5f, 0.0f, 0.976f, 1.0f},       // Cyan
        {0.7f, 0.0f, 0.620f, 1.0f},       // Blue
        {0.84f, 0.690f, 0.247f, 1.0f},    // Purple
        {1.0f, 1.0f, 0.357f, 0.357f}      // Red (loop)
    };

    private static final float[] SIZE_NODES = {0.14f, 0.21f, 0.13f, 0.08f, 0.05f, 0.04f};

    public RainbowMusicNoteParticle(ClientWorld world, double x, double y, double z,
                                     double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        this.maxAge = 20;

        this.scale = 0.14f;

        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;

        this.velocityMultiplier = 1.0f;

        this.gravityStrength = 0.0f;

        this.red = 1.0f;
        this.green = 0.357f;
        this.blue = 0.357f;
        this.alpha = 1.0f;
    }

    @Override
    public void tick() {
        super.tick();
        updateColor();

        updateSize();
    }

    private void updateColor() {
        float agePercent = (float) this.age / (float) this.maxAge;

        int lowerIndex = 0;
        int upperIndex = 1;

        for (int i = 0; i < COLOR_GRADIENT.length - 1; i++) {
            if (agePercent >= COLOR_GRADIENT[i][0] && agePercent <= COLOR_GRADIENT[i + 1][0]) {
                lowerIndex = i;
                upperIndex = i + 1;
                break;
            }
        }

        float[] lower = COLOR_GRADIENT[lowerIndex];
        float[] upper = COLOR_GRADIENT[upperIndex];

        float localPercent = (agePercent - lower[0]) / (upper[0] - lower[0]);

        this.red = lerp(lower[1], upper[1], localPercent);
        this.green = lerp(lower[2], upper[2], localPercent);
        this.blue = lerp(lower[3], upper[3], localPercent);
        this.alpha = 1.0f;
    }

    private void updateSize() {
        float agePercent = (float) this.age / (float) this.maxAge;

        float nodePosition = agePercent * (SIZE_NODES.length - 1);
        int lowerNode = (int) Math.floor(nodePosition);
        int upperNode = (int) Math.ceil(nodePosition);

        lowerNode = Math.max(0, Math.min(SIZE_NODES.length - 1, lowerNode));
        upperNode = Math.max(0, Math.min(SIZE_NODES.length - 1, upperNode));

        float localPercent = nodePosition - lowerNode;
        this.scale = lerp(SIZE_NODES[lowerNode], SIZE_NODES[upperNode], localPercent);
    }

    private float lerp(float start, float end, float percent) {
        return start + (end - start) * percent;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getBrightness(float tint) {
        return 15728880;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            RainbowMusicNoteParticle particle = new RainbowMusicNoteParticle(
                world, x, y, z, velocityX, velocityY, velocityZ
            );
            particle.setSprite(this.spriteProvider);
            return particle;
        }
    }
}
