package com.mrpg_lib.mixin;

import com.mrpg_lib.util.UnderwaterProjectiles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileUnderwaterMixin implements UnderwaterProjectiles.Access {
    // Rides on the vanilla projectile flags, so the client knows it too and doesn't slow the arrow down on its side.
    @Unique
    private static final byte UNDERWATER_FLAG = 64;

    @Shadow @Final private static TrackedData<Byte> PROJECTILE_FLAGS;

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
    private void mrpg$markFromWeapon(CallbackInfo ci) {
        if (UnderwaterProjectiles.isWeapon(((Entity) (Object) this).getWeaponStack())) {
            mrpg$setUnderwaterProjectile(true);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void mrpg$markAfterLoad(NbtCompound nbt, CallbackInfo ci) {
        if (UnderwaterProjectiles.isWeapon(((Entity) (Object) this).getWeaponStack())) {
            mrpg$setUnderwaterProjectile(true);
        }
    }

    @Inject(method = "getDragInWater", at = @At("HEAD"), cancellable = true)
    private void mrpg$underwaterDrag(CallbackInfoReturnable<Float> cir) {
        if (mrpg$isUnderwaterProjectile()) {
            cir.setReturnValue(UnderwaterProjectiles.WATER_DRAG);
        }
    }

    @Override
    public boolean mrpg$isUnderwaterProjectile() {
        return (((Entity) (Object) this).getDataTracker().get(PROJECTILE_FLAGS) & UNDERWATER_FLAG) != 0;
    }

    @Override
    public void mrpg$setUnderwaterProjectile(boolean underwater) {
        var tracker = ((Entity) (Object) this).getDataTracker();
        byte flags = tracker.get(PROJECTILE_FLAGS);
        tracker.set(PROJECTILE_FLAGS, (byte) (underwater ? flags | UNDERWATER_FLAG : flags & ~UNDERWATER_FLAG));
    }
}
