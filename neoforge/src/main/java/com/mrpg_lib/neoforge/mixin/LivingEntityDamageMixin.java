package com.mrpg_lib.neoforge.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Stack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
    @Shadow(remap = false) protected Stack<DamageContainer> damageContainers;

    // NeoForge's applyDamage ignores its damage argument and reads the damage container instead.
    // Without this, everything LivingEntityMixin changes on that argument does nothing here.
    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void mrpg_lib$passChangedDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (damageContainers.isEmpty()) return;
        DamageContainer container = damageContainers.peek();
        if (container.getNewDamage() != amount) {
            container.setNewDamage(amount);
        }
    }
}
