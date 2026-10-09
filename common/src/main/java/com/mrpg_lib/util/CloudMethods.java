package com.mrpg_lib.util;

import com.mrpg_lib.entity.CustomCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public final class CloudMethods {
    private CloudMethods() {
    }

    public static CustomCloudEntity spawnCustomCloud(
            ParticleEffect particleType,
            Entity owner,
            Entity placementTarget,
            int waitTime,
            float radiusCloud,
            int durationSecondsCloud,
            float radiusGrowthCloud,
            @Nullable RegistryEntry<StatusEffect> statusEffect,
            int durationSecondsStatusEffect,
            int amplifierStatusEffect,
            boolean canStackAmplifier,
            int maxAmplifier,
            boolean canDealDamage,
            float damageAmount,
            @Nullable DamageSource damageSource) {
        if (owner.getWorld().isClient) {
            return null;
        }
        CustomCloudEntity cloud = new CustomCloudEntity(owner.getWorld(), placementTarget.getX(), placementTarget.getY(), placementTarget.getZ());

        if (owner instanceof LivingEntity living) {
            cloud.setOwner(living);
        } else if (owner instanceof ProjectileEntity projectile && projectile.getOwner() instanceof LivingEntity living) {
            cloud.setOwner(living);
        }

        cloud.setParticleType(particleType);
        cloud.setRadius(radiusCloud);
        cloud.setDuration(durationSecondsCloud * 20);
        cloud.setWaitTime(waitTime);
        cloud.setRadiusGrowth((radiusGrowthCloud - radiusCloud) / (float) (durationSecondsCloud * 20));

        if (statusEffect != null) {
            cloud.setStatusEffect(statusEffect, durationSecondsStatusEffect * 20, amplifierStatusEffect);
            cloud.setAmplifierStacking(canStackAmplifier, maxAmplifier);
        }

        cloud.setDamageProperties(canDealDamage, damageAmount, damageSource);
        owner.getWorld().spawnEntity(cloud);
        return cloud;
    }
}
