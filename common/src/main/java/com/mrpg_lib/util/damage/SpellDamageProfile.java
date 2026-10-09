package com.mrpg_lib.util.damage;

import net.minecraft.util.Identifier;

public record SpellDamageProfile(
        Identifier school,
        double spellPowerWeight,
        double attackWeightWithSpellPower,
        double attackWeightWithoutSpellPower,
        double multiplier,
        double flat,
        double minDamage
) {
    public static final double DEFAULT_SPELL_POWER_WEIGHT = 0.75;
    public static final double DEFAULT_ATTACK_WEIGHT_WITH_SPELL_POWER = 0.25;
    public static final double DEFAULT_ATTACK_WEIGHT_WITHOUT_SPELL_POWER = 1.0;

    public static SpellDamageProfile of(Identifier school, double multiplier, double minDamage) {
        return new SpellDamageProfile(school, DEFAULT_SPELL_POWER_WEIGHT, DEFAULT_ATTACK_WEIGHT_WITH_SPELL_POWER,
                DEFAULT_ATTACK_WEIGHT_WITHOUT_SPELL_POWER, multiplier, 0.0, minDamage);
    }

    public SpellDamageProfile withWeights(double spellPowerWeight, double attackWeightWithSpellPower) {
        return new SpellDamageProfile(school, spellPowerWeight, attackWeightWithSpellPower,
                attackWeightWithoutSpellPower, multiplier, flat, minDamage);
    }

    public SpellDamageProfile withAttackWeightWithoutSpellPower(double attackWeightWithoutSpellPower) {
        return new SpellDamageProfile(school, spellPowerWeight, attackWeightWithSpellPower,
                attackWeightWithoutSpellPower, multiplier, flat, minDamage);
    }

    public SpellDamageProfile withFlat(double flat) {
        return new SpellDamageProfile(school, spellPowerWeight, attackWeightWithSpellPower,
                attackWeightWithoutSpellPower, multiplier, flat, minDamage);
    }

    public SpellDamageProfile withSchool(Identifier school) {
        return new SpellDamageProfile(school, spellPowerWeight, attackWeightWithSpellPower,
                attackWeightWithoutSpellPower, multiplier, flat, minDamage);
    }
}
