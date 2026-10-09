package com.mrpg_lib.compat.spell_power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.util.Identifier;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.HashMap;
import java.util.Map;

final class SpellPowerSchoolLookup {
    private static final Map<Identifier, SpellSchool> SCHOOLS = new HashMap<>();

    private SpellPowerSchoolLookup() {
    }

    static double getSpellPower(LivingEntity owner, Identifier schoolId) {
        SpellSchool school = SCHOOLS.computeIfAbsent(schoolId, SpellPowerSchoolLookup::findSchool);
        if (school == null) {
            return 0.0;
        }
        EntityAttributeInstance instance = owner.getAttributeInstance(school.attributeEntry);
        return instance == null ? 0.0 : instance.getValue();
    }

    private static SpellSchool findSchool(Identifier schoolId) {
        for (SpellSchool school : SpellSchools.all()) {
            if (school.id.equals(schoolId)) {
                return school;
            }
        }
        return null;
    }
}
