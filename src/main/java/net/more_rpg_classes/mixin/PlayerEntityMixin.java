package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", shift = At.Shift.AFTER))
    private void mrpgc$applyLifesteal(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!(target instanceof LivingEntity)) return;
        float damage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        EntityAttributeInstance lifesteal = player.getAttributeInstance(MRPGCEntityAttributes.LIFESTEAL_MODIFIER);
        if (lifesteal != null && lifesteal.getValue() != 100.0) {
            float healAmount = damage * ((float) (lifesteal.getValue() - 100) / 100f);
            player.heal(healAmount);
        }
    }


    @Inject(method = "attack", at = @At("TAIL"))
    private void mrpgc$boostRageDamageOnAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance rage = player.getAttributeInstance(MRPGCEntityAttributes.RAGE_MODIFIER);
            if (rage != null) {
                float rageValue = (float) (rage.getValue() - 100) / 100f;
                float health = player.getHealth();
                float maxHealth = (float) player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
                float missing = (maxHealth - health) / maxHealth;
                if (rageValue != 0 && health < maxHealth) {
                    float baseDamage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    float extraDamage = Math.max(0.1F, baseDamage * rageValue * missing);
                    livingTarget.timeUntilRegen = 0;
                    livingTarget.damage(player.getDamageSources().playerAttack(player), extraDamage);
                }
            }
        }
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void mrpgc$onAttackFuseMagicDamage(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object) this;
        ///AIR SPELL POWER
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance airFuse = player.getAttributeInstance(MRPGCEntityAttributes.AIR_FUSE_MODIFIER);
            if (airFuse != null && airFuse.getValue() != 100.0) {
                float airBonus = (float) ((airFuse.getValue() - 100) / 100f);
                float airPower = (float) player.getAttributeValue(MoreSpellSchools.AIR.attributeEntry);
                float magicDamage = Math.max(0.1f, airBonus * airPower);
                livingTarget.timeUntilRegen = 0;
                livingTarget.damage(SpellDamageSource.create(MoreSpellSchools.AIR, player), magicDamage);
            }
        }
        ///ARCANE SPELL POWER
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance arcaneFuse = player.getAttributeInstance(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER);
            if (arcaneFuse != null && arcaneFuse.getValue() != 100.0) {
                float arcaneBonus = (float) ((arcaneFuse.getValue() - 100) / 100f);
                float arcanePower = (float) player.getAttributeValue(SpellSchools.ARCANE.attributeEntry);
                float magicDamage = Math.max(0.1f, arcaneBonus * arcanePower);
                livingTarget.timeUntilRegen = 0;
                livingTarget.damage(SpellDamageSource.create(SpellSchools.ARCANE, player), magicDamage);
            }
        }
        ///EARTH SPELL POWER
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance earthFuse = player.getAttributeInstance(MRPGCEntityAttributes.EARTH_FUSE_MODIFIER);
            if (earthFuse != null && earthFuse.getValue() != 100.0) {
                float earthBonus = (float) ((earthFuse.getValue() - 100) / 100f);
                float earthPower = (float) player.getAttributeValue(MoreSpellSchools.EARTH.attributeEntry);
                float magicDamage = Math.max(0.1f, earthBonus * earthPower);
                livingTarget.timeUntilRegen = 0;
                livingTarget.damage(SpellDamageSource.create(MoreSpellSchools.EARTH, player), magicDamage);
            }
        }
        ///FIRE SPELL POWER
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance fireFuse = player.getAttributeInstance(MRPGCEntityAttributes.FIRE_FUSE_MODIFIER);
            if (fireFuse != null && fireFuse.getValue() != 100.0) {
                float fireBonus = (float) ((fireFuse.getValue() - 100) / 100f);
                float firePower = (float) player.getAttributeValue(SpellSchools.FIRE.attributeEntry);
                float magicDamage = Math.max(0.1f, fireBonus * firePower);
                livingTarget.timeUntilRegen = 0;
                livingTarget.damage(SpellDamageSource.create(SpellSchools.FIRE, player), magicDamage);
            }
        }
        ///FROST SPELL POWER
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance frostFuse = player.getAttributeInstance(MRPGCEntityAttributes.FROST_FUSE_MODIFIER);
            if (frostFuse != null && frostFuse.getValue() != 100.0) {
                float frostBonus = (float) ((frostFuse.getValue() - 100) / 100f);
                float frostPower = (float) player.getAttributeValue(SpellSchools.FROST.attributeEntry);
                float magicDamage = Math.max(0.1f, frostBonus * frostPower);
                livingTarget.timeUntilRegen = 0;
                livingTarget.damage(SpellDamageSource.create(SpellSchools.FROST, player), magicDamage);
            }
        }
        ///HEALING SPELL POWER
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance healingFuse = player.getAttributeInstance(MRPGCEntityAttributes.HEALING_FUSE_MODIFIER);
            if (healingFuse != null && healingFuse.getValue() != 100.0) {
                float healingBonus = (float) ((healingFuse.getValue() - 100) / 100f);
                float healingPower = (float) player.getAttributeValue(SpellSchools.HEALING.attributeEntry);
                float magicDamage = Math.max(0.1f, healingBonus * healingPower);
                livingTarget.timeUntilRegen = 0;
                livingTarget.damage(SpellDamageSource.create(SpellSchools.HEALING, player), magicDamage);
            }
        }
        ///WATER SPELL POWER
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance waterFuse = player.getAttributeInstance(MRPGCEntityAttributes.WATER_FUSE_MODIFIER);
            if (waterFuse != null && waterFuse.getValue() != 100.0) {
                float waterBonus = (float) ((waterFuse.getValue() - 100) / 100f);
                float waterPower = (float) player.getAttributeValue(MoreSpellSchools.WATER.attributeEntry);
                float magicDamage = Math.max(0.1f, waterBonus * waterPower);
                livingTarget.timeUntilRegen = 0;
                livingTarget.damage(SpellDamageSource.create(MoreSpellSchools.WATER, player), magicDamage);
            }
        }

    }

    /*
    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float rage$attack(float damage) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        int rage_attr = (int) ((LivingEntity) (Object) this).getAttributeValue(MRPGCEntityAttributes.RAGE_MODIFIER) -100;
        float value1 = (float) rage_attr / 100;
        float actual_health = player.getHealth();
        float max_health = (float) player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
        float missing_health_percentage = (max_health - actual_health) / max_health;
        if (rage_attr != 0 && actual_health != max_health){
            return damage + (damage * (value1 * missing_health_percentage));
        }
        return damage;
    }
    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float arcanefuse$attack(float damage) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        int value2 = (int) ((LivingEntity) (Object) this).getAttributeValue(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER) -100;
        float arcane_spellpower = (float) player.getAttributeValue(SpellSchools.ARCANE.getAttributeEntry());
        if(value2 != 0){
            float multiplier = (float) value2 /100;
            return damage + (multiplier * arcane_spellpower);
        }
        return damage;
    }
    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float lifesteal$attack(float damage) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        float actual_health = player.getHealth();
        float max_health = (float) player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);

        int value3 = (int) ((LivingEntity) (Object) this).getAttributeValue(MRPGCEntityAttributes.LIFESTEAL_MODIFIER) -100;
        if(value3 != 0 && actual_health != max_health){
            float multiplier = (float) value3 / 100;
            float heal = (damage * multiplier );
            player.heal(heal);
            return damage;
        }
        return damage;
    }
     */
}
