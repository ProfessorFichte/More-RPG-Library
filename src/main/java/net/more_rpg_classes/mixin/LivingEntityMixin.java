package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_engine.api.spell.ParticleBatch;
import net.spell_engine.particle.ParticleHelper;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique private float actualDamageDealt = 0;
    @Unique private float healthBeforeDamage = 0;

    @Unique private static final Map<UUID, Long> SPELL_VAMPIRE_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> LIFESTEAL_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> RAGE_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> FUSE_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> STRONG_EFFECT_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> WEAK_EFFECT_COOLDOWN = new HashMap<>();

    @Unique private static final TagKey<DamageType> SPELL_DAMAGE_TAG =
            TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("spell_power", "all"));

    @Unique private static final ParticleBatch LIFESTEAL_PARTICLES = new ParticleBatch(
            "minecraft:heart", ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER, null,
            8, 0.1F, 0.3F, 0);

    @Unique private static final ParticleBatch FUSE_PARTICLES = new ParticleBatch(
            "minecraft:enchant", ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER, null,
            12, 0.2F, 0.5F, 0);

    @Unique
    private LivingEntity getLivingAttackerFromDamageSource(DamageSource damageSource) {
        if (damageSource.getAttacker() instanceof LivingEntity living) {
            return living;
        }
        if (damageSource.getSource() instanceof PersistentProjectileEntity projectile) {
            if (projectile.getOwner() instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    @Inject(method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN"))
    private static void mrpgc_lib$createLivingAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
                .add(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER)
                .add(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.AIR_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.EARTH_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.FIRE_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.FROST_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.HEALING_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.WATER_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.LIFESTEAL_MODIFIER)
                .add(MRPGCEntityAttributes.RAGE_MODIFIER)
                .add(MRPGCEntityAttributes.SPELL_VAMPIRE)
                .add(MRPGCEntityAttributes.BURNING_CHANCE)
                .add(MRPGCEntityAttributes.STAGGER_CHANCE)
                .add(MRPGCEntityAttributes.STUN_CHANCE)
                .add(MRPGCEntityAttributes.POISON_CHANCE)
                .add(MRPGCEntityAttributes.FREEZE_CHANCE)
                .add(MRPGCEntityAttributes.BLEEDING_CHANCE)
                .add(MRPGCEntityAttributes.ARMOR_PIERCING)
                .add(MRPGCEntityAttributes.TENACITY)
                .add(MRPGCEntityAttributes.HEALING_TAKEN)
                .add(MRPGCEntityAttributes.DAMAGE_TAKEN);
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float mrpgc$modifyHealingTaken(float amount) {
        LivingEntity self = (LivingEntity)(Object)this;
        EntityAttributeInstance attr = self.getAttributeInstance(MRPGCEntityAttributes.HEALING_TAKEN);
        if (attr == null) return amount;
        return amount * (float)(attr.getValue() / 100.0);
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float mrpgc$modifyDamageTaken(float amount) {
        LivingEntity self = (LivingEntity)(Object)this;
        EntityAttributeInstance attr = self.getAttributeInstance(MRPGCEntityAttributes.DAMAGE_TAKEN);
        if (attr == null) return amount;
        return amount * (float)(attr.getValue() / 100.0);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void damageReflect$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!DamageTypes.THORNS.equals(source.getType()) && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (source.getSource() != null && source.getSource() == source.getAttacker()) {
                LivingEntity attackedEntity = (LivingEntity)(Object)this;
                Entity attacker = source.getAttacker();
                EntityAttributeInstance dmgReflect = attackedEntity.getAttributeInstance(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER);
                if (dmgReflect == null) return;
                int value1 = (int) dmgReflect.getValue();
                if (value1 != 100 && attacker instanceof LivingEntity livingAttacker && !attacker.getWorld().isClient) {
                    float reflectMultiplier = (float)(value1 - 100) / 100;
                    float reflectDamage = amount * reflectMultiplier;
                    if (reflectDamage != 0) {
                        livingAttacker.timeUntilRegen = 0;
                        livingAttacker.damage(source.getAttacker().getDamageSources().thorns(livingAttacker), reflectDamage);
                    }
                }
            }
        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void captureHealthBeforeApplyDamage(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        if (getLivingAttackerFromDamageSource(damageSource) != null) {
            healthBeforeDamage = ((LivingEntity)(Object)this).getHealth();
        }
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void calculateActualDamageFromHealthChange(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        if (getLivingAttackerFromDamageSource(damageSource) != null) {
            actualDamageDealt = healthBeforeDamage - ((LivingEntity)(Object)this).getHealth();
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void spellVampire$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!source.isIn(SPELL_DAMAGE_TAG)) return;
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;
        long currentTick = attacker.getWorld().getTime();
        UUID attackerId = attacker.getUuid();
        Long lastTick = SPELL_VAMPIRE_COOLDOWN.get(attackerId);
        if (lastTick != null && currentTick - lastTick < MRPGCMod.tweaksConfig.value.spellVampireCooldownTicks) return;
        EntityAttributeInstance spellVampire = attacker.getAttributeInstance(MRPGCEntityAttributes.SPELL_VAMPIRE);
        if (spellVampire != null) {
            int value = (int) spellVampire.getValue();
            if (value != 100 && attacker.getHealth() != (float) attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH)) {
                float heal = amount * ((float)(value - 100) / 100f);
                attacker.heal(heal);
                ParticleHelper.sendBatches(attacker, new ParticleBatch[]{LIFESTEAL_PARTICLES});
                SPELL_VAMPIRE_COOLDOWN.put(attackerId, currentTick);
            }
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", shift = At.Shift.AFTER))
    private void lifesteal$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;
        long currentTick = attacker.getWorld().getTime();
        UUID attackerId = attacker.getUuid();
        Long lastTick = LIFESTEAL_COOLDOWN.get(attackerId);
        if (lastTick != null && currentTick - lastTick < MRPGCMod.tweaksConfig.value.lifestealCooldownTicks) return;
        EntityAttributeInstance lifesteal = attacker.getAttributeInstance(MRPGCEntityAttributes.LIFESTEAL_MODIFIER);
        if (lifesteal != null) {
            int value = (int) lifesteal.getValue();
            if (value != 100 && attacker.getHealth() != (float) attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH)) {
                float heal = actualDamageDealt * ((float)(value - 100) / 100f);
                attacker.heal(heal);
                ParticleHelper.sendBatches(attacker, new ParticleBatch[]{LIFESTEAL_PARTICLES});
                LIFESTEAL_COOLDOWN.put(attackerId, currentTick);
            }
        }
    }

    @ModifyArgs(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void rage$addRageDamage(Args args) {
        DamageSource source = args.get(0);
        if (source.isIn(SPELL_DAMAGE_TAG)) return;
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;
        long currentTick = attacker.getWorld().getTime();
        UUID attackerId = attacker.getUuid();
        Long lastTick = RAGE_COOLDOWN.get(attackerId);
        if (lastTick != null && currentTick - lastTick < 10) return;
        EntityAttributeInstance rage = attacker.getAttributeInstance(MRPGCEntityAttributes.RAGE_MODIFIER);
        if (rage == null || rage.getValue() == 100.0) return;
        float health = attacker.getHealth();
        float maxHealth = (float) attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
        if (health < maxHealth) {
            float missing = (maxHealth - health) / maxHealth;
            EntityAttributeInstance attackDamage = attacker.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (attackDamage == null) return;
            float rageDamage = Math.max(0.1f, (float) attackDamage.getValue() * ((float)(rage.getValue() - 100) / 100f) * missing);
            args.set(1, (float) args.get(1) + rageDamage);
            RAGE_COOLDOWN.put(attackerId, currentTick);
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", shift = At.Shift.AFTER))
    private void fuse$applyFuseDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SPELL_DAMAGE_TAG)) return;
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;
        LivingEntity target = (LivingEntity)(Object)this;
        long currentTick = attacker.getWorld().getTime();
        UUID attackerId = attacker.getUuid();
        Long lastTick = FUSE_COOLDOWN.get(attackerId);
        if (lastTick != null && currentTick - lastTick < 20) return;
        FUSE_COOLDOWN.put(attackerId, currentTick);
        applyFuse(attacker, target, MRPGCEntityAttributes.AIR_FUSE_MODIFIER, MoreSpellSchools.AIR);
        applyFuse(attacker, target, MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER, SpellSchools.ARCANE);
        applyFuse(attacker, target, MRPGCEntityAttributes.EARTH_FUSE_MODIFIER, MoreSpellSchools.EARTH);
        applyFuse(attacker, target, MRPGCEntityAttributes.FIRE_FUSE_MODIFIER, SpellSchools.FIRE);
        applyFuse(attacker, target, MRPGCEntityAttributes.FROST_FUSE_MODIFIER, SpellSchools.FROST);
        applyFuse(attacker, target, MRPGCEntityAttributes.HEALING_FUSE_MODIFIER, SpellSchools.HEALING);
        applyFuse(attacker, target, MRPGCEntityAttributes.WATER_FUSE_MODIFIER, MoreSpellSchools.WATER);
    }

    @Unique
    private void applyFuse(LivingEntity attacker, LivingEntity target, net.minecraft.entity.attribute.EntityAttribute fuseAttribute, SpellSchool spellSchool) {
        EntityAttributeInstance fuseInstance = attacker.getAttributeInstance(fuseAttribute);
        if (fuseInstance == null || fuseInstance.getValue() == 100.0) return;
        EntityAttributeInstance spellPowerInstance = attacker.getAttributeInstance(spellSchool.attribute);
        if (spellPowerInstance == null) return;
        float magicDamage = Math.max(0.1f, (float)((fuseInstance.getValue() - 100) / 100f) * (float) spellPowerInstance.getValue());
        target.timeUntilRegen = 0;
        target.damage(SpellDamageSource.create(spellSchool, attacker), magicDamage);
        ParticleHelper.sendBatches(target, new ParticleBatch[]{FUSE_PARTICLES});
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", shift = At.Shift.AFTER))
    private void chanceEffects$applyOnAttack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SPELL_DAMAGE_TAG)) return;
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;

        EntityAttributeInstance burningChance = attacker.getAttributeInstance(MRPGCEntityAttributes.BURNING_CHANCE);
        EntityAttributeInstance staggerChance = attacker.getAttributeInstance(MRPGCEntityAttributes.STAGGER_CHANCE);
        EntityAttributeInstance stunChance = attacker.getAttributeInstance(MRPGCEntityAttributes.STUN_CHANCE);
        EntityAttributeInstance freezeChance = attacker.getAttributeInstance(MRPGCEntityAttributes.FREEZE_CHANCE);
        EntityAttributeInstance poisonChance = attacker.getAttributeInstance(MRPGCEntityAttributes.POISON_CHANCE);
        EntityAttributeInstance bleedingChance = attacker.getAttributeInstance(MRPGCEntityAttributes.BLEEDING_CHANCE);

        boolean anyActive = (burningChance != null && burningChance.getValue() > 100.0)
                || (staggerChance != null && staggerChance.getValue() > 100.0)
                || (stunChance != null && stunChance.getValue() > 100.0)
                || (freezeChance != null && freezeChance.getValue() > 100.0)
                || (poisonChance != null && poisonChance.getValue() > 100.0)
                || (bleedingChance != null && bleedingChance.getValue() > 100.0);
        if (!anyActive) return;

        LivingEntity target = (LivingEntity)(Object)this;
        long currentTick = attacker.getWorld().getTime();
        UUID attackerId = attacker.getUuid();
        EntityAttributeInstance attackDamageInstance = attacker.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        int amplifier = attackDamageInstance != null ? (int)(attackDamageInstance.getValue() * 0.15) : 0;
        Random random = new Random();

        Long lastStrong = STRONG_EFFECT_COOLDOWN.get(attackerId);
        if (lastStrong == null || currentTick - lastStrong >= 160) {
            if (burningChance != null && burningChance.getValue() > 100.0 && random.nextFloat() < (float)(burningChance.getValue() - 100) / 100f) {
                target.setOnFireFor(4);
                STRONG_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
            if (staggerChance != null && staggerChance.getValue() > 100.0 && random.nextFloat() < (float)(staggerChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, amplifier, true, false, true));
                STRONG_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
            if (stunChance != null && stunChance.getValue() > 100.0 && random.nextFloat() < (float)(stunChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(MRPGCEffects.STUNNED, 40, 0, true, false, true));
                STRONG_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
            if (freezeChance != null && freezeChance.getValue() > 100.0 && random.nextFloat() < (float)(freezeChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(MRPGCEffects.FROZEN_SOLID, 60, 0, true, false, true));
                STRONG_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
        }

        Long lastWeak = WEAK_EFFECT_COOLDOWN.get(attackerId);
        if (lastWeak == null || currentTick - lastWeak >= 80) {
            if (poisonChance != null && poisonChance.getValue() > 100.0 && random.nextFloat() < (float)(poisonChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 120, amplifier, true, false, true));
                WEAK_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
            if (bleedingChance != null && bleedingChance.getValue() > 100.0 && random.nextFloat() < (float)(bleedingChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(MRPGCEffects.BLEEDING, 120, amplifier, true, false, true));
                WEAK_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
        }
    }

    @Unique private static final UUID ARMOR_PIERCING_UUID = UUID.fromString("a8f6b5c2-3d4e-4f1a-9b2c-7e8d9f0a1b2c");
    @Unique private static final String ARMOR_PIERCING_NAME = "armor_piercing_reduction";

    @Inject(method = "damage", at = @At("HEAD"))
    private void armorPiercing$modifyArmor(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SPELL_DAMAGE_TAG)) return;
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;
        EntityAttributeInstance armorPiercing = attacker.getAttributeInstance(MRPGCEntityAttributes.ARMOR_PIERCING);
        if (armorPiercing != null && armorPiercing.getValue() > 100.0) {
            float piercingPercent = (float)(armorPiercing.getValue() - 100) / 100f;
            LivingEntity thisEntity = (LivingEntity)(Object)this;
            EntityAttributeInstance armorAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            EntityAttributeInstance toughnessAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            if (armorAttribute != null && toughnessAttribute != null) {
                armorAttribute.addTemporaryModifier(new EntityAttributeModifier(
                        ARMOR_PIERCING_UUID, ARMOR_PIERCING_NAME,
                        -armorAttribute.getValue() * piercingPercent, EntityAttributeModifier.Operation.ADDITION));
                toughnessAttribute.addTemporaryModifier(new EntityAttributeModifier(
                        ARMOR_PIERCING_UUID, ARMOR_PIERCING_NAME,
                        -toughnessAttribute.getValue() * piercingPercent, EntityAttributeModifier.Operation.ADDITION));
            }
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void armorPiercing$restoreArmor(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SPELL_DAMAGE_TAG)) return;
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;
        EntityAttributeInstance armorPiercing = attacker.getAttributeInstance(MRPGCEntityAttributes.ARMOR_PIERCING);
        if (armorPiercing != null && armorPiercing.getValue() > 100.0) {
            LivingEntity thisEntity = (LivingEntity)(Object)this;
            EntityAttributeInstance armorAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            EntityAttributeInstance toughnessAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            if (armorAttribute != null) armorAttribute.removeModifier(ARMOR_PIERCING_UUID);
            if (toughnessAttribute != null) toughnessAttribute.removeModifier(ARMOR_PIERCING_UUID);
        }
    }

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void tenacity$resistHarmfulEffects(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity thisEntity = (LivingEntity)(Object)this;
        if (thisEntity.getWorld().isClient()) return;
        StatusEffect effectType = effect.getEffectType();
        if (effectType.isBeneficial()) return;
        if (effectType == StatusEffects.BAD_OMEN) return;
        EntityAttributeInstance tenacityAttribute = thisEntity.getAttributeInstance(MRPGCEntityAttributes.TENACITY);
        if (tenacityAttribute == null) return;
        double resistChance = Math.max(0.0, Math.min(1.0, (tenacityAttribute.getValue() - 100.0) / 100.0));
        if (resistChance > 0 && thisEntity.getRandom().nextDouble() < resistChance) {
            cir.setReturnValue(false);
        }
    }
}
