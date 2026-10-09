package com.mrpg_lib.compat.spell_engine.mixin;

import com.mrpg_lib.compat.spell_engine.client.render.MobSpinTracker;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererSpinMixin {

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void mrpg$spinCastingMob(LivingEntity entity, float yaw, float delta, MatrixStack matrices,
                                     VertexConsumerProvider consumers, int light, CallbackInfo ci) {
        float degrees = MobSpinTracker.degrees(entity, delta);
        if (degrees != 0F) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(degrees));
        }
    }
}
