package com.mrpg_lib.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;

public class ExclusiveStatusEffect extends StatusEffect {
    public ExclusiveStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        for (StatusEffectInstance instance : new ArrayList<>(entity.getStatusEffects())) {
            if (instance.getEffectType().value() instanceof ExclusiveStatusEffect && instance.getEffectType().value() != this) {
                entity.removeStatusEffect(instance.getEffectType());
            }
        }
    }
}
