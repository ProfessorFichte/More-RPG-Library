package com.mrpg_lib.fx;

import com.mrpg_lib.client.particle.ColoredParticles;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public final class FxPresets {
    private FxPresets() {
    }

    public static final FxPreset EXPLOSION = new FxPreset("explosion",
            t -> t.radius(3.0F).speed(0.25F).sound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0F, 1.0F),
            t -> FxEffect.builder().radius(t.radius)
                    .layer(core(FxLayer.of(FxShape.SPHERE).radiusFromParams(0.3F).count((2.0F + t.radius * 0.8F) * t.count),
                            t, ParticleTypes.EXPLOSION))
                    .layer(main(FxLayer.of(FxShape.SPHERE).radiusFromParams(0.15F).count(28.0F * t.count)
                            .velocity(FxVelocity.OUTWARD).speed(t.speed * 0.6F, t.speed).speedFromRadius(), t, ParticleTypes.POOF, 1.4F))
                    .layer(tinted(FxLayer.of(FxShape.SHELL).radiusFromParams(0.55F).count(18.0F * t.count)
                            .velocity(FxVelocity.OUTWARD).speed(t.speed * 0.2F, t.speed * 0.4F).speedFromRadius(), t, ParticleTypes.SMOKE, 1.2F))
                    .layer(main(FxLayer.of(FxShape.RING).radiusFromParams(0.9F).anchor(FxAnchor.GROUND).count((16.0F + t.radius * 4.0F) * t.count)
                            .velocity(FxVelocity.OUTWARD).speed(0.02F, 0.05F), t, ParticleTypes.CLOUD, 1.0F)));

    public static final FxPreset BURST = new FxPreset("burst",
            t -> t.radius(0.5F).speed(0.15F),
            t -> FxEffect.builder().radius(t.radius)
                    .layer(main(FxLayer.of(FxShape.POINT).count(14.0F * t.count)
                            .velocity(FxVelocity.OUTWARD).speed(t.speed * 0.5F, t.speed).speedFromRadius(), t, ParticleTypes.POOF, 1.0F)));

    public static final FxPreset SHOCKWAVE = new FxPreset("shockwave",
            t -> t.radius(4.0F).duration(10),
            t -> FxEffect.builder().radius(t.radius).duration(t.duration)
                    .layer(main(FxLayer.of(FxShape.RING).radiusFromParams(1.0F).growFrom(0.1F).anchor(FxAnchor.GROUND)
                            .count(18.0F * t.count).velocity(FxVelocity.OUTWARD).speed(0.02F, 0.06F), t, ParticleTypes.CLOUD, 1.2F)));

    public static final FxPreset AURA = new FxPreset("aura",
            t -> t.radius(0.8F).height(2.0F).duration(60),
            t -> FxEffect.builder().radius(t.radius).duration(t.duration).follow()
                    .layer(main(FxLayer.of(FxShape.CYLINDER).radiusFromParams(1.0F).height(t.height).count(1.5F * t.count)
                            .velocity(FxVelocity.UP).speed(0.02F, 0.05F), t, ParticleTypes.END_ROD, 0.8F))
                    .layer(tinted(FxLayer.of(FxShape.COLUMN).radiusFromParams(0.6F).height(t.height * 0.8F).count(0.6F * t.count)
                            .motion(FxMotion.FLOAT), t, ParticleTypes.WITCH, 0.8F)));

    public static final FxPreset SWIRL = new FxPreset("swirl",
            t -> t.radius(2.0F).height(1.5F).turns(2.0F).duration(40),
            t -> FxEffect.builder().radius(t.radius).duration(t.duration).follow()
                    .layer(main(FxLayer.of(FxShape.HELIX).radiusFromParams(1.0F).height(t.height).turns(t.turns).count(4.0F * t.count)
                            .velocity(t.inward ? FxVelocity.INWARD : FxVelocity.TANGENT).speed(0.05F, 0.1F).spin(12.0F), t, ParticleTypes.CLOUD, 1.0F))
                    .layer(tinted(FxLayer.of(FxShape.SPIRAL).radiusFromParams(1.0F).turns(t.turns).count(2.0F * t.count).yOffset(0.1F)
                            .velocity(FxVelocity.TANGENT).speed(0.03F, 0.06F).spin(18.0F), t, ParticleTypes.CLOUD, 0.9F)));

    public static final FxPreset BEAM = new FxPreset("beam",
            t -> t.length(8.0F).width(0.15F),
            t -> FxEffect.builder().length(t.length)
                    .layer(main(FxLayer.of(FxShape.BEAM).lengthFromParams(1.0F).count(t.length * 4.0F * t.count).jitter(t.width * 0.5F),
                            t, ParticleTypes.END_ROD, 0.8F))
                    .layer(tinted(FxLayer.of(FxShape.BEAM).lengthFromParams(1.0F).count(t.length * 2.0F * t.count).jitter(t.width)
                            .velocity(FxVelocity.RANDOM).speed(0.005F, 0.02F), t, ParticleTypes.ENCHANTED_HIT, 0.7F)));

    public static final FxPreset TRAIL = new FxPreset("trail",
            t -> t.length(1.5F),
            t -> FxEffect.builder().length(t.length)
                    .layer(main(FxLayer.of(FxShape.LINE).lengthFromParams(1.0F).orientation(FxOrientation.LOOK).count(6.0F * t.count).jitter(0.05F),
                            t, ParticleTypes.CLOUD, 0.8F)));

    public static final FxPreset IMPACT = new FxPreset("impact",
            t -> t.radius(0.6F).speed(0.2F),
            t -> FxEffect.builder().radius(t.radius)
                    .layer(core(FxLayer.of(FxShape.POINT).count(1.0F), t, ParticleTypes.FLASH))
                    .layer(main(FxLayer.of(FxShape.POINT).count(10.0F * t.count)
                            .velocity(FxVelocity.OUTWARD).speed(t.speed * 0.4F, t.speed).speedFromRadius(), t, ParticleTypes.CRIT, 1.0F))
                    .layer(tinted(FxLayer.of(FxShape.RING).radiusFromParams(1.0F).count(10.0F * t.count)
                            .velocity(FxVelocity.OUTWARD).speed(0.02F, 0.04F), t, ParticleTypes.ENCHANTED_HIT, 0.9F)));

    public static final FxPreset CAST = new FxPreset("cast",
            t -> t.radius(0.6F),
            t -> FxEffect.builder().radius(t.radius)
                    .layer(main(FxLayer.of(FxShape.SHELL).radiusFromParams(1.0F).count(14.0F * t.count)
                            .velocity(FxVelocity.INWARD).speed(0.04F, 0.08F).speedFromRadius(), t, ParticleTypes.END_ROD, 0.8F))
                    .layer(tinted(FxLayer.of(FxShape.POINT).count(6.0F * t.count)
                            .velocity(FxVelocity.RANDOM).speed(0.01F, 0.03F), t, ParticleTypes.WITCH, 0.8F)));

    public static final FxPreset PILLAR = new FxPreset("pillar",
            t -> t.radius(0.7F).height(4.0F),
            t -> FxEffect.builder().radius(t.radius)
                    .layer(main(FxLayer.of(FxShape.COLUMN).radiusFromParams(1.0F).height(t.height).count(30.0F * t.count)
                            .velocity(FxVelocity.UP).speed(0.01F, 0.04F), t, ParticleTypes.END_ROD, 0.9F))
                    .layer(tinted(FxLayer.of(FxShape.RING).radiusFromParams(1.2F).anchor(FxAnchor.GROUND).count(16.0F * t.count),
                            t, ParticleTypes.CLOUD, 1.0F)));

    public static final FxPreset CLOUD = new FxPreset("cloud",
            t -> t.radius(3.0F).duration(100),
            t -> FxEffect.builder().radius(t.radius).duration(t.duration).interval(2)
                    .layer(main(FxLayer.of(FxShape.DISC).radiusFromParams(1.0F).yOffset(0.2F).count(3.0F * t.count)
                            .motion(FxMotion.DRIFT), t, ParticleTypes.CLOUD, 1.2F))
                    .layer(tinted(FxLayer.of(FxShape.SPHERE).radiusFromParams(0.8F).yOffset(0.6F).count(1.0F * t.count)
                            .motion(FxMotion.FLOAT), t, ParticleTypes.CLOUD, 1.0F)));

    public static final FxPreset SLASH = new FxPreset("slash",
            t -> t.radius(2.0F).angle(140.0F),
            t -> FxEffect.builder().radius(t.radius)
                    .layer(main(FxLayer.of(FxShape.ARC).radiusFromParams(1.0F).arc(t.angle).orientation(FxOrientation.WORLD).count(18.0F * t.count)
                            .velocity(FxVelocity.TANGENT).speed(0.02F), t, ParticleTypes.CRIT, 1.0F))
                    .layer(tinted(FxLayer.of(FxShape.ARC).radiusFromParams(0.85F).arc(t.angle).orientation(FxOrientation.WORLD).count(10.0F * t.count),
                            t, ParticleTypes.ENCHANTED_HIT, 0.9F)));

    public static final FxPreset STORM = new FxPreset("storm",
            t -> t.radius(2.5F).height(2.0F).turns(3.0F).duration(100)
                    .sound(FxSound.of(SoundEvents.ITEM_ELYTRA_FLYING, 1.0F, 1.8F).repeat(40)),
            t -> FxEffect.builder().radius(t.radius).duration(t.duration).follow()
                    .layer(main(FxLayer.of(FxShape.HELIX).radiusFromParams(1.0F).height(t.height).turns(t.turns).spread(MathHelper.TAU).jitter(0.15F)
                            .count(14.0F * t.count).velocity(FxVelocity.TANGENT).speed(0.08F, 0.12F), t, ParticleTypes.SNOWFLAKE, 1.0F))
                    .layer(tinted(FxLayer.of(FxShape.HELIX).radiusFromParams(0.5F).height(t.height * 0.5F).turns(t.turns).spread(MathHelper.TAU).jitter(0.15F)
                            .count(7.0F * t.count).velocity(FxVelocity.TANGENT).speed(0.08F, 0.12F), t, ParticleTypes.SNOWFLAKE, 0.9F)));

    public static List<FxPreset> all() {
        return List.of(EXPLOSION, BURST, SHOCKWAVE, AURA, SWIRL, BEAM, TRAIL, IMPACT, CAST, PILLAR, CLOUD, SLASH, STORM);
    }

    private static FxLayer main(FxLayer layer, FxTweaks t, ParticleEffect fallback, float dustScale) {
        ParticleEffect base = t.particle != null ? t.particle : fallback;
        if (t.color >= 0 && !(t.particle != null && ColoredParticles.variantOf(t.particle) == null)) {
            colored(layer, base, t.color, dustScale);
        } else {
            vanilla(layer, base);
        }
        return layer.spellEngineParticle(t.spellEngineParticle);
    }

    private static FxLayer tinted(FxLayer layer, FxTweaks t, ParticleEffect fallback, float dustScale) {
        ParticleEffect base = t.secondaryParticle != null ? t.secondaryParticle : t.particle != null ? t.particle : fallback;
        int color = t.color2 >= 0 ? t.color2 : t.color;
        boolean explicitUntintable = (t.secondaryParticle != null || t.particle != null) && ColoredParticles.variantOf(base) == null;
        if (color >= 0 && !explicitUntintable) {
            colored(layer, base, color, dustScale);
        } else {
            vanilla(layer, base);
        }
        return layer.spellEngineParticle(t.secondarySpellEngineParticle != null ? t.secondarySpellEngineParticle : t.spellEngineParticle);
    }

    private static FxLayer core(FxLayer layer, FxTweaks t, ParticleEffect fallback) {
        ParticleEffect base = t.secondaryParticle != null ? t.secondaryParticle : fallback;
        var variant = ColoredParticles.variantOf(base);
        if (t.color >= 0 && variant != null) {
            layer.colored(variant, t.color);
        } else {
            vanilla(layer, base);
        }
        return layer.spellEngineParticle(t.secondarySpellEngineParticle);
    }

    private static void vanilla(FxLayer layer, ParticleEffect base) {
        layer.particle(base);
        var variant = ColoredParticles.variantOf(base);
        if (variant != null) {
            layer.recolorable(variant);
        }
    }

    private static void colored(FxLayer layer, ParticleEffect base, int color, float dustScale) {
        var variant = ColoredParticles.variantOf(base);
        if (variant != null) {
            layer.colored(variant, color);
        } else {
            layer.dust(color, dustScale);
        }
    }
}
