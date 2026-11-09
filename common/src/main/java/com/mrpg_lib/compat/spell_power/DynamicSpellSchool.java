package com.mrpg_lib.compat.spell_power;

import com.mrpg_lib.compat.MrpgCompat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.util.Identifier;
import net.spell_power.SpellPowerMod;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.ArrayList;
import java.util.List;

public class DynamicSpellSchool {
    public static final Identifier ID = Identifier.of(SpellPowerMod.ID, "dynamic");
    private static final String PHYSICAL_MELEE = SpellPowerMod.ID + ":physical_melee";
    private static final String PHYSICAL_RANGED = SpellPowerMod.ID + ":physical_ranged";

    public static final SpellSchool DYNAMIC = new SpellSchool(SpellSchool.Archetype.MELEE, ID, 0xE6C35C, DamageTypes.PLAYER_ATTACK, EntityAttributes.GENERIC_ATTACK_DAMAGE);
    private static List<SpellSchool> candidates;

    static void create() {
        DYNAMIC.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD,
                query -> SpellPower.getSpellPower(resolve(query.entity()), query.entity()).nonCriticalValue());
        DYNAMIC.addSource(SpellSchool.Trait.HASTE, SpellSchool.Apply.ADD,
                query -> resolve(query.entity()).getValue(SpellSchool.Trait.HASTE, query) - 1.0);
        DYNAMIC.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD,
                query -> resolve(query.entity()).getValue(SpellSchool.Trait.CRIT_CHANCE, query));
        DYNAMIC.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD,
                query -> resolve(query.entity()).getValue(SpellSchool.Trait.CRIT_DAMAGE, query) - 1.0);
        SpellSchools.register(DYNAMIC);
    }

    public static SpellSchool resolve(LivingEntity entity) {
        SpellSchool best = null;
        double bestPower = Double.NEGATIVE_INFINITY;
        for (var school : candidates()) {
            double power = SpellPower.getSpellPower(school, entity).nonCriticalValue();
            if (power > bestPower) {
                best = school;
                bestPower = power;
            }
        }
        return best != null ? best : SpellSchools.GENERIC;
    }

    private static synchronized List<SpellSchool> candidates() {
        if (candidates != null) return candidates;
        var list = new ArrayList<SpellSchool>();
        var melee = SpellSchools.getSchool(PHYSICAL_MELEE);
        if (melee != null) list.add(melee);
        var ranged = SpellSchools.getSchool(PHYSICAL_RANGED);
        if (ranged != null && MrpgCompat.RANGED_WEAPON_API) list.add(ranged);
        for (var school : SpellSchools.all()) {
            if (school.isMagicArchetype() && school != SpellSchools.GENERIC && school != SpellSchools.HEALING) {
                list.add(school);
            }
        }
        candidates = List.copyOf(list);
        return candidates;
    }
}
