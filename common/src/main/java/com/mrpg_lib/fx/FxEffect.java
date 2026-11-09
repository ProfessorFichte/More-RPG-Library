package com.mrpg_lib.fx;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FxEffect {
    public record Child(FxEffect effect, int delayTicks) {}

    @Nullable private Identifier id;
    private final List<FxLayer> layers;
    private final List<FxSound> sounds;
    private final List<Child> children;
    private final float defaultRadius;
    private final float defaultLength;
    private final int durationTicks;
    private final int intervalTicks;
    private final boolean follow;
    private final int maxParticles;

    private FxEffect(Builder builder) {
        this.layers = List.copyOf(builder.layers);
        this.sounds = List.copyOf(builder.sounds);
        this.children = List.copyOf(builder.children);
        this.defaultRadius = builder.defaultRadius;
        this.defaultLength = builder.defaultLength;
        this.durationTicks = builder.durationTicks;
        this.intervalTicks = Math.max(1, builder.intervalTicks);
        this.follow = builder.follow;
        this.maxParticles = builder.maxParticles;
    }

    public static Builder builder() {
        return new Builder();
    }

    void assignId(Identifier id) {
        this.id = id;
    }

    @Nullable public Identifier id() { return id; }
    public List<FxLayer> layers() { return layers; }
    public List<FxSound> sounds() { return sounds; }
    public List<Child> children() { return children; }
    public float defaultRadius() { return defaultRadius; }
    public float defaultLength() { return defaultLength; }
    public int durationTicks() { return durationTicks; }
    public int intervalTicks() { return intervalTicks; }
    public boolean follow() { return follow; }
    public int maxParticles() { return maxParticles; }
    public boolean timed() { return durationTicks > 0; }

    public static final class Builder {
        private final List<FxLayer> layers = new ArrayList<>();
        private final List<FxSound> sounds = new ArrayList<>();
        private final List<Child> children = new ArrayList<>();
        private float defaultRadius = 1.0F;
        private float defaultLength = 1.0F;
        private int durationTicks = 0;
        private int intervalTicks = 1;
        private boolean follow = false;
        private int maxParticles = 256;

        private Builder() {
        }

        public Builder layer(FxLayer layer) {
            layers.add(layer);
            return this;
        }

        public Builder sound(FxSound sound) {
            sounds.add(sound);
            return this;
        }

        public Builder sound(SoundEvent sound, float volume, float pitch) {
            return sound(FxSound.of(sound, volume, pitch));
        }

        public Builder child(FxEffect effect, int delayTicks) {
            children.add(new Child(effect, delayTicks));
            return this;
        }

        public Builder radius(float radius) {
            this.defaultRadius = radius;
            return this;
        }

        public Builder length(float length) {
            this.defaultLength = length;
            return this;
        }

        public Builder duration(int ticks) {
            this.durationTicks = ticks;
            return this;
        }

        public Builder interval(int ticks) {
            this.intervalTicks = ticks;
            return this;
        }

        public Builder follow() {
            this.follow = true;
            return this;
        }

        public Builder maxParticles(int max) {
            this.maxParticles = max;
            return this;
        }

        public FxEffect build() {
            return new FxEffect(this);
        }
    }
}
