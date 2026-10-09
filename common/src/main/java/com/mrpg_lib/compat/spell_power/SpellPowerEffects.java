package com.mrpg_lib.compat.spell_power;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;
import net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect;

public final class SpellPowerEffects {
    private SpellPowerEffects() {
    }

    public static StatusEffect frozenSolid(StatusEffectCategory category, int color) {
        return new SpellPowerFrozenSolidEffect(category, color)
                .setVulnerability(SpellSchools.FROST, new SpellPower.Vulnerability(0, 0.1F, 0.2F));
    }

    public static StatusEffect soaked(StatusEffectCategory category, int color) {
        return new SpellPowerSoakedEffect(category, color)
                .setVulnerability(SpellSchools.LIGHTNING, new SpellPower.Vulnerability(0.15F, 0.1F, 0))
                .setVulnerability(SpellSchools.FROST, new SpellPower.Vulnerability(0.15F, 0, 0.3F));
    }

    public static StatusEffect arcanePrecision(StatusEffectCategory category) {
        return new SpellVulnerabilityStatusEffect(category, SpellSchools.ARCANE.color)
                .setVulnerability(SpellSchools.ARCANE, new SpellPower.Vulnerability(0.025F, 0.05F, 0.1F));
    }
}
