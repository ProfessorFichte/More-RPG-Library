package net.more_rpg_classes.mixin;

import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.more_rpg_classes.effect.StealthStatusEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// This code is under ARR license, permission to used it granted by Daedelus
/// https://github.com/ZsoltMolnarrr/Rogues/blob/1.21.1/src/main/java/net/rogues/mixin/TrackTargetGoalStealth.java
@Mixin(TrackTargetGoal.class)
public class TrackTargetGoalStealth {
    @Shadow
    @Final
    protected MobEntity mob;

    @Inject(method = "getFollowRange", at = @At("HEAD"), cancellable = true)
    private void getFollowRange_HEAD_Stealth(CallbackInfoReturnable<Double> cir) {
        var target = mob.getTarget();
        if (target == null) return;
        for (var instance : target.getStatusEffects()) {
            var effect = instance.getEffectType().value();
            if (effect instanceof StealthStatusEffect stealth) {
                cir.setReturnValue(stealth.stealthFollowRange());
                cir.cancel();
                return;
            }
        }
    }
}
