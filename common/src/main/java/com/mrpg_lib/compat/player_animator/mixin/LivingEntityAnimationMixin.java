package com.mrpg_lib.compat.player_animator.mixin;

import com.mrpg_lib.compat.player_animator.client.MobAnimatable;
import com.mrpg_lib.compat.player_animator.client.MobAnimationState;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAnimationMixin implements MobAnimatable {
    @Unique
    @Nullable
    private MobAnimationState mrpgLib$animationState;

    @Override
    public MobAnimationState mrpgLib$animationState() {
        if (mrpgLib$animationState == null) {
            mrpgLib$animationState = new MobAnimationState();
        }
        return mrpgLib$animationState;
    }

    @Override
    @Nullable
    public MobAnimationState mrpgLib$animationStateIfPresent() {
        return mrpgLib$animationState;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void mrpgLib$tickAnimation(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (mrpgLib$animationState != null && self.getWorld().isClient) {
            mrpgLib$animationState.tick(self);
        }
    }
}
