package com.mrpg_lib.compat.player_animator.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;

@Environment(EnvType.CLIENT)
public record MobModelParts(
        ModelPart head,
        ModelPart torso,
        ModelPart leftArm,
        ModelPart rightArm,
        ModelPart leftLeg,
        ModelPart rightLeg) {

    public void resetTransforms() {
        head.resetTransform();
        torso.resetTransform();
        leftArm.resetTransform();
        rightArm.resetTransform();
        leftLeg.resetTransform();
        rightLeg.resetTransform();
    }
}
