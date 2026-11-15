package com.mrpg_lib.compat.critical_strike;

import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.internal.CriticalStriker;
import net.minecraft.entity.LivingEntity;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.List;

import static com.mrpg_lib.compat.spell_power.MoreSpellSchools.*;

public class CriticalStrikeCompat {

    private static List<SpellSchool> schools() {
        return List.of(FROST_RANGED, FIRE_RANGED, RAGE_MELEE);
    }

    // Mobs with their own attribute builder don't have the crit attributes, asking for them anyway throws.
    private static boolean hasCritAttributes(LivingEntity entity) {
        return entity.getAttributes().hasAttribute(CriticalStrikeAttributes.CHANCE.attributeEntry)
                && entity.getAttributes().hasAttribute(CriticalStrikeAttributes.DAMAGE.attributeEntry);
    }

    public static void configureSchoolAttributes() {
        for (SpellSchool school : schools()) {
            school.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query -> {
                if (!hasCritAttributes(query.entity())) return 0.0;
                var value = query.entity().getAttributeValue(CriticalStrikeAttributes.CHANCE.attributeEntry);
                return (double) CriticalStrikeAttributes.CHANCE.asChance(value);
            });
            school.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
                if (!hasCritAttributes(query.entity())) return 0.0;
                var value = query.entity().getAttributeValue(CriticalStrikeAttributes.DAMAGE.attributeEntry);
                return CriticalStrikeAttributes.DAMAGE.asMultiplier(value) - 1;
            });
            SpellSchools.configureSpellCritDamage(school);
            SpellSchools.configureSpellCritChance(school);
        }
    }

    public static void init() {
        for (SpellSchool school : schools()) {
            school.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query -> {
                if (query.entity() instanceof CriticalStriker criticalStriker) {
                    return criticalStriker.rng_criticalChance();
                }
                return 0.0;
            });
            school.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
                if (query.entity() instanceof CriticalStriker criticalStriker) {
                    return criticalStriker.rng_criticalDamageMultiplier() - 1;
                }
                return 0.0;
            });
            SpellSchools.configureSpellCritDamage(school);
            SpellSchools.configureSpellCritChance(school);
        }
    }
}
