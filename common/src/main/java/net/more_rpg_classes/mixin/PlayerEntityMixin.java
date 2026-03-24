package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_engine.api.effect.SpellEngineEffects;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.fx.SpellEngineParticles;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Unique private long lastFuseTick = 0;
    @Unique private long lastStrongEffectTick = 0;
    @Unique private long lastWeakEffectTick = 0;
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
    @Unique private static final ParticleBatch FUSE_PARTICLES = new ParticleBatch(
            SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPELL,
                    SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
            ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER, null,
            20, 0.3F, 0.8F, 0).color(Color.FROST.toRGBA());

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
            if(!target.getWorld().isClient()){
                ParticleHelper.sendBatches(target, new ParticleBatch[]{FUSE_PARTICLES.color(Color.from(spellSchool.color).toRGBA())});
            }
        }
    }

    @Unique
    private float calculateRageDamage() {
        PlayerEntity player = (PlayerEntity)(Object)this;
        EntityAttributeInstance rage = player.getAttributeInstance(MRPGCEntityAttributes.RAGE_MODIFIER);

        if (rage != null && rage.getValue() != 100.0) {
            float rageValue = (float) (rage.getValue() - 100) / 100f;
            float health = player.getHealth();
            float maxHealth = (float) player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);

            if (health < maxHealth) {
                float missing = (maxHealth - health) / maxHealth;
                float baseDamage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                return Math.max(0.1F, baseDamage * rageValue * missing);
            }
        }
        return 0;
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float mrpgc$addRageDamageToEntityDamage(float damage) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (!player.getWorld().isClient()) {
            float rageDamage = calculateRageDamage();
            float totalDamage = damage + rageDamage;
            return totalDamage;
        }
        return damage;
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void mrpgc$onAttackFuseMagicDamage(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        PlayerEntity player = (PlayerEntity)(Object) this;
        if (player.getWorld().isClient()) {
            return;
        }

        long currentTick = player.getWorld().getTime();
        if (currentTick - lastFuseTick < 20) {
            return;
        }
        lastFuseTick = currentTick;

        applyFuseDamage(player, livingTarget, MRPGCEntityAttributes.AIR_FUSE_MODIFIER, MoreSpellSchools.AIR);
        applyFuseDamage(player, livingTarget, MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER, SpellSchools.ARCANE);
        applyFuseDamage(player, livingTarget, MRPGCEntityAttributes.EARTH_FUSE_MODIFIER, MoreSpellSchools.EARTH);
        applyFuseDamage(player, livingTarget, MRPGCEntityAttributes.FIRE_FUSE_MODIFIER, SpellSchools.FIRE);
        applyFuseDamage(player, livingTarget, MRPGCEntityAttributes.FROST_FUSE_MODIFIER, SpellSchools.FROST);
        applyFuseDamage(player, livingTarget, MRPGCEntityAttributes.HEALING_FUSE_MODIFIER, SpellSchools.HEALING);
        applyFuseDamage(player, livingTarget, MRPGCEntityAttributes.WATER_FUSE_MODIFIER, MoreSpellSchools.WATER);
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void mrpgc$applyChanceBasedEffects(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            long currentTick = player.getWorld().getTime();
            Random random = new Random();
            float attackDamage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            int amplifier = (int)(attackDamage * 0.15);

            if (currentTick - lastStrongEffectTick >= 160) {
                EntityAttributeInstance burningChance = player.getAttributeInstance(MRPGCEntityAttributes.BURNING_CHANCE);
                if (burningChance != null && burningChance.getValue() > 100.0) {
                    float chance = (float)(burningChance.getValue() - 100) / 100f;
                    if (random.nextFloat() < chance) {
                        livingTarget.addStatusEffect(new StatusEffectInstance(
                                MRPGCEffects.IGNITED.entry, 40,amplifier,true,false,true));
                        lastStrongEffectTick = currentTick;
                    }
                }
                EntityAttributeInstance staggerChance = player.getAttributeInstance(MRPGCEntityAttributes.STAGGER_CHANCE);
                if (staggerChance != null && staggerChance.getValue() > 100.0) {
                    float chance = (float)(staggerChance.getValue() - 100) / 100f;
                    if (random.nextFloat() < chance) {
                        livingTarget.addStatusEffect(new StatusEffectInstance(
                                MRPGCEffects.STAGGER.entry, 80, amplifier,true,false,true));
                        lastStrongEffectTick = currentTick;
                    }
                }
                EntityAttributeInstance stunChance = player.getAttributeInstance(MRPGCEntityAttributes.STUN_CHANCE);
                if (stunChance != null && stunChance.getValue() > 100.0) {
                    float chance = (float)(stunChance.getValue() - 100) / 100f;
                    if (random.nextFloat() < chance) {
                        livingTarget.addStatusEffect(new StatusEffectInstance(
                                SpellEngineEffects.STUN.entry, 40, 0,true,false,true));
                        lastStrongEffectTick = currentTick;
                    }
                }
                EntityAttributeInstance freezeChance = player.getAttributeInstance(MRPGCEntityAttributes.FREEZE_CHANCE);
                if (freezeChance != null && freezeChance.getValue() > 100.0) {
                    float chance = (float)(freezeChance.getValue() - 100) / 100f;
                    if (random.nextFloat() < chance) {
                        livingTarget.addStatusEffect(new StatusEffectInstance(
                                MRPGCEffects.FROZEN_SOLID.entry, 60, 0,true,false,true));
                        lastStrongEffectTick = currentTick;
                        if(!livingTarget.getWorld().isClient()){
                            ParticleHelper.sendBatches(livingTarget, new ParticleBatch[]{FREEZE_PARTICLES});
                        }
                    }
                }
            }

            if (currentTick - lastWeakEffectTick >= 80) {
                EntityAttributeInstance poisonChance = player.getAttributeInstance(MRPGCEntityAttributes.POISON_CHANCE);
                if (poisonChance != null && poisonChance.getValue() > 100.0) {
                    float chance = (float)(poisonChance.getValue() - 100) / 100f;
                    if (random.nextFloat() < chance) {
                        livingTarget.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.POISON, 120, amplifier,true,false,true));
                        lastWeakEffectTick = currentTick;
                        if(!livingTarget.getWorld().isClient()){
                            ParticleHelper.sendBatches(livingTarget, new ParticleBatch[]{POISON_PARTICLES});
                        }
                    }
                }
                EntityAttributeInstance bleedingChance = player.getAttributeInstance(MRPGCEntityAttributes.BLEEDING_CHANCE);
                if (bleedingChance != null && bleedingChance.getValue() > 100.0) {
                    float chance = (float)(bleedingChance.getValue() - 100) / 100f;
                    if (random.nextFloat() < chance) {
                        livingTarget.addStatusEffect(new StatusEffectInstance(
                                MRPGCEffects.BLEEDING.entry, 120, amplifier,true,false,true));
                        lastWeakEffectTick = currentTick;
                        if(!livingTarget.getWorld().isClient()){
                            ParticleHelper.sendBatches(livingTarget, new ParticleBatch[]{BLEEDING_PARTICLES});
                        }
                    }
                }
            }
        }
    }
}
