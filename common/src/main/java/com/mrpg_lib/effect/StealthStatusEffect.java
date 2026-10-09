package com.mrpg_lib.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public abstract class StealthStatusEffect extends StatusEffect {
    public interface Fx {
        void playSound(LivingEntity entity, Identifier soundId);

        void popParticles(LivingEntity entity, Object particleGroup);
    }

    private static Fx fx = new Fx() {
        @Override
        public void playSound(LivingEntity entity, Identifier soundId) {
            entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvent.of(soundId), SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        @Override
        public void popParticles(LivingEntity entity, Object particleGroup) {
        }
    };

    public static void setFx(Fx fx) {
        StealthStatusEffect.fx = fx;
    }

    private static Predicate<LivingEntity> activeCheck = entity -> {
        for (var instance : entity.getStatusEffects()) {
            if (instance.getEffectType().value() instanceof StealthStatusEffect) {
                return true;
            }
        }
        return false;
    };

    public static void setActiveCheck(Predicate<LivingEntity> activeCheck) {
        StealthStatusEffect.activeCheck = activeCheck;
    }

    public static boolean isStealthActive(LivingEntity entity) {
        return activeCheck.test(entity);
    }

    protected StealthStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public Object stealthPopParticles() {
        return null;
    }

    public Identifier stealthLeaveSoundId() {
        return null;
    }

    public abstract double stealthFollowRange();

    public abstract double stealthVisibilityMultiplier();

    public void onStealthRemoved(LivingEntity entity) {
        if (entity.getWorld().isClient()) return;
        var soundId = stealthLeaveSoundId();
        if (soundId != null) {
            fx.playSound(entity, soundId);
        }
        var particles = stealthPopParticles();
        if (particles != null) {
            fx.popParticles(entity, particles);
        }
    }
}
