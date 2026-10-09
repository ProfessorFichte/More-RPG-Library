package com.mrpg_lib.compat.spell_power;

import net.minecraft.entity.LivingEntity;
import net.spell_power.api.SpellSchools;

public class SpellPowerMethods {
    public static double getHighestSpellSchoolPower(LivingEntity entity) {
        double maxPower = 0.0;
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.ARCANE.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.FIRE.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.FROST.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.HEALING.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.LIGHTNING.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.SOUL.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.EARTH.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.WATER.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.AIR.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.NATURE.attributeEntry));

        return maxPower;
    }
}
