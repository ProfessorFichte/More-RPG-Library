package com.mrpg_lib.util.damage;

import com.mrpg_lib.compat.CompatHooks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import org.jetbrains.annotations.Nullable;

public final class SpellDamage {
    private SpellDamage() {
    }

    public static float calculate(@Nullable LivingEntity owner, SpellDamageProfile profile) {
        if (owner == null) {
            return (float) Math.max(profile.minDamage(), profile.flat());
        }

        EntityAttributeInstance attackInstance = owner.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double attack = attackInstance == null ? 0.0 : attackInstance.getValue();
        double spellPower = CompatHooks.schoolPower(owner, profile.school());

        double base = spellPower > 0.0
                ? profile.spellPowerWeight() * spellPower + profile.attackWeightWithSpellPower() * attack
                : profile.attackWeightWithoutSpellPower() * attack;

        return (float) Math.max(profile.minDamage(), profile.flat() + profile.multiplier() * base);
    }
}
