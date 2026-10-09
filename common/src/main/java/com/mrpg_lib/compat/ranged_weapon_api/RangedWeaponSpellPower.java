package com.mrpg_lib.compat.ranged_weapon_api;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import static com.mrpg_lib.compat.spell_power.MoreSpellSchools.*;

public final class RangedWeaponSpellPower {
    private RangedWeaponSpellPower() {
    }

    public static void configureHaste() {
        FIRE_RANGED.addSource(SpellSchool.Trait.HASTE, SpellSchool.Apply.ADD, query -> {
            var haste = query.entity().getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
            var rate = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste);
            return rate - 1;
        });
        FROST_RANGED.addSource(SpellSchool.Trait.HASTE, SpellSchool.Apply.ADD, query -> {
            var haste = query.entity().getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
            var rate = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste);
            return rate - 1;
        });
        SpellSchools.configureSpellHaste(FROST_RANGED);
        SpellSchools.configureSpellHaste(FIRE_RANGED);
    }
}
