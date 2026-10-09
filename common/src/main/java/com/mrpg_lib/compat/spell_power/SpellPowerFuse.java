package com.mrpg_lib.compat.spell_power;

import com.mrpg_lib.compat.CompatHooks;
import com.mrpg_lib.entity.attribute.MRPGCEntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

public final class SpellPowerFuse {
    private SpellPowerFuse() {
    }

    public static void apply(LivingEntity attacker, LivingEntity target, boolean particles) {
        applyFuse(attacker, target, MRPGCEntityAttributes.AIR_FUSE_MODIFIER, MoreSpellSchools.AIR, particles);
        applyFuse(attacker, target, MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER, SpellSchools.ARCANE, particles);
        applyFuse(attacker, target, MRPGCEntityAttributes.EARTH_FUSE_MODIFIER, MoreSpellSchools.EARTH, particles);
        applyFuse(attacker, target, MRPGCEntityAttributes.FIRE_FUSE_MODIFIER, SpellSchools.FIRE, particles);
        applyFuse(attacker, target, MRPGCEntityAttributes.FROST_FUSE_MODIFIER, SpellSchools.FROST, particles);
        applyFuse(attacker, target, MRPGCEntityAttributes.HEALING_FUSE_MODIFIER, SpellSchools.HEALING, particles);
        applyFuse(attacker, target, MRPGCEntityAttributes.WATER_FUSE_MODIFIER, MoreSpellSchools.WATER, particles);
    }

    private static void applyFuse(LivingEntity attacker, LivingEntity target, RegistryEntry<EntityAttribute> fuseAttribute, SpellSchool spellSchool, boolean particles) {
        EntityAttributeInstance fuseInstance = attacker.getAttributeInstance(fuseAttribute);
        if (fuseInstance == null || fuseInstance.getValue() == 100.0) return;
        EntityAttributeInstance spellPowerInstance = attacker.getAttributeInstance(spellSchool.attributeEntry);
        if (spellPowerInstance == null) return;
        float magicDamage = Math.max(0.1f, (float)((fuseInstance.getValue() - 100) / 100f) * (float) spellPowerInstance.getValue());
        target.timeUntilRegen = 0;
        target.damage(SpellDamageSource.create(spellSchool, attacker), magicDamage);
        if (particles) {
            CompatHooks.fx().fuse(target, spellSchool.color);
        }
    }
}
