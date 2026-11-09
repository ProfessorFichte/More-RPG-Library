package com.mrpg_lib.fx;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FxTweaks {
    float radius = 1.0F;
    float height = 1.0F;
    float length = 1.0F;
    float width = 0.15F;
    int color = -1;
    int color2 = -1;
    float speed = 0.1F;
    float count = 1.0F;
    int duration = 0;
    float turns = 2.0F;
    float angle = 120.0F;
    boolean inward = false;
    @Nullable ParticleEffect particle;
    @Nullable ParticleEffect secondaryParticle;
    @Nullable String spellEngineParticle;
    @Nullable String secondarySpellEngineParticle;
    @Nullable FxSound sound;
    final List<FxSound> extraSounds = new ArrayList<>();
    boolean silent = false;

    FxTweaks() {
    }

    public FxTweaks radius(float radius) {
        this.radius = radius;
        return this;
    }

    public FxTweaks height(float height) {
        this.height = height;
        return this;
    }

    public FxTweaks length(float length) {
        this.length = length;
        return this;
    }

    public FxTweaks width(float width) {
        this.width = width;
        return this;
    }

    public FxTweaks color(int rgb) {
        this.color = rgb & 0xFFFFFF;
        return this;
    }

    public FxTweaks color(int rgb, int secondaryRgb) {
        this.color = rgb & 0xFFFFFF;
        this.color2 = secondaryRgb & 0xFFFFFF;
        return this;
    }

    public FxTweaks speed(float speed) {
        this.speed = speed;
        return this;
    }

    public FxTweaks count(float multiplier) {
        this.count = multiplier;
        return this;
    }

    public FxTweaks duration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public FxTweaks turns(float turns) {
        this.turns = turns;
        return this;
    }

    public FxTweaks angle(float degrees) {
        this.angle = degrees;
        return this;
    }

    public FxTweaks inward() {
        this.inward = true;
        return this;
    }

    public FxTweaks vanillaParticle(ParticleEffect particle) {
        this.particle = particle;
        return this;
    }

    public FxTweaks secondaryVanillaParticle(ParticleEffect particle) {
        this.secondaryParticle = particle;
        return this;
    }

    public FxTweaks spellEngineParticle(String id) {
        this.spellEngineParticle = id;
        return this;
    }

    public FxTweaks secondarySpellEngineParticle(String id) {
        this.secondarySpellEngineParticle = id;
        return this;
    }

    public FxTweaks sound(SoundEvent sound, float volume, float pitch) {
        this.sound = FxSound.of(sound, volume, pitch);
        this.silent = false;
        return this;
    }

    public FxTweaks sound(FxSound sound) {
        this.sound = sound;
        this.silent = false;
        return this;
    }

    public FxTweaks addSound(FxSound sound) {
        this.extraSounds.add(sound);
        return this;
    }

    public FxTweaks silent() {
        this.silent = true;
        return this;
    }

    public float radius() { return radius; }
    public float height() { return height; }
    public float length() { return length; }
    public float width() { return width; }
    public int color() { return color; }
    public int color2() { return color2; }
    public float speed() { return speed; }
    public float count() { return count; }
    public int duration() { return duration; }
    public float turns() { return turns; }
    public float angle() { return angle; }
    public boolean isInward() { return inward; }
}
