package com.mrpg_lib.compat.spell_engine.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrpg_lib.compat.spell_engine.MobArrowContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSkeletonEntity.class)
public class AbstractSkeletonEntityMixin {

    @WrapOperation(method = "shootAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean mrpg$mobArrowSpells(World world, Entity entity, Operation<Boolean> original) {
        if (entity instanceof ProjectileEntity projectile) {
            MobArrowContext.onArrowCreated((AbstractSkeletonEntity) (Object) this, projectile, false);
        }
        return original.call(world, entity);
    }
}
