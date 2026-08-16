package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.more_rpg_classes.effect.ControlEnemyStatusEffect;
import net.more_rpg_classes.entity.ControlledOwnerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class ControlOwnerMixin implements ControlledOwnerAccess {
    @Unique
    private UUID mrpg$controlOwner;

    @Override
    public UUID mrpg$getControlOwner() {
        return mrpg$controlOwner;
    }

    @Override
    public void mrpg$setControlOwner(UUID owner) {
        this.mrpg$controlOwner = owner;
    }

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"))
    private void mrpg$captureControlOwner(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getWorld().isClient()) return;
        if (!(effect.getEffectType().value() instanceof ControlEnemyStatusEffect)) return;
        if (source instanceof LivingEntity owner) {
            this.mrpg$controlOwner = owner.getUuid();
        }
    }
}
