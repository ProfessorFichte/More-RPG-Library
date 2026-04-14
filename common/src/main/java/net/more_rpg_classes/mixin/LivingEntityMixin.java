package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_engine.api.effect.SpellEngineEffects;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.fx.SpellEngineParticles;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellPowerTags;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

    @Unique private float actualDamageDealt = 0;
    @Unique private float healthBeforeDamage = 0;

    @Unique private static final Map<UUID, Long> SPELL_VAMPIRE_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> LIFESTEAL_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> RAGE_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> FUSE_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> STRONG_EFFECT_COOLDOWN = new HashMap<>();
    @Unique private static final Map<UUID, Long> WEAK_EFFECT_COOLDOWN = new HashMap<>();

    @Unique private static final ParticleBatch LIFESTEAL_PARTICLES = new ParticleBatch(
            SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.STRIPE,
                    SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString(),
            ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET, null,
            20, 0.18F, 0.5F, 0).color(Color.RED.toRGBA());
    @Unique private static final ParticleBatch FUSE_PARTICLES = new ParticleBatch(
            SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPELL,
                    SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
            ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER, null,
            20, 0.3F, 0.8F, 0).color(Color.FROST.toRGBA());
    @Unique private static final ParticleBatch BLEEDING_PARTICLES = new ParticleBatch(
            SpellEngineParticles.dripping_blood.id().toString(),
            ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER, null,
            20, 0.3F, 0.8F, 0).color(Color.RED.toRGBA());
    @Unique private static final ParticleBatch POISON_PARTICLES = new ParticleBatch(
            SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SKULL,
                    SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString(),
            ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER, null,
            20, 0.3F, 0.8F, 0).color(Color.GREEN.toRGBA());
    @Unique private static final ParticleBatch FREEZE_PARTICLES = new ParticleBatch(
            SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.FROST,
                    SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
            ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER, null,
            20, 0.3F, 0.8F, 0).color(Color.FROST.toRGBA());

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
                .add(MRPGCEntityAttributes.TENACITY)
        ;
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void damageReflect$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!DamageTypes.THORNS.equals(source.getType()) && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (source.isDirect()) {
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
        if (!source.isIn(SpellPowerTags.DamageTypes.ALL)) return;
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
        if (source.isIn(SpellPowerTags.DamageTypes.ALL)) return;
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

    @ModifyArgs(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void duelistsFocus$reduceDamage(Args args) {
        LivingEntity thisEntity = (LivingEntity)(Object)this;
        if (!hasStatusEffect(MRPGCEffects.DUELISTS_FOCUS_OWNER.entry)) return;
        if (thisEntity.getWorld().isClient()) return;
        DamageSource source = args.get(0);
        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity livingAttacker && !livingAttacker.hasStatusEffect(MRPGCEffects.DUELISTS_FOCUS_TARGET.entry)) {
            args.set(1, (float) args.get(1) * 0.75F);
        }
    }

    @ModifyArgs(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void duelistsFocus$increaseDamageToTarget(Args args) {
        LivingEntity thisEntity = (LivingEntity)(Object)this;
        if (!thisEntity.hasStatusEffect(MRPGCEffects.DUELISTS_FOCUS_TARGET.entry)) return;
        if (thisEntity.getWorld().isClient()) return;
        DamageSource source = args.get(0);
        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity livingAttacker && livingAttacker.hasStatusEffect(MRPGCEffects.DUELISTS_FOCUS_OWNER.entry)) {
            args.set(1, (float) args.get(1) * 1.25F);
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", shift = At.Shift.AFTER))
    private void fuse$applyFuseDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SpellPowerTags.DamageTypes.ALL)) return;
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
    private void applyFuse(LivingEntity attacker, LivingEntity target, RegistryEntry<EntityAttribute> fuseAttribute, SpellSchool spellSchool) {
        EntityAttributeInstance fuseInstance = attacker.getAttributeInstance(fuseAttribute);
        if (fuseInstance == null || fuseInstance.getValue() == 100.0) return;
        EntityAttributeInstance spellPowerInstance = attacker.getAttributeInstance(spellSchool.attributeEntry);
        if (spellPowerInstance == null) return;
        float magicDamage = Math.max(0.1f, (float)((fuseInstance.getValue() - 100) / 100f) * (float) spellPowerInstance.getValue());
        target.timeUntilRegen = 0;
        target.damage(SpellDamageSource.create(spellSchool, attacker), magicDamage);
        ParticleHelper.sendBatches(target, new ParticleBatch[]{FUSE_PARTICLES.color(Color.from(spellSchool.color).toRGBA())});
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", shift = At.Shift.AFTER))
    private void chanceEffects$applyOnAttack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SpellPowerTags.DamageTypes.ALL)) return;
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
                target.addStatusEffect(new StatusEffectInstance(MRPGCEffects.IGNITED.entry, 40, amplifier, true, false, true));
                STRONG_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
            if (staggerChance != null && staggerChance.getValue() > 100.0 && random.nextFloat() < (float)(staggerChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(MRPGCEffects.STAGGER.entry, 80, amplifier, true, false, true));
                STRONG_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
            if (stunChance != null && stunChance.getValue() > 100.0 && random.nextFloat() < (float)(stunChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(SpellEngineEffects.STUN.entry, 40, 0, true, false, true));
                STRONG_EFFECT_COOLDOWN.put(attackerId, currentTick);
            }
            if (freezeChance != null && freezeChance.getValue() > 100.0 && random.nextFloat() < (float)(freezeChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(MRPGCEffects.FROZEN_SOLID.entry, 60, 0, true, false, true));
                STRONG_EFFECT_COOLDOWN.put(attackerId, currentTick);
                ParticleHelper.sendBatches(target, new ParticleBatch[]{FREEZE_PARTICLES});
            }
        }

        Long lastWeak = WEAK_EFFECT_COOLDOWN.get(attackerId);
        if (lastWeak == null || currentTick - lastWeak >= 80) {
            if (poisonChance != null && poisonChance.getValue() > 100.0 && random.nextFloat() < (float)(poisonChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 120, amplifier, true, false, true));
                WEAK_EFFECT_COOLDOWN.put(attackerId, currentTick);
                ParticleHelper.sendBatches(target, new ParticleBatch[]{POISON_PARTICLES});
            }
            if (bleedingChance != null && bleedingChance.getValue() > 100.0 && random.nextFloat() < (float)(bleedingChance.getValue() - 100) / 100f) {
                target.addStatusEffect(new StatusEffectInstance(MRPGCEffects.BLEEDING.entry, 120, amplifier, true, false, true));
                WEAK_EFFECT_COOLDOWN.put(attackerId, currentTick);
                ParticleHelper.sendBatches(target, new ParticleBatch[]{BLEEDING_PARTICLES});
            }
        }
    }

    @Unique
    private static final net.minecraft.util.Identifier ARMOR_PIERCING_ID = net.minecraft.util.Identifier.of("more_rpg_classes", "armor_piercing_reduction");

    @Inject(method = "damage", at = @At("HEAD"))
    private void armorPiercing$modifyArmor(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SpellPowerTags.DamageTypes.ALL)) return;
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;
        EntityAttributeInstance armorPiercing = attacker.getAttributeInstance(MRPGCEntityAttributes.ARMOR_PIERCING);
        if (armorPiercing != null && armorPiercing.getValue() > 100.0) {
            float piercingPercent = (float)(armorPiercing.getValue() - 100) / 100f;
            LivingEntity thisEntity = (LivingEntity)(Object)this;
            EntityAttributeInstance armorAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            EntityAttributeInstance toughnessAttribute = thisEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            if (armorAttribute != null && toughnessAttribute != null) {
                armorAttribute.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                        ARMOR_PIERCING_ID, -armorAttribute.getValue() * piercingPercent, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
                toughnessAttribute.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                        ARMOR_PIERCING_ID, -toughnessAttribute.getValue() * piercingPercent, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void armorPiercing$restoreArmor(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(SpellPowerTags.DamageTypes.ALL)) return;
        LivingEntity attacker = getLivingAttackerFromDamageSource(source);
        if (attacker == null || attacker.getWorld().isClient()) return;
        EntityAttributeInstance armorPiercing = attacker.getAttributeInstance(MRPGCEntityAttributes.ARMOR_PIERCING);
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

    @Inject(method = "baseTick", at = @At("TAIL"))
    public void baseTickPowderSnowFrostedSolidEffect(CallbackInfo ci) {
        var entity = (LivingEntity)((Object)this);
        entity.inPowderSnow = entity.inPowderSnow || hasStatusEffect(MRPGCEffects.FROSTED.entry);
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    public void baseTickPowderSnowFrozenSolidEffect(CallbackInfo ci) {
        var entity = (LivingEntity)((Object)this);
        entity.inPowderSnow = entity.inPowderSnow || hasStatusEffect(MRPGCEffects.FROZEN_SOLID.entry);
    }

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void tenacity$resistHarmfulEffects(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity thisEntity = (LivingEntity)(Object)this;
        if (thisEntity.getWorld().isClient()) return;
        if (effect.getEffectType().value().isBeneficial()) return;
        RegistryEntry<StatusEffect> effectType = effect.getEffectType();
        if (effectType.matchesKey(StatusEffects.BAD_OMEN.getKey().get()) ||
            effectType.matchesKey(StatusEffects.TRIAL_OMEN.getKey().get()) ||
            effectType.matchesKey(StatusEffects.RAID_OMEN.getKey().get())) return;
        EntityAttributeInstance tenacityAttribute = thisEntity.getAttributeInstance(MRPGCEntityAttributes.TENACITY);
        if (tenacityAttribute == null) return;
        double resistChance = Math.max(0.0, Math.min(1.0, (tenacityAttribute.getValue() - 100.0) / 100.0));
        if (resistChance > 0 && thisEntity.getRandom().nextDouble() < resistChance) {
            cir.setReturnValue(false);
        }
    }
}
