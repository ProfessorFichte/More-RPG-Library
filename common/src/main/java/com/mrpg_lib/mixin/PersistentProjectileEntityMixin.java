package com.mrpg_lib.mixin;

import com.mrpg_lib.compat.CompatHooks;
import com.mrpg_lib.effect.MRPGCEffects;
import com.mrpg_lib.entity.attribute.MRPGCEntityAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.mrpg_lib.util.CustomMethods.getRangedDamageAttribute;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

    @Unique
    private static final Map<UUID, Long> lastFuseTickMap = new HashMap<>();
    @Unique
    private static final Map<UUID, Long> lastStrongEffectTickMap = new HashMap<>();
    @Unique
    private static final Map<UUID, Long> lastWeakEffectTickMap = new HashMap<>();

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void mrpgc$applyAttributeEffectsOnHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity projectile = (PersistentProjectileEntity)(Object)this;
        Entity hitEntity = entityHitResult.getEntity();

        if (!(hitEntity instanceof LivingEntity target)) {
            return;
        }

        if (!(projectile.getOwner() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.getWorld().isClient()) {
            return;
        }

        UUID attackerUUID = attacker.getUuid();
        long currentTick = attacker.getWorld().getTime();

        long lastFuseTick = lastFuseTickMap.getOrDefault(attackerUUID, 0L);
        if (currentTick - lastFuseTick >= 20) {
            lastFuseTickMap.put(attackerUUID, currentTick);
            CompatHooks.applyFuse(attacker, target, false);
        }

        EntityAttributeInstance burningChance = attacker.getAttributeInstance(MRPGCEntityAttributes.BURNING_CHANCE);
        EntityAttributeInstance staggerChance = attacker.getAttributeInstance(MRPGCEntityAttributes.STAGGER_CHANCE);
        EntityAttributeInstance stunChance = attacker.getAttributeInstance(MRPGCEntityAttributes.STUN_CHANCE);
        EntityAttributeInstance freezeChance = attacker.getAttributeInstance(MRPGCEntityAttributes.FREEZE_CHANCE);
        EntityAttributeInstance poisonChance = attacker.getAttributeInstance(MRPGCEntityAttributes.POISON_CHANCE);
        EntityAttributeInstance bleedingChance = attacker.getAttributeInstance(MRPGCEntityAttributes.BLEEDING_CHANCE);

        boolean anyChanceActive = (burningChance != null && burningChance.getValue() > 100.0)
                || (staggerChance != null && staggerChance.getValue() > 100.0)
                || (stunChance != null && stunChance.getValue() > 100.0)
                || (freezeChance != null && freezeChance.getValue() > 100.0)
                || (poisonChance != null && poisonChance.getValue() > 100.0)
                || (bleedingChance != null && bleedingChance.getValue() > 100.0);
        if (!anyChanceActive) return;

        Random random = new Random();
        int amplifier = (int)((float) getRangedDamageAttribute(attacker) * 0.15);

        long lastStrongTick = lastStrongEffectTickMap.getOrDefault(attackerUUID, 0L);
        if (currentTick - lastStrongTick >= 160) {
            if (burningChance != null && burningChance.getValue() > 100.0) {
                float chance = (float)(burningChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            MRPGCEffects.IGNITED.entry, 40, amplifier, true, false, true));
                    lastStrongEffectTickMap.put(attackerUUID, currentTick);
                }
            }

            if (staggerChance != null && staggerChance.getValue() > 100.0) {
                float chance = (float)(staggerChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            MRPGCEffects.STAGGER.entry, 80, amplifier, true, false, true));
                    lastStrongEffectTickMap.put(attackerUUID, currentTick);
                }
            }

            var stunEffect = CompatHooks.stunEffect();
            if (stunEffect != null && stunChance != null && stunChance.getValue() > 100.0) {
                float chance = (float)(stunChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            stunEffect, 40, 0, true, false, true));
                    lastStrongEffectTickMap.put(attackerUUID, currentTick);
                }
            }

            if (freezeChance != null && freezeChance.getValue() > 100.0) {
                float chance = (float)(freezeChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            MRPGCEffects.FROZEN_SOLID.entry, 60, 0, true, false, true));
                    lastStrongEffectTickMap.put(attackerUUID, currentTick);
                }
            }
        }

        long lastWeakTick = lastWeakEffectTickMap.getOrDefault(attackerUUID, 0L);
        if (currentTick - lastWeakTick >= 80) {
            if (poisonChance != null && poisonChance.getValue() > 100.0) {
                float chance = (float)(poisonChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.POISON, 120, amplifier, true, false, true));
                    lastWeakEffectTickMap.put(attackerUUID, currentTick);
                }
            }

            var bleedEffect = CompatHooks.bleedEffect();
            if (bleedEffect != null && bleedingChance != null && bleedingChance.getValue() > 100.0) {
                float chance = (float)(bleedingChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            bleedEffect, 120, amplifier, true, false, true));
                    lastWeakEffectTickMap.put(attackerUUID, currentTick);
                }
            }
        }
    }
}
