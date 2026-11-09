package com.mrpg_lib.fx;

import net.minecraft.sound.SoundEvent;

public record FxSound(SoundEvent sound, float volume, float pitch, float pitchVariance, int delayTicks, int repeatTicks) {
    public static FxSound of(SoundEvent sound, float volume, float pitch) {
        return new FxSound(sound, volume, pitch, 0.0F, 0, 0);
    }

    public FxSound pitchVariance(float variance) {
        return new FxSound(sound, volume, pitch, variance, delayTicks, repeatTicks);
    }

    public FxSound delay(int ticks) {
        return new FxSound(sound, volume, pitch, pitchVariance, ticks, repeatTicks);
    }

    public FxSound repeat(int ticks) {
        return new FxSound(sound, volume, pitch, pitchVariance, delayTicks, ticks);
    }
}
