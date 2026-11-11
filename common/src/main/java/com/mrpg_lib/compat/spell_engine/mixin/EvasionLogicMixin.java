package com.mrpg_lib.compat.spell_engine.mixin;

import com.mrpg_lib.entity.ISpellCasterEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.spell_engine.SpellEngineMod;
import net.spell_engine.api.entity.EvasionLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EvasionLogic.class, remap = false)
public class EvasionLogicMixin {

    @Inject(method = "tryEvade", at = @At("HEAD"), cancellable = true)
    private static void tryEvade$mrpgCastingMob(LivingEntity entity, float amount, DamageSource source,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ISpellCasterEntity caster && caster.isMobCasting()
                && !SpellEngineMod.config.attribute_evasion_allowed_while_spell_casting) {
            cir.setReturnValue(false);
        }
    }
}
