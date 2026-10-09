package com.mrpg_lib.compat.spell_engine.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.spell_engine.entity.SummonedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SummonedEntity.class)
public abstract class SummonedEntityOwnerMixin {

    @Inject(method = "getOwner", at = @At("RETURN"), cancellable = true)
    private void mrpg$resolveMobOwner(CallbackInfoReturnable<LivingEntity> cir) {
        if (cir.getReturnValue() != null) return;
        SummonedEntity self = (SummonedEntity) (Object) this;
        var ownerUuid = self.getOwnerUuid();
        if (ownerUuid == null || !(self.getWorld() instanceof ServerWorld world)) return;
        if (world.getEntity(ownerUuid) instanceof LivingEntity owner && owner.isAlive()) {
            cir.setReturnValue(owner);
        }
    }
}
