package com.mrpg_lib.compat.spell_power;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.spell_engine.particle.SoakedMist;
import com.mrpg_lib.effect.SoakedEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect;

public class SpellPowerSoakedEffect extends SpellVulnerabilityStatusEffect {
    public SpellPowerSoakedEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity livingEntity, int amplifier) {
        return SoakedEffect.handleUpdate(livingEntity, MrpgCompat.SPELL_ENGINE ? SoakedMist.type() : ParticleTypes.CLOUD);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
