package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.more_rpg_classes.effect.ControlEnemyStatusEffect;
import net.more_rpg_classes.entity.ControlledOwnerAccess;
import net.more_rpg_classes.entity.ISpellCasterEntity;
import net.spell_engine.internals.target.EntityRelation;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_engine.internals.target.SpellTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

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

    @Inject(at = @At("RETURN"), method = "actionAllowed", cancellable = true)
    private static void actionAllowed$mrpgControlledCaster(SpellTarget.FocusMode focusMode, SpellTarget.Intent intent,
                                                           LivingEntity attacker, Entity target,
                                                           CallbackInfoReturnable<Boolean> cir) {
        if (attacker == target) return;
        if (ControlEnemyStatusEffect.isControlled(attacker)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("RETURN"), method = "actionAllowed", cancellable = true)
    private static void actionAllowed$mrpgProtectControlledFromOwner(SpellTarget.FocusMode focusMode, SpellTarget.Intent intent,
                                                                       LivingEntity attacker, Entity target,
                                                                       CallbackInfoReturnable<Boolean> cir) {
        if (intent != SpellTarget.Intent.HARMFUL) return;
        if (attacker == target || !(target instanceof LivingEntity controlled)) return;
        if (!ControlEnemyStatusEffect.isControlled(controlled)) return;
        if (!(controlled instanceof ControlledOwnerAccess access)) return;

        UUID ownerId = access.mrpg$getControlOwner();
        if (ownerId == null) return;
        if (attacker.getUuid().equals(ownerId)) {
            cir.setReturnValue(false);
            return;
        }

        if (!(controlled.getWorld() instanceof ServerWorld serverWorld)) return;
        if (!(serverWorld.getEntity(ownerId) instanceof LivingEntity owner)) return;
        var relation = EntityRelations.getRelation(owner, attacker);
        if (relation == EntityRelation.ALLY || relation == EntityRelation.FRIENDLY) {
            cir.setReturnValue(false);
        }
    }
}
