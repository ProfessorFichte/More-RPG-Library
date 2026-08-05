package net.more_rpg_classes.util;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.spell_engine.utils.TargetHelper;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.*;

public class CustomMethods {

    public static boolean clearNegativeEffects(LivingEntity entity, boolean debuff) {
        if (!debuff) return false;
        var effects = entity.getStatusEffects();
        var toRemove = new ArrayList<StatusEffect>();
        for (var instance : effects) {
            StatusEffect effect = instance.getEffectType();
            if (!effect.isBeneficial()) {
                toRemove.add(effect);
            }
        }
        for (var effect : toRemove) {
            entity.removeStatusEffect(effect);
        }
        return !toRemove.isEmpty();
    }

    public static void stackFreezeStacks(LivingEntity entity, int amount) {
        if (!entity.canFreeze()) return;
        int cap = entity.getMinFreezeDamageTicks() + 5;
        int newTicks = Math.min(cap, entity.getFrozenTicks() + amount);
        entity.setFrozenTicks(newTicks);
    }

    public static void freezeDamageTicks(LivingEntity entity) {
        if (!entity.canFreeze()) return;
        int cap = entity.getMinFreezeDamageTicks() + 5;
        int newTicks = Math.min(cap, entity.getFrozenTicks() + 3);
        entity.setFrozenTicks(newTicks);
    }

    public static void applyStatusEffect(LivingEntity target, int effectAmplifier, int effectDurationSeconds, StatusEffect statusEffect,
                                         int maxStackAmplifier, boolean canStackAmplifier, boolean showIcon, boolean increaseDuration,
                                         int increaseEffectDurationSeconds) {
        if (target.hasStatusEffect(statusEffect)) {
            int currentAmplifier = target.getStatusEffect(statusEffect).getAmplifier();
            int currentDuration = target.getStatusEffect(statusEffect).getDuration();
            int increaseAmp = 0;
            if (increaseDuration) {
                currentDuration = currentDuration + (increaseEffectDurationSeconds * 20);
            }
            if (canStackAmplifier) {
                increaseAmp = increaseAmp + 1;
            }
            if (currentAmplifier < maxStackAmplifier) {
                target.addStatusEffect(new StatusEffectInstance(statusEffect, currentDuration, currentAmplifier + increaseAmp, false, false, showIcon));
            } else {
                target.addStatusEffect(new StatusEffectInstance(statusEffect, currentDuration, maxStackAmplifier, false, false, showIcon));
            }
        } else {
            target.addStatusEffect(new StatusEffectInstance(statusEffect, effectDurationSeconds * 20, effectAmplifier, false, false, showIcon));
        }
    }

