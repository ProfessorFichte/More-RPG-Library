package net.more_rpg_classes.util;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

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

    public static void spellSchoolDamageCalculation(SpellSchool spellSchool, float damageMultiplication, LivingEntity target, PlayerEntity attacker){
        float spellPower = (float) spellSchool.getValue(SpellSchool.Trait.POWER,new SpellSchool.QueryArgs(attacker));
        float critChance = (float) spellSchool.getValue(SpellSchool.Trait.CRIT_CHANCE,new SpellSchool.QueryArgs(attacker));
        float critDamage = (float) spellSchool.getValue(SpellSchool.Trait.CRIT_DAMAGE,new SpellSchool.QueryArgs(attacker));
        
        float damageAmount = spellPower * damageMultiplication;
        float random = new Random().nextFloat(1.0F);
        if(random < critChance){
            damageAmount = damageAmount* critDamage;
        }
        target.damage(SpellDamageSource.create(spellSchool,attacker),damageAmount);
    }

    public static double getHighestSpellSchoolPower(LivingEntity entity) {
        double maxPower = 0.0;
        // Check all SpellSchools
        // SPELL POWER MOD
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.ARCANE.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.FIRE.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.FROST.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.HEALING.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.LIGHTNING.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(SpellSchools.SOUL.attributeEntry));
        // MORE RPG LIBRARY
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.EARTH.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.WATER.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.AIR.attributeEntry));
        maxPower = Math.max(maxPower, entity.getAttributeValue(MoreSpellSchools.NATURE.attributeEntry));

        return maxPower;
    }
    public static double getHighestDamageAttribute(LivingEntity entity) {
        double entitySpellPower = getHighestSpellSchoolPower(entity);
        double meleeDamage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double rangedDamage = 0;
        if(FabricLoader.getInstance().isModLoaded("ranged_weapon_api")){
            rangedDamage = entity.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
        }
        return Math.max(meleeDamage, Math.max(rangedDamage, entitySpellPower));
    }
    public static boolean isEntityProtectedCheck(Entity other, LivingEntity owner) {
        if (other == null || owner == null) {
            return false;
        }
        if (other instanceof PlayerEntity && owner instanceof MobEntity) {
            return false;
        }
        LivingEntity otherLivingEntity = null;
        if (other instanceof LivingEntity livingEntity) {
            otherLivingEntity = livingEntity;
        } else if (other instanceof ProjectileEntity projectileEntity) {
            if (projectileEntity.getOwner() instanceof LivingEntity projectileOwnerLiving) {
                otherLivingEntity = projectileOwnerLiving;
            }
        }
        if (otherLivingEntity == null) {
            return false;
        }
        var relation = EntityRelations.getRelation(owner, otherLivingEntity);
        switch (relation) {
            case ALLY, FRIENDLY -> {
                return true;
            }
            case MIXED, HOSTILE, NEUTRAL -> {
                return false;
            }
        }
        return false;
    }
    public static double getRangedDamageAttribute(LivingEntity entity){
        if (FabricLoader.getInstance().isModLoaded("ranged_weapon_api")) {
            return entity.getAttributeValue( EntityAttributes_RangedWeapon.DAMAGE.entry);
        }
        else {
           return entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }
    }
}
