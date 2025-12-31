package net.more_rpg_classes.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.attribute.ProjectileAttributeData;
import net.spell_engine.api.effect.SpellEngineEffects;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.Random;

public class ProjectileEffectApplicator {

    private static final Random RANDOM = new Random();

    public static void applyEffects(
        ProjectileAttributeData data,
        LivingEntity target,
        Entity shooter,
        World world
    ) {
        if (world.isClient()) {
            return;
        }

        // Apply all fuse magic damage
        applyFuseDamage(data.airFuse, data.airPower, target, shooter, MoreSpellSchools.AIR);
        applyFuseDamage(data.arcaneFuse, data.arcanePower, target, shooter, SpellSchools.ARCANE);
        applyFuseDamage(data.earthFuse, data.earthPower, target, shooter, MoreSpellSchools.EARTH);
        applyFuseDamage(data.fireFuse, data.firePower, target, shooter, SpellSchools.FIRE);
        applyFuseDamage(data.frostFuse, data.frostPower, target, shooter, SpellSchools.FROST);
        applyFuseDamage(data.healingFuse, data.healingPower, target, shooter, SpellSchools.HEALING);
        applyFuseDamage(data.waterFuse, data.waterPower, target, shooter, MoreSpellSchools.WATER);
        // Apply all chance-based effects
        int amplifier = (int)(data.attackDamage * 0.15);
        applyChanceBasedEffects(data, target, amplifier);
    }

    private static void applyFuseDamage(
        float fuseModifier,
        float spellPower,
        LivingEntity target,
        Entity shooter,
        SpellSchool spellSchool
    ) {
        if (fuseModifier != 100.0f && shooter instanceof LivingEntity livingShooter) {
            float fuseBonus = (fuseModifier - 100) / 100f;
            float magicDamage = Math.max(0.1f, fuseBonus * spellPower);
            target.timeUntilRegen = 0;
            target.damage(SpellDamageSource.create(spellSchool, livingShooter), magicDamage);
        }
    }

    private static void applyChanceBasedEffects(
        ProjectileAttributeData data,
        LivingEntity target,
        int amplifier
    ) {
        // 1. Burning Chance
        if (data.burningChance > 100.0f) {
            float chance = (data.burningChance - 100) / 100f;
            if (RANDOM.nextFloat() < chance) {
                target.addStatusEffect(new StatusEffectInstance(
                    MRPGCEffects.IGNITED.entry, 40, amplifier, true, false, true));
            }
        }

        // 2. Stagger Chance
        if (data.staggerChance > 100.0f) {
            float chance = (data.staggerChance - 100) / 100f;
            if (RANDOM.nextFloat() < chance) {
                target.addStatusEffect(new StatusEffectInstance(
                    MRPGCEffects.STAGGER.entry, 80, amplifier, true, false, true));
            }
        }

        // 3. Stun Chance
        if (data.stunChance > 100.0f) {
            float chance = (data.stunChance - 100) / 100f;
            if (RANDOM.nextFloat() < chance) {
                target.addStatusEffect(new StatusEffectInstance(
                    SpellEngineEffects.STUN.entry, 40, 0, true, false, true));
            }
        }

        // 4. Poison Chance
        if (data.poisonChance > 100.0f) {
            float chance = (data.poisonChance - 100) / 100f;
            if (RANDOM.nextFloat() < chance) {
                target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.POISON, 120, amplifier, true, false, true));
            }
        }

        // 5. Freeze Chance
        if (data.freezeChance > 100.0f) {
            float chance = (data.freezeChance - 100) / 100f;
            if (RANDOM.nextFloat() < chance) {
                target.addStatusEffect(new StatusEffectInstance(
                    MRPGCEffects.FROZEN_SOLID.entry, 60, 0, true, false, true));
            }
        }

        // 6. Bleeding Chance
        if (data.bleedingChance > 100.0f) {
            float chance = (data.bleedingChance - 100) / 100f;
            if (RANDOM.nextFloat() < chance) {
                target.addStatusEffect(new StatusEffectInstance(
                    MRPGCEffects.BLEEDING.entry, 120, amplifier, true, false, true));
            }
        }
    }
}
