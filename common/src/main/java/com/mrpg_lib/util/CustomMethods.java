package com.mrpg_lib.util;

import com.mrpg_lib.compat.CompatHooks;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

public class CustomMethods {
    public static void clearNegativeEffects(LivingEntity entity, boolean removeOne) {
        var effects = entity.getStatusEffects();
        var toRemove = new java.util.ArrayList<RegistryEntry<StatusEffect>>();
        for (var instance : effects) {
            var effectEntry = instance.getEffectType();
            if (!effectEntry.value().isBeneficial() && !effectEntry.equals(StatusEffects.TRIAL_OMEN)) {
                toRemove.add(effectEntry);
            }
        }
        if (removeOne) {
            if (!toRemove.isEmpty()) {
                entity.removeStatusEffect(toRemove.get(0));
            }
        } else {
            for (var effect : toRemove) {
                entity.removeStatusEffect(effect);
            }
        }
    }
    public static void stackFreezeStacks(LivingEntity e, int amount) {
        if (!e.canFreeze()) return;

        int cap = e.getMinFreezeDamageTicks() +5;
        int newTicks = Math.min(cap, e.getFrozenTicks() + amount);
        e.setFrozenTicks(newTicks);
    }
    public static void freezeDamageTicks(LivingEntity e) {
        if (!e.canFreeze()) return;

        int cap = e.getMinFreezeDamageTicks() +5;
        int newTicks = Math.min(cap, e.getFrozenTicks() + 3);
        e.setFrozenTicks(newTicks);
    }
    public static void spawnCloudEntity(
            ParticleEffect particleType, Entity owner, float radiusCloud, int durationSecondsCloud, float radiusGrowthCloud
            , RegistryEntry<StatusEffect> statusEffect, int durationSecondsStatusEffect, int amplifierStatusEffect) {
        if (!owner.getWorld().isClient) {
            List<LivingEntity> list = owner.getWorld().getNonSpectatingEntities(LivingEntity.class, owner.getBoundingBox().expand(4.0, 2.0, 4.0));
            AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(owner.getWorld(), owner.getX(), owner.getY(), owner.getZ());
            Entity entity = null;
            if (owner instanceof LivingEntity) {
                entity = owner;
            } else if (owner instanceof ProjectileEntity projectile) {
                owner = projectile.getOwner();
                areaEffectCloudEntity.setOwner((LivingEntity) owner);
            }
            if (entity instanceof LivingEntity) {
                areaEffectCloudEntity.setOwner((LivingEntity) entity);
            }
            areaEffectCloudEntity.setParticleType(particleType);
            areaEffectCloudEntity.setRadius(radiusCloud);
            areaEffectCloudEntity.setDuration(durationSecondsCloud * 20);
            areaEffectCloudEntity.setRadiusGrowth((radiusGrowthCloud - areaEffectCloudEntity.getRadius()) / (float) areaEffectCloudEntity.getDuration());
            areaEffectCloudEntity.addEffect(new StatusEffectInstance(statusEffect,
                    durationSecondsStatusEffect * 20, amplifierStatusEffect, false, false, true));

            if (!list.isEmpty()) {
                Iterator var5 = list.iterator();
                while (var5.hasNext()) {
                    LivingEntity livingEntity2 = (LivingEntity) var5.next();
                    double x = owner.squaredDistanceTo(livingEntity2);
                    if (x < 16.0) {
                        areaEffectCloudEntity.setPosition(livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ());
                        break;
                    }
                }
            }
            owner.getWorld().spawnEntity(areaEffectCloudEntity);
        }
    }

    public static void applyStatusEffect(LivingEntity target, int effectAmplifier,int effectDurationSeconds,RegistryEntry<StatusEffect> statusEffect,
                                         int maxStackAmplifier, boolean canStackAmplifier, boolean showIcon, boolean increaseDuration,
                                         int increaseEffectDurationSeconds){

            if(target.hasStatusEffect(statusEffect)){
                int currentAmplifier = target.getStatusEffect(statusEffect).getAmplifier();
                int currentDuration = target.getStatusEffect(statusEffect).getDuration();
                int increaseAmp = 0;
                if(increaseDuration){
                    currentDuration = currentDuration + (increaseEffectDurationSeconds*20);
                }
                if(canStackAmplifier){
                    increaseAmp = increaseAmp + 1;
                }
                if(currentAmplifier<maxStackAmplifier){
                    target.addStatusEffect(new StatusEffectInstance(statusEffect, currentDuration, currentAmplifier + increaseAmp, false, false, showIcon));
                }else{
                    target.addStatusEffect(new StatusEffectInstance(statusEffect, currentDuration, maxStackAmplifier, false, false, showIcon));
                }
            }else{
                target.addStatusEffect(new StatusEffectInstance(statusEffect, effectDurationSeconds*20, effectAmplifier, false, false, showIcon));
            }
    }

    public static double getHighestDamageAttribute(LivingEntity entity) {
        double entitySpellPower = CompatHooks.highestSpellSchoolPower(entity);
        double meleeDamage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var rangedAttribute = CompatHooks.rangedDamageAttribute();
        double rangedDamage = rangedAttribute != null ? entity.getAttributeValue(rangedAttribute) : 0;
        return Math.max(meleeDamage, Math.max(rangedDamage, entitySpellPower));
    }
    public static double getRangedDamageAttribute(LivingEntity entity) {
        var rangedAttribute = CompatHooks.rangedDamageAttribute();
        var instance = entity.getAttributeInstance(rangedAttribute != null ? rangedAttribute : EntityAttributes.GENERIC_ATTACK_DAMAGE);
        return instance != null ? instance.getValue() : 0.0;
    }
}
