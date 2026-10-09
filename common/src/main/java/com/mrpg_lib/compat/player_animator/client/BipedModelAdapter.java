package com.mrpg_lib.compat.player_animator.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class BipedModelAdapter implements MobModelAdapter {
    @Override
    @Nullable
    public MobModelParts parts(EntityModel<?> model) {
        if (!(model instanceof BipedEntityModel<?> biped)) return null;
        return new MobModelParts(biped.head, biped.body, biped.leftArm, biped.rightArm, biped.leftLeg, biped.rightLeg);
    }

    @Override
    public void finish(EntityModel<?> model) {
        if (!(model instanceof BipedEntityModel<?> biped)) return;
        biped.hat.copyTransform(biped.head);
        if (model instanceof PlayerEntityModel<?> player) {
            player.jacket.copyTransform(player.body);
            player.leftSleeve.copyTransform(player.leftArm);
            player.rightSleeve.copyTransform(player.rightArm);
            player.leftPants.copyTransform(player.leftLeg);
            player.rightPants.copyTransform(player.rightLeg);
        }
    }
}
