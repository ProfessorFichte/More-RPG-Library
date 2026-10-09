package com.mrpg_lib.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class IgnitedEffect extends StatusEffect {
    public IgnitedEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        float baseDamage = 1.0F;
        float scaledDamage = baseDamage + (amplifier * 0.1F);
        entity.timeUntilRegen = 0;
        entity.damage(entity.getDamageSources().onFire(), scaledDamage);
        return true;
    }
}
