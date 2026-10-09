package com.mrpg_lib.compat.ranged_weapon_api;

import com.mrpg_lib.compat.CompatHooks;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

public final class RangedWeaponCompat {
    private RangedWeaponCompat() {
    }

    public static void init() {
        CompatHooks.setRangedDamageAttribute(RangedWeaponCompat::damageAttribute);
    }

    public static RegistryEntry<EntityAttribute> damageAttribute() {
        return EntityAttributes_RangedWeapon.DAMAGE.entry;
    }
}
