package net.more_rpg_classes.custom;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.config.WeaknessConfig;
import net.more_rpg_classes.util.tags.MRPGCEntityTags;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.weakness.ScopedWeakness;
import net.spell_engine.api.tags.SpellEngineEntityTags;
import net.spell_power.SpellPowerMod;
import net.spell_power.api.SpellSchool;

import java.util.List;

public class MoreSpellSchoolWeakness {
    public static List<ScopedWeakness> getWeaknesses(Identifier schoolId) {
        if (schoolId == null) {
            return List.of();
        }
        var config = MRPGCMod.weaknessConfig.value;
        if (config == null || config.school_weaknesses == null) {
            return List.of();
        }
        var key = schoolId.toString();
        return config.school_weaknesses.getOrDefault(key, List.of());
    }

    public static List<ScopedWeakness> getWeaknesses(@Nullable SpellSchool school) {
        if (school == null) {
            return List.of();
        }
        return getWeaknesses(school.id);
    }

    public static WeaknessConfig createDefault() {
        var config = new WeaknessConfig();

        var waterWeakness = new Spell.Impact.TargetModifier();
        var waterCondition = new Spell.TargetCondition();

        waterCondition.entity_type = "#" + MRPGCEntityTags.WEAK_TO_WATER;
        waterWeakness.conditions = List.of(waterCondition);
        waterWeakness.modifier = new Spell.Impact.Modifier();
        waterWeakness.modifier.critical_chance_bonus = 1.0f;
        config.school_weaknesses.put(MoreSpellSchools.WATER.id.toString(), List.of(
                new ScopedWeakness(Spell.Impact.Action.Type.DAMAGE, waterWeakness)
        ));

        var waterResistance = new Spell.Impact.TargetModifier();
        var waterConditionResistance = new Spell.TargetCondition();

        waterConditionResistance.entity_type = "#" + MRPGCEntityTags.RESISTANT_TO_WATER;
        waterResistance.conditions = List.of(waterConditionResistance);
        waterResistance.modifier = new Spell.Impact.Modifier();
        waterResistance.modifier.power_multiplier = -0.3f;
        config.school_weaknesses.put(MoreSpellSchools.WATER.id.toString(), List.of(
                new ScopedWeakness(Spell.Impact.Action.Type.DAMAGE, waterResistance)
        ));

        var earthWeakness = new Spell.Impact.TargetModifier();
        var earthCondition = new Spell.TargetCondition();

        earthCondition.entity_type = "#" + MRPGCEntityTags.WEAK_TO_EARTH;
        earthWeakness.conditions = List.of(earthCondition);
        earthWeakness.modifier = new Spell.Impact.Modifier();
        earthWeakness.modifier.critical_chance_bonus = 0.3f;
        config.school_weaknesses.put(MoreSpellSchools.EARTH.id.toString(), List.of(
                new ScopedWeakness(Spell.Impact.Action.Type.DAMAGE, earthWeakness)
        ));

        var fireWeakness = new Spell.Impact.TargetModifier();
        var fireCondition = new Spell.TargetCondition();

        fireCondition.entity_type = "#" + SpellEngineEntityTags.Vulnerability.WEAK_TO_FIRE.id();
        fireWeakness.conditions = List.of(fireCondition);
        fireWeakness.modifier = new Spell.Impact.Modifier();
        fireWeakness.modifier.critical_chance_bonus = 0.3f;
        config.school_weaknesses.put(Identifier.of(SpellPowerMod.ID, "fire_ranged").toString(), List.of(
                new ScopedWeakness(Spell.Impact.Action.Type.DAMAGE, fireWeakness)
        ));

        var frostWeakness = new Spell.Impact.TargetModifier();
        var frostWeaknessCondition = new Spell.TargetCondition();
        frostWeaknessCondition.entity_type = "#" + SpellEngineEntityTags.Vulnerability.WEAK_TO_FROST.id();
        frostWeakness.conditions = List.of(frostWeaknessCondition);
        frostWeakness.modifier = new Spell.Impact.Modifier();
        frostWeakness.modifier.power_multiplier = 0.3f;

        var frostResistance = new Spell.Impact.TargetModifier();
        var frostResistanceCondition = new Spell.TargetCondition();
        frostResistanceCondition.entity_type = "#" + SpellEngineEntityTags.Vulnerability.RESISTANT_TO_FROST.id();
        frostResistance.conditions = List.of(frostResistanceCondition);
        frostResistance.modifier = new Spell.Impact.Modifier();
        frostResistance.modifier.power_multiplier = -0.3f;
        config.school_weaknesses.put(Identifier.of(SpellPowerMod.ID, "frost_ranged").toString(), List.of(
                new ScopedWeakness(null, frostWeakness),
                new ScopedWeakness(null, frostResistance)
        ));

        return config;
    }
}
