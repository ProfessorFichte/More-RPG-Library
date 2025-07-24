package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class, priority = 15000)
public abstract class PlayerEntityMixin {

    @Inject(method = "createPlayerAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;", at = @At("RETURN"))
    private static void moreEntityAttributes$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        info.getReturnValue()
                .add(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER)
                .add(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.LIFESTEAL_MODIFIER)
                .add(MRPGCEntityAttributes.RAGE_MODIFIER);
    }

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
    private void mrpgc$boostDamageOnAttack(Entity target, CallbackInfo ci) {
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
    private void mrpgc$onAttackArcaneFuse(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object) this;
        if (target instanceof LivingEntity livingTarget && !player.getWorld().isClient()) {
            EntityAttributeInstance arcaneFuse = player.getAttributeInstance(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER);
            if (arcaneFuse != null && arcaneFuse.getValue() != 100.0) {
                float arcaneBonus = (float) ((arcaneFuse.getValue() - 100) / 100f);
                float arcanePower = (float) player.getAttributeValue(SpellSchools.ARCANE.attribute);
                float magicDamage = Math.max(0.1f, arcaneBonus * arcanePower);
                DamageSource arcaneSource = player.getDamageSources().magic(); // fallback
                livingTarget.timeUntilRegen = 0;
                livingTarget.damage(SpellDamageSource.create(SpellSchools.ARCANE, player), magicDamage);
            }
        }
    }




    /*
    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
    )
    private boolean mrpgc$redirectDamage(Entity target, DamageSource source, float originalAmount) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        float modifiedAmount = originalAmount;

        // RAGE bonus
        EntityAttributeInstance rage = player.getAttributeInstance(MRPGCEntityAttributes.RAGE_MODIFIER);
        if (rage != null) {
            float rageValue = (float) (rage.getValue() - 100) / 100f;
            float currentHealth = player.getHealth();
            float maxHealth = (float) player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
            float missingHealth = (maxHealth - currentHealth) / maxHealth;
            if (rageValue != 0 && currentHealth < maxHealth) {
                modifiedAmount += originalAmount * (rageValue * missingHealth);
            }
        }

        // ARCANE FUSE bonus
        EntityAttributeInstance arcaneFuse = player.getAttributeInstance(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER);
        if (arcaneFuse != null && arcaneFuse.getValue() != 100.0) {
            float arcaneBonus = (float) ((arcaneFuse.getValue() - 100) / 100f);
            float arcaneAttribute = (float) player.getAttributeValue(SpellSchools.ARCANE.attribute);
            modifiedAmount += arcaneBonus * arcaneAttribute;
        }

        return target.damage(source, modifiedAmount);
    }

     */
}
