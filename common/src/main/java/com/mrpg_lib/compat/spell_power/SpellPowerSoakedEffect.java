package com.mrpg_lib.compat.spell_power;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.spell_engine.particle.SoakedMist;
import com.mrpg_lib.effect.SoakedEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.statuseffects.VulnerabilityEffect;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpellPowerSoakedEffect extends StatusEffect implements VulnerabilityEffect {
    private final Map<SpellSchool, SpellPower.Vulnerability> vulnerabilities = new LinkedHashMap<>();

    public SpellPowerSoakedEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public SpellPowerSoakedEffect addVulnerability(SpellSchool school, SpellPower.Vulnerability vulnerability) {
        vulnerabilities.put(school, vulnerability);
        return this;
    }

    @Override
    public SpellPower.Vulnerability getVulnerability(SpellSchool school, int amplifier) {
        SpellPower.Vulnerability vulnerability = vulnerabilities.get(school);
        return vulnerability != null ? vulnerability.multiply(amplifier + 1) : SpellPower.Vulnerability.none;
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
