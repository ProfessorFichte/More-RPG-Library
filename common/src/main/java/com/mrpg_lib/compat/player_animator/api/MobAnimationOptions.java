package com.mrpg_lib.compat.player_animator.api;

public record MobAnimationOptions(
        MobAnimationLayer layer,
        float speed,
        int fadeTicks,
        Mirror mirror,
        boolean persistent,
        float length,
        float upswingRate,
        float upswingMultiplier) {

    public enum Mirror { AUTO, NEVER, ALWAYS }

    public enum PoseStyle { PLAIN, WEAPON, WEAPON_TWO_HANDED }

    public static final MobAnimationOptions DEFAULT = new MobAnimationOptions(MobAnimationLayer.MISC, 1.0F, -1, Mirror.AUTO, false, 0.0F, 0.0F, 0.0F);

    public MobAnimationOptions(MobAnimationLayer layer, float speed, int fadeTicks, Mirror mirror, boolean persistent) {
        this(layer, speed, fadeTicks, mirror, persistent, 0.0F, 0.0F, 0.0F);
    }

    public MobAnimationOptions(MobAnimationLayer layer, float speed, int fadeTicks, Mirror mirror, boolean persistent, int durationTicks) {
        this(layer, speed, fadeTicks, mirror, persistent, Math.max(0, durationTicks), 0.0F, 0.0F);
    }

    public int durationTicks() {
        return Math.round(length);
    }

    public MobAnimationOptions withLayer(MobAnimationLayer layer) {
        return new MobAnimationOptions(layer, speed, fadeTicks, mirror, persistent, length, upswingRate, upswingMultiplier);
    }

    public MobAnimationOptions withSpeed(float speed) {
        return new MobAnimationOptions(layer, speed, fadeTicks, mirror, persistent, length, upswingRate, upswingMultiplier);
    }

    public MobAnimationOptions withFadeTicks(int fadeTicks) {
        return new MobAnimationOptions(layer, speed, fadeTicks, mirror, persistent, length, upswingRate, upswingMultiplier);
    }

    public MobAnimationOptions withMirror(Mirror mirror) {
        return new MobAnimationOptions(layer, speed, fadeTicks, mirror, persistent, length, upswingRate, upswingMultiplier);
    }

    public MobAnimationOptions withPersistent(boolean persistent) {
        return new MobAnimationOptions(layer, speed, fadeTicks, mirror, persistent, length, upswingRate, upswingMultiplier);
    }

    public MobAnimationOptions withDuration(int durationTicks) {
        return withDuration((float) durationTicks);
    }

    public MobAnimationOptions withDuration(float length) {
        return new MobAnimationOptions(layer, speed, fadeTicks, mirror, persistent, sanitize(length), 0.0F, 0.0F);
    }

    public MobAnimationOptions withAttackTiming(float length, float upswingRate, float upswingMultiplier) {
        return new MobAnimationOptions(layer, speed, fadeTicks, mirror, persistent, sanitize(length), sanitize(upswingRate), sanitize(upswingMultiplier));
    }

    private static float sanitize(float value) {
        return Float.isFinite(value) ? Math.max(0.0F, value) : 0.0F;
    }
}
