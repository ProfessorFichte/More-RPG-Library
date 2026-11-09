package com.mrpg_lib.fx;

import com.mrpg_lib.client.particle.ColoredParticleType;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class FxLayer {
    FxShape shape = FxShape.POINT;
    float radius = 1.0F;
    boolean radiusFromParams = false;
    float growFrom = 1.0F;
    float height = 1.0F;
    float length = 1.0F;
    boolean lengthFromParams = false;
    float arcDegrees = 120.0F;
    float coneDegrees = 30.0F;
    float turns = 2.0F;
    float spread = 0.0F;
    float jitter = 0.0F;
    float count = 1.0F;
    float chance = 1.0F;
    FxVelocity velocity = FxVelocity.NONE;
    float minSpeed = 0.0F;
    float maxSpeed = 0.0F;
    boolean speedFromRadius = false;
    FxMotion motion = FxMotion.DEFAULT;
    FxAnchor anchor = FxAnchor.POSITION;
    float verticalOrigin = 0.5F;
    float yOffset = 0.0F;
    FxOrientation orientation = FxOrientation.WORLD;
    float spinPerTick = 0.0F;
    int delayTicks = 0;
    int lifetime = -1;
    ParticleEffect particle = ParticleTypes.POOF;
    @Nullable Identifier spellEngineParticle;
    int color = -1;
    int colorEnd = -1;
    float particleScale = 1.0F;
    boolean tintable = false;
    @Nullable ColoredParticleType coloredType;

    private FxLayer() {
    }

    public static FxLayer of(FxShape shape) {
        var layer = new FxLayer();
        layer.shape = shape;
        return layer;
    }

    public FxLayer copy() {
        var copy = new FxLayer();
        copy.shape = shape;
        copy.radius = radius;
        copy.radiusFromParams = radiusFromParams;
        copy.growFrom = growFrom;
        copy.height = height;
        copy.length = length;
        copy.lengthFromParams = lengthFromParams;
        copy.arcDegrees = arcDegrees;
        copy.coneDegrees = coneDegrees;
        copy.turns = turns;
        copy.spread = spread;
        copy.jitter = jitter;
        copy.count = count;
        copy.chance = chance;
        copy.velocity = velocity;
        copy.minSpeed = minSpeed;
        copy.maxSpeed = maxSpeed;
        copy.speedFromRadius = speedFromRadius;
        copy.motion = motion;
        copy.anchor = anchor;
        copy.verticalOrigin = verticalOrigin;
        copy.yOffset = yOffset;
        copy.orientation = orientation;
        copy.spinPerTick = spinPerTick;
        copy.delayTicks = delayTicks;
        copy.lifetime = lifetime;
        copy.particle = particle;
        copy.spellEngineParticle = spellEngineParticle;
        copy.color = color;
        copy.colorEnd = colorEnd;
        copy.particleScale = particleScale;
        copy.tintable = tintable;
        copy.coloredType = coloredType;
        return copy;
    }

    public FxLayer radius(float radius) {
        this.radius = radius;
        this.radiusFromParams = false;
        return this;
    }

    public FxLayer radiusFromParams(float factor) {
        this.radius = factor;
        this.radiusFromParams = true;
        return this;
    }

    public FxLayer growFrom(float startFactor) {
        this.growFrom = startFactor;
        return this;
    }

    public FxLayer height(float height) {
        this.height = height;
        return this;
    }

    public FxLayer length(float length) {
        this.length = length;
        this.lengthFromParams = false;
        return this;
    }

    public FxLayer lengthFromParams(float factor) {
        this.length = factor;
        this.lengthFromParams = true;
        return this;
    }

    public FxLayer arc(float degrees) {
        this.arcDegrees = degrees;
        return this;
    }

    public FxLayer cone(float degrees) {
        this.coneDegrees = degrees;
        return this;
    }

    public FxLayer turns(float turns) {
        this.turns = turns;
        return this;
    }

    public FxLayer spread(float radians) {
        this.spread = radians;
        return this;
    }

    public FxLayer jitter(float jitter) {
        this.jitter = jitter;
        return this;
    }

    public FxLayer count(float count) {
        this.count = count;
        return this;
    }

    public FxLayer chance(float chance) {
        this.chance = chance;
        return this;
    }

    public FxLayer velocity(FxVelocity velocity) {
        this.velocity = velocity;
        return this;
    }

    public FxLayer speed(float speed) {
        return speed(speed, speed);
    }

    public FxLayer speed(float min, float max) {
        this.minSpeed = min;
        this.maxSpeed = max;
        return this;
    }

    public FxLayer speedFromRadius() {
        this.speedFromRadius = true;
        return this;
    }

    public FxLayer motion(FxMotion motion) {
        this.motion = motion;
        return this;
    }

    public FxLayer anchor(FxAnchor anchor) {
        this.anchor = anchor;
        return this;
    }

    public FxLayer verticalOrigin(float fractionOfHeight) {
        this.verticalOrigin = fractionOfHeight;
        return this;
    }

    public FxLayer yOffset(float offset) {
        this.yOffset = offset;
        return this;
    }

    public FxLayer orientation(FxOrientation orientation) {
        this.orientation = orientation;
        return this;
    }

    public FxLayer spin(float degreesPerTick) {
        this.spinPerTick = degreesPerTick;
        return this;
    }

    public FxLayer delay(int ticks) {
        this.delayTicks = ticks;
        return this;
    }

    public FxLayer lifetime(int ticks) {
        this.lifetime = ticks;
        return this;
    }

    public FxLayer particle(ParticleEffect particle) {
        this.particle = particle;
        this.tintable = false;
        this.coloredType = null;
        return this;
    }

    public FxLayer recolorable(ColoredParticleType type) {
        this.coloredType = type;
        this.tintable = true;
        this.particleScale = 1.0F;
        return this;
    }

    public FxLayer colored(ColoredParticleType type, int color) {
        return colored(type, color, 1.0F);
    }

    public FxLayer colored(ColoredParticleType type, int color, float scale) {
        this.coloredType = type;
        this.color = color;
        this.colorEnd = -1;
        this.particleScale = scale;
        this.tintable = true;
        this.particle = type.of(color, scale);
        return this;
    }

    public FxLayer particle(ParticleEffect particle, @Nullable String spellEngineParticle) {
        particle(particle);
        return spellEngineParticle(spellEngineParticle);
    }

    public FxLayer spellEngineParticle(@Nullable String id) {
        this.spellEngineParticle = id == null ? null : Identifier.tryParse(id);
        return this;
    }

    public FxLayer dust(int color, float scale) {
        this.coloredType = null;
        this.color = color;
        this.colorEnd = -1;
        this.particleScale = scale;
        this.tintable = true;
        this.particle = new DustParticleEffect(rgb(color), scale);
        return this;
    }

    public FxLayer dust(int color, int colorEnd, float scale) {
        this.coloredType = null;
        this.color = color;
        this.colorEnd = colorEnd;
        this.particleScale = scale;
        this.tintable = true;
        this.particle = new DustColorTransitionParticleEffect(rgb(color), rgb(colorEnd), scale);
        return this;
    }

    ParticleEffect particleFor(int colorOverride) {
        if (!tintable || colorOverride < 0) return particle;
        if (coloredType != null) return coloredType.of(colorOverride, particleScale);
        return colorEnd < 0
                ? new DustParticleEffect(rgb(colorOverride), particleScale)
                : new DustColorTransitionParticleEffect(rgb(colorOverride), rgb(colorEnd), particleScale);
    }

    static Vector3f rgb(int color) {
        return new Vector3f(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
    }

    public FxShape shape() { return shape; }
    public float radius() { return radius; }
    public boolean radiusFromParams() { return radiusFromParams; }
    public float growFrom() { return growFrom; }
    public float height() { return height; }
    public float length() { return length; }
    public boolean lengthFromParams() { return lengthFromParams; }
    public float arcDegrees() { return arcDegrees; }
    public float coneDegrees() { return coneDegrees; }
    public float turns() { return turns; }
    public float spread() { return spread; }
    public float jitter() { return jitter; }
    public float count() { return count; }
    public float chance() { return chance; }
    public FxVelocity velocity() { return velocity; }
    public float minSpeed() { return minSpeed; }
    public float maxSpeed() { return maxSpeed; }
    public boolean scalesSpeedWithRadius() { return speedFromRadius; }
    public FxMotion motion() { return motion; }
    public FxAnchor anchor() { return anchor; }
    public float verticalOrigin() { return verticalOrigin; }
    public float yOffset() { return yOffset; }
    public FxOrientation orientation() { return orientation; }
    public float spinPerTick() { return spinPerTick; }
    public int delayTicks() { return delayTicks; }
    public int lifetime() { return lifetime; }
    public ParticleEffect particle(int colorOverride) { return particleFor(colorOverride); }
    @Nullable public Identifier spellEngineParticle() { return spellEngineParticle; }
    public int color() { return color; }
    public int colorEnd() { return colorEnd; }
    public float particleScale() { return particleScale; }
}
