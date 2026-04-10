package net.more_rpg_classes.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PopupParticle extends BillboardParticle {
    private static final int POP_UP_TICKS = 8;
    private static final int STAY_TICKS = 40;
    private static final int SHRINK_TICKS = 6;
    private static final float BASE_SCALE = 0.5f;
    private static final float MAX_SCALE = 0.55f;

    private final int entityId;
    private float currentOffsetY = 0f;
    private final Identifier textureId;
    private float minU = 0f, maxU = 1f, minV = 0f, maxV = 1f;

    private PopupParticle(ClientWorld world, double x, double y, double z, PopupParticleEffect effect) {
        super(world, x, y, z);
        this.entityId = effect.entityId;
        this.maxAge = POP_UP_TICKS + STAY_TICKS + SHRINK_TICKS;
        this.scale = 0f;
        this.collidesWithWorld = false;
        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;

        if (effect.isSpell) {
            this.textureId = Identifier.of(effect.iconId.getNamespace(), "textures/spell/" + effect.iconId.getPath() + ".png");
        } else {
            this.textureId = Identifier.ofVanilla("textures/atlas/mob_effects.png");
            var maybeEntry = world.getRegistryManager()
                .get(RegistryKeys.STATUS_EFFECT)
                .getEntry(effect.iconId);
            if (maybeEntry.isPresent()) {
                RegistryEntry.Reference<StatusEffect> entry = maybeEntry.get();
                var sprite = MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(entry);
                if (sprite != null) {
                    this.minU = sprite.getMinU();
                    this.maxU = sprite.getMaxU();
                    this.minV = sprite.getMinV();
                    this.maxV = sprite.getMaxV();
                } else {
                    this.markDead();
                }
            } else {
                this.markDead();
            }
        }
    }

    @Override protected float getMinU() { return minU; }
    @Override protected float getMaxU() { return maxU; }
    @Override protected float getMinV() { return minV; }
    @Override protected float getMaxV() { return maxV; }

    @Override
    public void buildGeometry(VertexConsumer ignored, Camera camera, float tickDelta) {
        if (this.scale <= 0 || this.alpha <= 0) return;

        float px = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camera.getPos().x);
        float py = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camera.getPos().y);
        float pz = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camera.getPos().z);

        Quaternionf rot = camera.getRotation();
        float size = this.scale;
        int light = this.getBrightness(tickDelta);

        float[][] corners = {
            { 1f, -1f, maxU, maxV },
            { 1f,  1f, maxU, minV },
            {-1f,  1f, minU, minV },
            {-1f, -1f, minU, maxV }
        };

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, textureId);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        for (float[] c : corners) {
            Vector3f pos = new Vector3f(c[0], c[1], 0f).rotate(rot).mul(size).add(px, py, pz);
            builder.vertex(pos.x, pos.y, pos.z)
                .texture(c[2], c[3])
                .color(this.red, this.green, this.blue, this.alpha)
                .light(light);
        }
        BuiltBuffer built = builder.endNullable();
        if (built != null) {
            try {
                BufferRenderer.drawWithGlobalProgram(built);
            } finally {
                built.close();
            }
        }
        RenderSystem.disableBlend();
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        Entity entity = this.world.getEntityById(entityId);
        if (entity == null) {
            this.markDead();
            return;
        }

        if (this.age < POP_UP_TICKS) {
            float t = (float) this.age / POP_UP_TICKS;
            currentOffsetY = t * 0.5f;
            this.scale = BASE_SCALE * t;
        } else if (this.age < POP_UP_TICKS + STAY_TICKS) {
            float t = (float) (this.age - POP_UP_TICKS) / STAY_TICKS;
            currentOffsetY = 0.5f;
            this.scale = BASE_SCALE + (MAX_SCALE - BASE_SCALE) * t;
        } else {
            float t = (float) (this.age - POP_UP_TICKS - STAY_TICKS) / SHRINK_TICKS;
            currentOffsetY = 0.5f;
            this.scale = MAX_SCALE * (1f - t);
            this.alpha = 1f - t;
        }

        this.x = entity.getX();
        this.y = entity.getY() + entity.getHeight() + 0.3 + currentOffsetY;
        this.z = entity.getZ();

        this.age++;
        if (this.age >= this.maxAge) {
            this.markDead();
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    public static class Factory implements ParticleFactory<PopupParticleEffect> {
        @Override
        public Particle createParticle(PopupParticleEffect effect, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
            return new PopupParticle(world, x, y, z, effect);
        }
    }
}
