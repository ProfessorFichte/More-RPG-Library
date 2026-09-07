package net.more_rpg_classes.compat;

import net.critical_strike.internal.CriticalStriker;
import net.spell_engine.Platform;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import static net.more_rpg_classes.custom.MoreSpellSchools.*;

public class CriticalStrikeCompat {

    public static void init() {
        if (Platform.util().isModLoaded("critical_strike")) {

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
}
