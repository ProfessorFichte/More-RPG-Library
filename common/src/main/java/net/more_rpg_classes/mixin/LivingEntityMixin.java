package net.more_rpg_classes.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_power.api.SpellPowerTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);
    @Unique long lastSpellvampireTick = 0;
    @Unique private long lastLifestealTick = 0;
    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void mrpgc_lib$createLivingAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
                .add(MRPGCEntityAttributes.AIR_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.EARTH_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.FIRE_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.FROST_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.HEALING_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.WATER_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER)
                .add(MRPGCEntityAttributes.LIFESTEAL_MODIFIER)
                .add(MRPGCEntityAttributes.RAGE_MODIFIER)
                .add(MRPGCEntityAttributes.SPELL_VAMPIRE)
        ;
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void damageReflect$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(!DamageTypes.THORNS.equals(source.getType())
                || !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            EntityAttributeInstance dmgReflect = ((LivingEntity) (Object) this)
                    .getAttributeInstance(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER);
            int value1 = (int) dmgReflect.getValue();
            float reflectDamage = 0;
            if (value1 != 100) {
                value1 = value1 - 100;
                reflectDamage += reflectMethod(value1, source, amount);
            }
            if (reflectDamage > 0) {
                source.getAttacker().damage(source.getAttacker().getDamageSources().thorns((PlayerEntity) (Object) this), reflectDamage);
            }
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void spellVampire$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SpellPowerTags.DamageTypes.ALL)) {
            Entity entity = source.getAttacker();
            if (entity instanceof PlayerEntity playerEntity) {
                long currentTick = playerEntity.getWorld().getTime();
                if (currentTick - lastSpellvampireTick < MRPGCMod.tweaksConfig.value.spellVampireCooldownTicks) {
                    return;
                }

                float actualHealth = playerEntity.getHealth();
                float maxHealth = (float) playerEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
                EntityAttributeInstance spellVampire =
                        playerEntity.getAttributeInstance(MRPGCEntityAttributes.SPELL_VAMPIRE);
                if (spellVampire != null) {
                    int value = (int) spellVampire.getValue();
                    if (value != 100 && actualHealth != maxHealth) {
                        value = value - 100;
                        float multiplier = (float) value / 100f;
                        float heal = amount * multiplier;
                        playerEntity.heal(heal);
                        lastSpellvampireTick = currentTick;
                    }
                }
            }
        }
    }
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void lifesteal$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(DamageTypeTags.IS_PLAYER_ATTACK)) {
            Entity entity = source.getAttacker();
            if (entity instanceof PlayerEntity playerEntity) {
                long currentTick = playerEntity.getWorld().getTime();
                if (currentTick - lastLifestealTick < MRPGCMod.tweaksConfig.value.lifestealCooldownTicks) {
                    return;
                }

                float actualHealth = playerEntity.getHealth();
                float maxHealth = (float) playerEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
                EntityAttributeInstance lifesteal =
                        playerEntity.getAttributeInstance(MRPGCEntityAttributes.LIFESTEAL_MODIFIER);
                if (lifesteal != null) {
                    int value = (int) lifesteal.getValue();
                    if (value != 100 && actualHealth != maxHealth) {
                        value = value - 100;
                        float multiplier = (float) value / 100f;
                        float heal = amount * multiplier;
                        playerEntity.heal(heal);
                        lastLifestealTick = currentTick;
                    }
                }
            }
        }
    }

    @Unique
    private float reflectMethod(int reflectamount, DamageSource source, float damage) {
        if (reflectamount != 0) {
            Entity attacker = source.getAttacker();
            if (attacker instanceof LivingEntity && !attacker.getWorld().isClient) {
                return damage * ((float) reflectamount / 100);
            }
        }
        return 0;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    public void baseTickPowderSnowFrostedSolidEffect(CallbackInfo ci) {
        var entity = (LivingEntity) ((Object)this);
        entity.inPowderSnow = entity.inPowderSnow || hasStatusEffect(MRPGCEffects.FROSTED.registryEntry);
    }
    @Inject(method = "baseTick", at = @At("TAIL"))
    public void baseTickPowderSnowFrozenSolidEffect(CallbackInfo ci) {
        var entity = (LivingEntity) ((Object)this);
        entity.inPowderSnow = entity.inPowderSnow || hasStatusEffect(MRPGCEffects.FROZEN_SOLID.registryEntry);
    }
    
}

