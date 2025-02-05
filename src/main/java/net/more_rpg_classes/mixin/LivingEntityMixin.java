package net.more_rpg_classes.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void mrpgc_lib$createLivingAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
                .add(MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER)
                .add(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER)
                .add(MRPGCEntityAttributes.LIFESTEAL_MODIFIER)
                .add(MRPGCEntityAttributes.RAGE_MODIFIER)
        ;
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void damageReflect$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        EntityAttributeInstance dmgReflect = ((LivingEntity) (Object) this)
                .getAttributeInstance(MRPGCEntityAttributes.DAMAGE_REFLECT_MODIFIER);
        int value1 = (int) dmgReflect.getValue();
        float reflectDamage = 0;
        if (value1 != 100) {
            value1 = value1 -100;
            reflectDamage += reflectMethod(value1, source, amount);
        }
        if (reflectDamage > 0) {
            source.getAttacker().damage(source.getAttacker().getDamageSources().thorns((PlayerEntity) (Object) this), reflectDamage);
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
}
