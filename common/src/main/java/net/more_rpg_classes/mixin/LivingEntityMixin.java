package net.more_rpg_classes.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.fx.SpellEngineParticles;
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
    @Unique private float actualDamageDealt = 0;
    @Unique private float healthBeforeDamage = 0;
    @Unique private static final ParticleBatch LIFESTEAL_PARTICLES = new ParticleBatch(
            SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.STRIPE,
                    SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString(),
            ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET, null,
            20, 0.18F, 0.5F, 0).color(Color.RED.toRGBA());

    @Unique
    private boolean isPlayerDamageForLifesteal(DamageSource damageSource) {
        if (damageSource.getAttacker() instanceof PlayerEntity) {
            return true;
        }
        if (damageSource.getSource() instanceof PersistentProjectileEntity projectile) {
            return projectile.getOwner() instanceof PlayerEntity;
        }
        return false;
    }

    @Unique
    private PlayerEntity getPlayerFromDamageSource(DamageSource damageSource) {
        // First try to get from attacker
        if (damageSource.getAttacker() instanceof PlayerEntity player) {
            return player;
        }
        // Try from projectile owner
        if (damageSource.getSource() instanceof PersistentProjectileEntity projectile) {
            if (projectile.getOwner() instanceof PlayerEntity player) {
                return player;
            }
        }
        return null;
    }
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
                .add(MRPGCEntityAttributes.BURNING_CHANCE)
                .add(MRPGCEntityAttributes.STAGGER_CHANCE)
                .add(MRPGCEntityAttributes.ARMOR_PIERCING)
                .add(MRPGCEntityAttributes.STUN_CHANCE)
                .add(MRPGCEntityAttributes.POISON_CHANCE)
                .add(MRPGCEntityAttributes.FREEZE_CHANCE)
                .add(MRPGCEntityAttributes.BLEEDING_CHANCE)
        ;
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void damageReflect$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(!DamageTypes.THORNS.equals(source.getType()) && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if(source.isDirect()){
                LivingEntity attackedEntity = ((LivingEntity) (Object) this);
                Entity attacker = source.getAttacker();
                EntityAttributeInstance dmgReflect =
                        attackedEntity.getAttributeInstance(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER);
                int value1 = (int) dmgReflect.getValue();
                float reflectDamage = 0;
                if (value1 != 100 && attacker instanceof LivingEntity livingAttacker && !attacker.getWorld().isClient) {
                    float reflectMultiplier = (float) (value1 - 100) /100;
                    reflectDamage = amount * reflectMultiplier;
                    if(reflectDamage != 0){
                        livingAttacker.timeUntilRegen = 0;
                        livingAttacker.damage(source.getAttacker().getDamageSources().thorns(livingAttacker), reflectDamage);
                    }
                }
            }
        }
    }



    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void captureHealthBeforeApplyDamage(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        boolean isMelee = damageSource.getAttacker() instanceof PlayerEntity && damageSource.isIn(DamageTypeTags.IS_PLAYER_ATTACK);
        boolean isProjectile = damageSource.getSource() instanceof PersistentProjectileEntity &&
                               ((PersistentProjectileEntity)damageSource.getSource()).getOwner() instanceof PlayerEntity;

        if (isMelee || isProjectile) {
            LivingEntity thisEntity = (LivingEntity)(Object)this;
            healthBeforeDamage = thisEntity.getHealth();
        }
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void calculateActualDamageFromHealthChange(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        boolean isMelee = damageSource.getAttacker() instanceof PlayerEntity && damageSource.isIn(DamageTypeTags.IS_PLAYER_ATTACK);
        boolean isProjectile = damageSource.getSource() instanceof PersistentProjectileEntity &&
                               ((PersistentProjectileEntity)damageSource.getSource()).getOwner() instanceof PlayerEntity;

        if (isMelee || isProjectile) {
            LivingEntity thisEntity = (LivingEntity)(Object)this;
            float healthAfter = thisEntity.getHealth();
            this.actualDamageDealt = healthBeforeDamage - healthAfter;
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
                        if(!playerEntity.getWorld().isClient()){
                            ParticleHelper.sendBatches(playerEntity, new ParticleBatch[]{LIFESTEAL_PARTICLES});
                        }
                        lastSpellvampireTick = currentTick;
                    }
                }
            }
        }
    }
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", shift = At.Shift.AFTER))
    private void lifesteal$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (isPlayerDamageForLifesteal(source)) {
            PlayerEntity playerEntity = getPlayerFromDamageSource(source);
            if (playerEntity != null && !playerEntity.getWorld().isClient()) {
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
                        float heal = this.actualDamageDealt * multiplier;
                        playerEntity.heal(heal);
                        if(!playerEntity.getWorld().isClient()){
                            ParticleHelper.sendBatches(playerEntity, new ParticleBatch[]{LIFESTEAL_PARTICLES});
                        }
                        lastLifestealTick = currentTick;
                    }
                }
            }
        }
    }

    @Unique
    private static final net.minecraft.util.Identifier ARMOR_PIERCING_ID = net.minecraft.util.Identifier.of("more_rpg_classes", "armor_piercing_reduction");

    @Inject(method = "damage", at = @At("HEAD"))
    private void armorPiercing$modifyArmor(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof PlayerEntity player && source.isIn(DamageTypeTags.IS_PLAYER_ATTACK) && !player.getWorld().isClient()) {
            EntityAttributeInstance armorPiercing = player.getAttributeInstance(MRPGCEntityAttributes.ARMOR_PIERCING);

            if (armorPiercing != null && armorPiercing.getValue() > 100.0) {
                float piercingPercent = (float)(armorPiercing.getValue() - 100) / 100f;

                LivingEntity thisEntity = (LivingEntity)(Object)this;
                EntityAttributeInstance armorAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
                EntityAttributeInstance toughnessAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);

                if (armorAttribute != null && toughnessAttribute != null) {
                    double currentArmor = armorAttribute.getValue();
                    double currentToughness = toughnessAttribute.getValue();

                    double armorReduction = -currentArmor * piercingPercent;
                    double toughnessReduction = -currentToughness * piercingPercent;

                    armorAttribute.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                            ARMOR_PIERCING_ID, armorReduction, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
                    toughnessAttribute.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                            ARMOR_PIERCING_ID, toughnessReduction, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
                }
            }
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void armorPiercing$restoreArmor(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof PlayerEntity player && source.isIn(DamageTypeTags.IS_PLAYER_ATTACK) && !player.getWorld().isClient()) {
            EntityAttributeInstance armorPiercing = player.getAttributeInstance(MRPGCEntityAttributes.ARMOR_PIERCING);

            if (armorPiercing != null && armorPiercing.getValue() > 100.0) {
                LivingEntity thisEntity = (LivingEntity)(Object)this;
                EntityAttributeInstance armorAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
                EntityAttributeInstance toughnessAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);

                if (armorAttribute != null && toughnessAttribute != null) {
                    armorAttribute.removeModifier(ARMOR_PIERCING_ID);
                    toughnessAttribute.removeModifier(ARMOR_PIERCING_ID);
                }
            }
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    public void baseTickPowderSnowFrostedSolidEffect(CallbackInfo ci) {
        var entity = (LivingEntity) ((Object)this);
        entity.inPowderSnow = entity.inPowderSnow || hasStatusEffect(MRPGCEffects.FROSTED.entry);
    }
    @Inject(method = "baseTick", at = @At("TAIL"))
    public void baseTickPowderSnowFrozenSolidEffect(CallbackInfo ci) {
        var entity = (LivingEntity) ((Object)this);
        entity.inPowderSnow = entity.inPowderSnow || hasStatusEffect(MRPGCEffects.FROZEN_SOLID.entry);
    }
    
}

