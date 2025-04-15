package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {


    @Inject(method = "createPlayerAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;", require = 1, allow = 1, at = @At("RETURN"))
    private static void moreEntityAttributes$addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        info.getReturnValue().add(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER);
        info.getReturnValue().add(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER);
        info.getReturnValue().add(MRPGCEntityAttributes.LIFESTEAL_MODIFIER);
        info.getReturnValue().add(MRPGCEntityAttributes.RAGE_MODIFIER);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D", ordinal = 0))
    public double modifyBaseDamage(PlayerEntity instance, EntityAttribute entityAttribute, Entity target) {
        //LIFESTEAL
        float damage = (float) instance.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        EntityAttributeInstance lifesteal = ((LivingEntity) (Object) this).getAttributeInstance(MRPGCEntityAttributes.LIFESTEAL_MODIFIER);
        int value3 = (int) lifesteal.getValue();
            if ((instance instanceof ServerPlayerEntity) && (target instanceof LivingEntity)) {
                if(value3 != 100){
                    value3 = value3 -100;
                    float heal = (damage * ((float) value3 /100));
                    instance.heal(heal);
                    return damage;
                }
            }
        return damage;
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float modifyDamage(float damage) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        //RAGE
        EntityAttributeInstance ragedmg = ((LivingEntity) (Object) this).getAttributeInstance(MRPGCEntityAttributes.RAGE_MODIFIER);
        float value1 = (float) (ragedmg.getValue()-100) / 100;
        float actual_health = player.getHealth();
        float max_health = (float) player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
        float missing_health_percentage = (max_health - actual_health) / max_health;
        if (value1 != 0 && actual_health != max_health){
            return damage + (damage * (value1 * missing_health_percentage));
        }
        //ARCANE_FUSE
        EntityAttributeInstance arcanefuse = ((LivingEntity) (Object) this).getAttributeInstance(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER);
        int value2 = (int) arcanefuse.getValue();
        if(value2 != 100){
            return (float) (damage + (((arcanefuse.getValue()-100)/100) * (float) player.getAttributeValue(SpellSchools.ARCANE.attribute)));
        }
        return damage;
    }
}
