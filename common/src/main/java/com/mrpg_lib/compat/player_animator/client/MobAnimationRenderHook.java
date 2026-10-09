package com.mrpg_lib.compat.player_animator.client;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class MobAnimationRenderHook {
    private static final double BODY_PIVOT_HEIGHT = 0.7;
    private static final float HELD_ITEM_POSITION_SCALE = 0.0625F;

    private MobAnimationRenderHook() {
    }

    public static void beforeAngles(LivingEntity entity, EntityModel<?> model, float tickDelta) {
        MobAnimationState state = renderState(entity);
        if (state == null) return;
        var binding = MobModelAdapters.resolve(model);
        if (binding == null) return;
        state.applier().setTickDelta(tickDelta);
        binding.parts().resetTransforms();
    }

    public static void afterAngles(LivingEntity entity, EntityModel<?> model) {
        MobAnimationState state = renderState(entity);
        if (state == null) return;
        var binding = MobModelAdapters.resolve(model);
        if (binding == null) return;
        var applier = state.applier();
        var parts = binding.parts();
        binding.adapter().prepare(model);
        applier.updatePart("head", parts.head());
        applier.updatePart("leftArm", parts.leftArm());
        applier.updatePart("rightArm", parts.rightArm());
        applier.updatePart("leftLeg", parts.leftLeg());
        applier.updatePart("rightLeg", parts.rightLeg());
        applier.updatePart("torso", parts.torso());
        binding.adapter().finish(model);
        state.markApplied();
    }

    public static void applyBodyTransform(LivingEntity entity, MatrixStack matrices, float tickDelta) {
        MobAnimationState state = renderState(entity);
        if (state == null || !state.isActive()) return;
        var applier = state.applier();
        applier.setTickDelta(tickDelta);
        Vec3f scale = applier.get3DTransform("body", TransformType.SCALE,
                new Vec3f(1.0F, 1.0F, 1.0F));
        matrices.scale(scale.getX(), scale.getY(), scale.getZ());
        Vec3f position = applier.get3DTransform("body", TransformType.POSITION, Vec3f.ZERO);
        matrices.translate(position.getX(), position.getY() + BODY_PIVOT_HEIGHT, position.getZ());
        Vec3f rotation = applier.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(rotation.getZ()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rotation.getY()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(rotation.getX()));
        matrices.translate(0.0, -BODY_PIVOT_HEIGHT, 0.0);
    }

    public static void applyHeldItemTransform(LivingEntity entity, Arm arm, MatrixStack matrices) {
        MobAnimationState state = renderState(entity);
        if (state == null || !state.isActive()) return;
        var applier = state.applier();
        String part = arm == Arm.LEFT ? "leftItem" : "rightItem";
        Vec3f scale = applier.get3DTransform(part, TransformType.SCALE, new Vec3f(1.0F, 1.0F, 1.0F));
        Vec3f rotation = applier.get3DTransform(part, TransformType.ROTATION, Vec3f.ZERO);
        Vec3f position = applier.get3DTransform(part, TransformType.POSITION, Vec3f.ZERO).scale(HELD_ITEM_POSITION_SCALE);
        matrices.scale(scale.getX(), scale.getY(), scale.getZ());
        matrices.translate(position.getX(), position.getY(), position.getZ());
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(rotation.getZ()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rotation.getY()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(rotation.getX()));
    }

    @Nullable
    private static MobAnimationState renderState(LivingEntity entity) {
        if (entity instanceof AbstractClientPlayerEntity || !(entity instanceof MobAnimatable animatable)) {
            return null;
        }
        MobAnimationState state = animatable.mrpgLib$animationStateIfPresent();
        return state != null && state.needsRender() ? state : null;
    }
}
