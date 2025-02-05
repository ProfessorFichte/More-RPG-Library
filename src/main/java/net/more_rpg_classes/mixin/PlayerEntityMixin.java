package net.more_rpg_classes.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_power.api.SpellSchools;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow @Final private static Logger LOGGER;

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
}
