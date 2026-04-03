package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.EntityHitResult;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_engine.api.effect.SpellEngineEffects;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static net.more_rpg_classes.util.CustomMethods.getRangedDamageAttribute;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

    @Unique
    private static final Map<UUID, Long> lastFuseTickMap = new HashMap<>();
    @Unique
    private static final Map<UUID, Long> lastStrongEffectTickMap = new HashMap<>();
    @Unique
    private static final Map<UUID, Long> lastWeakEffectTickMap = new HashMap<>();

    @Unique
    private void applyFuseDamage(LivingEntity attacker, LivingEntity target,
                                 RegistryEntry<EntityAttribute> fuseAttribute,
                                 SpellSchool spellSchool) {
        EntityAttributeInstance fuseInstance = attacker.getAttributeInstance(fuseAttribute);
        if (fuseInstance != null && fuseInstance.getValue() != 100.0) {
            EntityAttributeInstance spellPowerInstance = attacker.getAttributeInstance(spellSchool.attributeEntry);
            if (spellPowerInstance == null) return;
            float magicDamage = Math.max(0.1f, (float)((fuseInstance.getValue() - 100) / 100f) * (float) spellPowerInstance.getValue());
            target.timeUntilRegen = 0;
            target.damage(SpellDamageSource.create(spellSchool, attacker), magicDamage);
        }
    }

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
            applyFuseDamage(attacker, target, MRPGCEntityAttributes.AIR_FUSE_MODIFIER, MoreSpellSchools.AIR);
            applyFuseDamage(attacker, target, MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER, SpellSchools.ARCANE);
            applyFuseDamage(attacker, target, MRPGCEntityAttributes.EARTH_FUSE_MODIFIER, MoreSpellSchools.EARTH);
            applyFuseDamage(attacker, target, MRPGCEntityAttributes.FIRE_FUSE_MODIFIER, SpellSchools.FIRE);
            applyFuseDamage(attacker, target, MRPGCEntityAttributes.FROST_FUSE_MODIFIER, SpellSchools.FROST);
            applyFuseDamage(attacker, target, MRPGCEntityAttributes.HEALING_FUSE_MODIFIER, SpellSchools.HEALING);
            applyFuseDamage(attacker, target, MRPGCEntityAttributes.WATER_FUSE_MODIFIER, MoreSpellSchools.WATER);
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

            if (stunChance != null && stunChance.getValue() > 100.0) {
                float chance = (float)(stunChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            SpellEngineEffects.STUN.entry, 40, 0, true, false, true));
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

            if (bleedingChance != null && bleedingChance.getValue() > 100.0) {
                float chance = (float)(bleedingChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            MRPGCEffects.BLEEDING.entry, 120, amplifier, true, false, true));
                    lastWeakEffectTickMap.put(attackerUUID, currentTick);
                }
            }
        }
    }
}
