package com.mrpg_lib.compat.critical_strike;

import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.internal.CriticalStriker;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import static com.mrpg_lib.compat.spell_power.MoreSpellSchools.*;

public class CriticalStrikeCompat {

    public static void configureSchoolAttributes() {
        FROST_RANGED.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
            var value = query.entity().getAttributeValue(CriticalStrikeAttributes.CHANCE.attributeEntry);
            return (double) CriticalStrikeAttributes.CHANCE.asChance(value);
        });
        FROST_RANGED.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
            var value = query.entity().getAttributeValue(CriticalStrikeAttributes.DAMAGE.attributeEntry);
            return CriticalStrikeAttributes.DAMAGE.asMultiplier(value) - 1;
        });
        FIRE_RANGED.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
            var value = query.entity().getAttributeValue(CriticalStrikeAttributes.CHANCE.attributeEntry);
            return (double) CriticalStrikeAttributes.CHANCE.asChance(value);
        });
        FIRE_RANGED.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
            var value = query.entity().getAttributeValue(CriticalStrikeAttributes.DAMAGE.attributeEntry);
            return CriticalStrikeAttributes.DAMAGE.asMultiplier(value) - 1;
        });
        RAGE_MELEE.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
            var value = query.entity().getAttributeValue(CriticalStrikeAttributes.CHANCE.attributeEntry);
            return (double) CriticalStrikeAttributes.CHANCE.asChance(value);
        });
        RAGE_MELEE.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
            var value = query.entity().getAttributeValue(CriticalStrikeAttributes.DAMAGE.attributeEntry);
            return CriticalStrikeAttributes.DAMAGE.asMultiplier(value) - 1;
        });
        SpellSchools.configureSpellCritDamage(FROST_RANGED);
        SpellSchools.configureSpellCritChance(FROST_RANGED);
        SpellSchools.configureSpellCritDamage(FIRE_RANGED);
        SpellSchools.configureSpellCritChance(FIRE_RANGED);
        SpellSchools.configureSpellCritDamage(RAGE_MELEE);
        SpellSchools.configureSpellCritChance(RAGE_MELEE);
    }

    public static void init() {
        FROST_RANGED.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
            if (query.entity() instanceof CriticalStriker criticalStriker) {
                return criticalStriker.rng_criticalChance();
            }
            return 0.0;
        });
        FROST_RANGED.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
            if (query.entity() instanceof CriticalStriker criticalStriker) {
                return criticalStriker.rng_criticalDamageMultiplier() - 1;
            }
            return 0.0;
        });
        FIRE_RANGED.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
            if (query.entity() instanceof CriticalStriker criticalStriker) {
                return criticalStriker.rng_criticalChance();
            }
            return 0.0;
        });
        FIRE_RANGED.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
            if (query.entity() instanceof CriticalStriker criticalStriker) {
                return criticalStriker.rng_criticalDamageMultiplier() - 1;
            }
            return 0.0;
        });
        RAGE_MELEE.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
            if (query.entity() instanceof CriticalStriker criticalStriker) {
                return criticalStriker.rng_criticalChance();
            }
            return 0.0;
        });
        RAGE_MELEE.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
            if (query.entity() instanceof CriticalStriker criticalStriker) {
                return criticalStriker.rng_criticalDamageMultiplier() - 1;
            }
            return 0.0;
        });
        SpellSchools.configureSpellCritDamage(FROST_RANGED);
        SpellSchools.configureSpellCritChance(FROST_RANGED);
        SpellSchools.configureSpellCritDamage(FIRE_RANGED);
        SpellSchools.configureSpellCritChance(FIRE_RANGED);
        SpellSchools.configureSpellCritDamage(RAGE_MELEE);
        SpellSchools.configureSpellCritChance(RAGE_MELEE);
    }
}
