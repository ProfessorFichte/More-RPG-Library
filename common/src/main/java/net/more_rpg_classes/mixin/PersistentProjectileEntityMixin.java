package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
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
    private void applyFuseDamage(PlayerEntity player, LivingEntity target,
                                 RegistryEntry<EntityAttribute> fuseAttribute,
                                 SpellSchool spellSchool) {
        if (target == null || player.getWorld().isClient()) {
            return;
        }

        EntityAttributeInstance fuseInstance = player.getAttributeInstance(fuseAttribute);
        if (fuseInstance != null && fuseInstance.getValue() != 100.0) {
            float fuseBonus = (float)((fuseInstance.getValue() - 100) / 100f);
            float spellPower = (float) player.getAttributeValue(spellSchool.attributeEntry);
            float magicDamage = Math.max(0.1f, fuseBonus * spellPower);
            target.timeUntilRegen = 0;
            target.damage(SpellDamageSource.create(spellSchool, player), magicDamage);
        }
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void mrpgc$applyAttributeEffectsOnHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity projectile = (PersistentProjectileEntity)(Object)this;
        Entity hitEntity = entityHitResult.getEntity();

        if (!(hitEntity instanceof LivingEntity target)) {
            return;
        }

        if (!(projectile.getOwner() instanceof PlayerEntity player)) {
            return;
        }

        if (player.getWorld().isClient()) {
            return;
        }

        UUID playerUUID = player.getUuid();
        long currentTick = player.getWorld().getTime();

        long lastFuseTick = lastFuseTickMap.getOrDefault(playerUUID, 0L);
        if (currentTick - lastFuseTick >= 20) {
            lastFuseTickMap.put(playerUUID, currentTick);
            applyFuseDamage(player, target, MRPGCEntityAttributes.AIR_FUSE_MODIFIER, MoreSpellSchools.AIR);
            applyFuseDamage(player, target, MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER, SpellSchools.ARCANE);
            applyFuseDamage(player, target, MRPGCEntityAttributes.EARTH_FUSE_MODIFIER, MoreSpellSchools.EARTH);
            applyFuseDamage(player, target, MRPGCEntityAttributes.FIRE_FUSE_MODIFIER, SpellSchools.FIRE);
            applyFuseDamage(player, target, MRPGCEntityAttributes.FROST_FUSE_MODIFIER, SpellSchools.FROST);
            applyFuseDamage(player, target, MRPGCEntityAttributes.HEALING_FUSE_MODIFIER, SpellSchools.HEALING);
            applyFuseDamage(player, target, MRPGCEntityAttributes.WATER_FUSE_MODIFIER, MoreSpellSchools.WATER);
        }

        Random random = new Random();
        float attackDamage = (float) getRangedDamageAttribute(player);
        int amplifier = (int)(attackDamage * 0.15);

        long lastStrongTick = lastStrongEffectTickMap.getOrDefault(playerUUID, 0L);
        if (currentTick - lastStrongTick >= 160) {
            EntityAttributeInstance burningChance = player.getAttributeInstance(MRPGCEntityAttributes.BURNING_CHANCE);
            if (burningChance != null && burningChance.getValue() > 100.0) {
                float chance = (float)(burningChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            MRPGCEffects.IGNITED.entry, 40, amplifier, true, false, true));
                    lastStrongEffectTickMap.put(playerUUID, currentTick);
                }
            }

            EntityAttributeInstance staggerChance = player.getAttributeInstance(MRPGCEntityAttributes.STAGGER_CHANCE);
            if (staggerChance != null && staggerChance.getValue() > 100.0) {
                float chance = (float)(staggerChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            MRPGCEffects.STAGGER.entry, 80, amplifier, true, false, true));
                    lastStrongEffectTickMap.put(playerUUID, currentTick);
                }
            }

            EntityAttributeInstance stunChance = player.getAttributeInstance(MRPGCEntityAttributes.STUN_CHANCE);
            if (stunChance != null && stunChance.getValue() > 100.0) {
                float chance = (float)(stunChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            SpellEngineEffects.STUN.entry, 40, 0, true, false, true));
                    lastStrongEffectTickMap.put(playerUUID, currentTick);
                }
            }

            EntityAttributeInstance freezeChance = player.getAttributeInstance(MRPGCEntityAttributes.FREEZE_CHANCE);
            if (freezeChance != null && freezeChance.getValue() > 100.0) {
                float chance = (float)(freezeChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            MRPGCEffects.FROZEN_SOLID.entry, 60, 0, true, false, true));
                    lastStrongEffectTickMap.put(playerUUID, currentTick);
                }
            }
        }

        long lastWeakTick = lastWeakEffectTickMap.getOrDefault(playerUUID, 0L);
        if (currentTick - lastWeakTick >= 80) {
            EntityAttributeInstance poisonChance = player.getAttributeInstance(MRPGCEntityAttributes.POISON_CHANCE);
            if (poisonChance != null && poisonChance.getValue() > 100.0) {
                float chance = (float)(poisonChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.POISON, 120, amplifier, true, false, true));
                    lastWeakEffectTickMap.put(playerUUID, currentTick);
                }
            }

            EntityAttributeInstance bleedingChance = player.getAttributeInstance(MRPGCEntityAttributes.BLEEDING_CHANCE);
            if (bleedingChance != null && bleedingChance.getValue() > 100.0) {
                float chance = (float)(bleedingChance.getValue() - 100) / 100f;
                if (random.nextFloat() < chance) {
                    target.addStatusEffect(new StatusEffectInstance(
                            MRPGCEffects.BLEEDING.entry, 120, amplifier, true, false, true));
                    lastWeakEffectTickMap.put(playerUUID, currentTick);
                }
            }
        }
    }
}
