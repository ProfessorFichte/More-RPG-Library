package com.mrpg_lib.compat.spell_power.mixin;

import com.mrpg_lib.compat.spell_power.DynamicSpellSchool;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SpellDamageSource.class, remap = false)
public class SpellDamageSourceMixin {
    @Inject(method = "player", at = @At("HEAD"), cancellable = true)
    private static void mrpg_lib$dynamicPlayerDamage(SpellSchool school, PlayerEntity attacker, CallbackInfoReturnable<DamageSource> cir) {
        if (school == DynamicSpellSchool.DYNAMIC) {
            cir.setReturnValue(SpellDamageSource.player(DynamicSpellSchool.resolve(attacker), attacker));
        }
    }

    @Inject(method = "mob", at = @At("HEAD"), cancellable = true)
    private static void mrpg_lib$dynamicMobDamage(SpellSchool school, LivingEntity attacker, CallbackInfoReturnable<DamageSource> cir) {
        if (school == DynamicSpellSchool.DYNAMIC) {
            cir.setReturnValue(SpellDamageSource.mob(DynamicSpellSchool.resolve(attacker), attacker));
        }
    }
}