    public static void increaseAmpByChance(
            LivingEntity entity, StatusEffect statusEffect, int duration, int amplifier, int max_amp, int chance) {
        int roll = (int) ((Math.random() * (1 + chance)) + 1);
        if (roll >= chance) {
            if (entity.hasStatusEffect(statusEffect)) {
                int currentAmplifier = entity.getStatusEffect(statusEffect).getAmplifier();
                if (currentAmplifier >= max_amp) {
                    entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, currentAmplifier, false, false, true));
                    return;
                }
                entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, currentAmplifier + amplifier, false, false, true));
            }
            entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, false, false, true));
        }
    }

    public static void increaseHiddenAmpByChance(
            LivingEntity entity, StatusEffect statusEffect, int duration, int amplifier, int max_amp, int chance) {
        int roll = (int) ((Math.random() * (1 + chance)) + 1);
        if (roll >= chance) {
            if (entity.hasStatusEffect(statusEffect)) {
                int currentAmplifier = entity.getStatusEffect(statusEffect).getAmplifier();
                int currentDuration = entity.getStatusEffect(statusEffect).getDuration();
                if (currentAmplifier >= max_amp) {
                    entity.addStatusEffect(new StatusEffectInstance(statusEffect, currentDuration, currentAmplifier, false, false, false));
                    return;
                }
                entity.addStatusEffect(new StatusEffectInstance(statusEffect, currentDuration, currentAmplifier + amplifier, false, false, false));
            }
            entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, false, false, false));
        }
    }

    public static void decreaseAmpByChance(
            LivingEntity entity, StatusEffect statusEffect, int removedampstack, int chance) {
        int roll = (int) ((Math.random() * (1 + chance)) + 1);
        if (roll >= chance) {
            if (entity.hasStatusEffect(statusEffect)) {
                int currentAmp = entity.getStatusEffect(statusEffect).getAmplifier();
                int Duration = entity.getStatusEffect(statusEffect).getDuration();
                if (currentAmp < 1) {
                    entity.removeStatusEffect(statusEffect);
                    return;
                }
                entity.removeStatusEffect(statusEffect);
                entity.addStatusEffect(new StatusEffectInstance(
                        statusEffect, Duration, currentAmp - removedampstack, false, false, true));
            }
        }
    }

    public static void decreaseAmp(
            LivingEntity entity, StatusEffect statusEffect, int removedampstack) {
        if (entity.hasStatusEffect(statusEffect)) {
            int currentAmp = entity.getStatusEffect(statusEffect).getAmplifier();
            int Duration = entity.getStatusEffect(statusEffect).getDuration();
            if (currentAmp < 1) {
                entity.removeStatusEffect(statusEffect);
                return;
            }
            entity.removeStatusEffect(statusEffect);
            entity.addStatusEffect(new StatusEffectInstance(
                    statusEffect, Duration, currentAmp - removedampstack, false, false, true));
        }
    }

    public static void increaseEffectLevel(LivingEntity entity, StatusEffect statusEffect, int duration, int amplifier, int amplifierMax) {
        if (entity.hasStatusEffect(statusEffect)) {
            int currentAmplifier = entity.getStatusEffect(statusEffect).getAmplifier();
            if (currentAmplifier >= amplifierMax) {
                entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, currentAmplifier, false, false, true));
                return;
            }
            entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, currentAmplifier + amplifier, false, false, true));
        }
        entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, false, false, true));
    }

    public static void increaseHiddenEffectLevel(LivingEntity entity, StatusEffect statusEffect, int duration, int amplifier, int amplifierMax) {
        if (entity.hasStatusEffect(statusEffect)) {
            int currentAmplifier = entity.getStatusEffect(statusEffect).getAmplifier();
            if (currentAmplifier >= amplifierMax) {
                entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, currentAmplifier, false, false, false));
                return;
            }
            entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, currentAmplifier + amplifier, false, false, false));
        }
        entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, false, false, false));
    }

    public static void decreaseEffectLevel(LivingEntity entity, StatusEffect statusEffect, int amplifier) {
        if (entity.hasStatusEffect(statusEffect)) {
            int currentAmplifier = entity.getStatusEffect(statusEffect).getAmplifier();
            int currentDuration = entity.getStatusEffect(statusEffect).getDuration();
            if (currentAmplifier < 1) {
                entity.removeStatusEffect(statusEffect);
                return;
            }
            entity.removeStatusEffect(statusEffect);
            entity.addStatusEffect(new StatusEffectInstance(statusEffect, currentDuration, currentAmplifier - amplifier, false, false, true));
        }
    }

    public static double getHighestSpellSchoolPower(LivingEntity entity) {
        double maxPower = 0.0;
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.ARCANE.attribute));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.FIRE.attribute));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.FROST.attribute));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.HEALING.attribute));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.LIGHTNING.attribute));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.SOUL.attribute));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.EARTH.attribute));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.WATER.attribute));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.AIR.attribute));
        return maxPower;
    }

    public static double getHighestDamageAttribute(LivingEntity entity) {
        double spellPower = getHighestSpellSchoolPower(entity);
        double meleeDamage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double rangedDamage = getRangedDamageAttribute(entity);
        return Math.max(meleeDamage, Math.max(rangedDamage, spellPower));
    }

    public static double getRangedDamageAttribute(LivingEntity entity) {
        if (FabricLoader.getInstance().isModLoaded("ranged_weapon_api")) {
            try {
                var instance = entity.getAttributeInstance(EntityAttributes_RangedWeapon.DAMAGE.attribute);
                return instance != null ? instance.getValue() : 0.0;
            } catch (Throwable t) {
                return entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            }
        }
        return entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }

    public static boolean isEntityProtectedCheck(Entity other, LivingEntity owner) {
        if (other == null || owner == null) return false;
        if (other instanceof PlayerEntity && owner instanceof MobEntity) return false;

        LivingEntity otherLiving = null;
        if (other instanceof LivingEntity le) {
            otherLiving = le;
        } else if (other instanceof ProjectileEntity pe) {
            if (pe.getOwner() instanceof LivingEntity lo) {
                otherLiving = lo;
            }
        }
        if (otherLiving == null) return false;

        var relation = TargetHelper.getRelation(owner, otherLiving);
        switch (relation) {
            case FRIENDLY, SEMI_FRIENDLY -> { return true; }
            case NEUTRAL, MIXED, HOSTILE -> { return false; }
        }
        return false;
    }

    public static void spellSchoolDamageCalculation(SpellSchool spellSchool, float damageMultiplication, LivingEntity target, PlayerEntity attacker) {
        float spellPower = (float) spellSchool.getValue(SpellSchool.Trait.POWER, new SpellSchool.QueryArgs(attacker));
        float critChance = (float) spellSchool.getValue(SpellSchool.Trait.CRIT_CHANCE, new SpellSchool.QueryArgs(attacker));
        float critDamage = (float) spellSchool.getValue(SpellSchool.Trait.CRIT_DAMAGE, new SpellSchool.QueryArgs(attacker));
        float damageAmount = spellPower * damageMultiplication;
        if (new Random().nextFloat(1.0F) < critChance) {
            damageAmount *= critDamage;
        }
        target.damage(SpellDamageSource.create(spellSchool, attacker), damageAmount);
    }

    public static void spawnCloudEntity(
            ParticleEffect particleType, Entity owner, float radiusCloud, int durationSecondsCloud, float radiusGrowthCloud,
            StatusEffect statusEffect, int durationSecondsStatusEffect, int amplifierStatusEffect) {
        if (!owner.getWorld().isClient) {
            List<LivingEntity> list = owner.getWorld().getNonSpectatingEntities(LivingEntity.class, owner.getBoundingBox().expand(4.0, 2.0, 4.0));
            AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(owner.getWorld(), owner.getX(), owner.getY(), owner.getZ());
            if (owner instanceof LivingEntity le) {
                cloud.setOwner(le);
            } else if (owner instanceof ProjectileEntity pe && pe.getOwner() instanceof LivingEntity lo) {
                cloud.setOwner(lo);
            }
            cloud.setParticleType(particleType);
            cloud.setRadius(radiusCloud);
            cloud.setDuration(durationSecondsCloud * 20);
            cloud.setRadiusGrowth((radiusGrowthCloud - cloud.getRadius()) / (float) cloud.getDuration());
            cloud.addEffect(new StatusEffectInstance(statusEffect, durationSecondsStatusEffect * 20, amplifierStatusEffect, false, false, true));
            if (!list.isEmpty()) {
                for (LivingEntity nearby : list) {
                    if (owner.squaredDistanceTo(nearby) < 16.0) {
                        cloud.setPosition(nearby.getX(), nearby.getY(), nearby.getZ());
                        break;
                    }
                }
            }
            owner.getWorld().spawnEntity(cloud);
        }
    }
}
