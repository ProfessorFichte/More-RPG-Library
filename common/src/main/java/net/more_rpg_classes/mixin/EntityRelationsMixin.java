package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.more_rpg_classes.entity.ISpellCasterEntity;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_engine.internals.target.SpellTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRelations.class)
public class EntityRelationsMixin {

    @Inject(at = @At("RETURN"), method = "actionAllowed", cancellable = true)
    private static void actionAllowed$mrpgCasters(SpellTarget.FocusMode focusMode, SpellTarget.Intent intent,
                                                  LivingEntity attacker, Entity target,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (!(attacker instanceof ISpellCasterEntity)) return;
        if (attacker == target) return;

        if (intent == SpellTarget.Intent.HARMFUL) {
            if (!(target instanceof HostileEntity)) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        } else {
            if (target instanceof HostileEntity) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}
