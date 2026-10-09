package com.mrpg_lib.compat.spell_power;

import com.mrpg_lib.effect.FrozenSolidEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect;

public class SpellPowerFrozenSolidEffect extends SpellVulnerabilityStatusEffect {
    public SpellPowerFrozenSolidEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public void onApplied(LivingEntity livingEntity, int amplifier) {
        super.onApplied(livingEntity, amplifier);
        FrozenSolidEffect.handleApplied(livingEntity);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity livingEntity, int amplifier) {
        return FrozenSolidEffect.handleUpdate(livingEntity);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
