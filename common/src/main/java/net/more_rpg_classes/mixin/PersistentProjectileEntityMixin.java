package net.more_rpg_classes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.more_rpg_classes.entity.IProjectileAttributeStorage;
import net.more_rpg_classes.entity.attribute.ProjectileAttributeData;
import net.more_rpg_classes.util.ProjectileEffectApplicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements IProjectileAttributeStorage {

    @Unique
    private ProjectileAttributeData mrpgc$attributeData = null;

    @Override
    public void mrpgc$setAttributeData(ProjectileAttributeData data) {
        this.mrpgc$attributeData = data;
    }

    @Override
    public ProjectileAttributeData mrpgc$getAttributeData() {
        return this.mrpgc$attributeData;
    }

    @Override
    public boolean mrpgc$hasAttributeData() {
        return this.mrpgc$attributeData != null;
    }

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void mrpgc$captureShooterAttributes(Entity owner, CallbackInfo ci) {
        if (owner instanceof PlayerEntity player && !player.getWorld().isClient()) {
            this.mrpgc$attributeData = ProjectileAttributeData.fromPlayer(player);
        }
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void mrpgc$applyAttributeEffectsOnHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (this.mrpgc$attributeData == null) {
            return;
        }

        Entity hitEntity = entityHitResult.getEntity();
        if (!(hitEntity instanceof LivingEntity target)) {
            return;
        }

        PersistentProjectileEntity projectile = (PersistentProjectileEntity)(Object)this;
        Entity shooter = projectile.getOwner();

        if (shooter == null || projectile.getWorld().isClient()) {
            return;
        }

        ProjectileEffectApplicator.applyEffects(
            this.mrpgc$attributeData,
            target,
            shooter,
            projectile.getWorld()
        );
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void mrpgc$writeAttributeDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.mrpgc$attributeData != null) {
            NbtCompound attributeNbt = new NbtCompound();
            this.mrpgc$attributeData.writeToNbt(attributeNbt);
            nbt.put("mrpgc_attribute_data", attributeNbt);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void mrpgc$readAttributeDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("mrpgc_attribute_data")) {
            NbtCompound attributeNbt = nbt.getCompound("mrpgc_attribute_data");
            this.mrpgc$attributeData = ProjectileAttributeData.readFromNbt(attributeNbt);
        }
    }
}
