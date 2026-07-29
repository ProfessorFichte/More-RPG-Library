package net.more_rpg_classes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.more_rpg_classes.effect.StealthStatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// This code is under ARR license, permission to used it granted by Daedelus
/// https://github.com/ZsoltMolnarrr/Rogues/blob/1.21.1/src/main/java/net/rogues/mixin/LivingEntityStealth.java
@Mixin(LivingEntity.class)
public class LivingEntityStealth {
    @Unique
    private StealthStatusEffect mrpgc$activeStealthEffect() {
        var thisEntity = (LivingEntity) (Object) this;
        for (var instance : thisEntity.getStatusEffects()) {
            var effect = instance.getEffectType().value();
            if (effect instanceof StealthStatusEffect stealth) {
                return stealth;
            }
        }
        return null;
    }

    @WrapOperation(method = "updatePotionVisibility", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean updatePotionVisibility_WRAP_Stealth(LivingEntity instance, RegistryEntry<StatusEffect> effect, Operation<Boolean> original) {
        return original.call(instance, effect) || mrpgc$activeStealthEffect() != null;
    }

    @Inject(method = "getAttackDistanceScalingFactor", at = @At("RETURN"), cancellable = true)
    private void getAttackDistanceScalingFactor_RETURN_Stealth(Entity entity, CallbackInfoReturnable<Double> cir) {
        var stealth = mrpgc$activeStealthEffect();
        if (stealth != null) {
            cir.setReturnValue(cir.getReturnValue() * stealth.stealthVisibilityMultiplier());
        }
    }
}
