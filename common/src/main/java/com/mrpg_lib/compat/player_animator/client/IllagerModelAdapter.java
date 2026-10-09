package com.mrpg_lib.compat.player_animator.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class IllagerModelAdapter implements MobModelAdapter {
    @Override
    @Nullable
    public MobModelParts parts(EntityModel<?> model) {
        if (!(model instanceof IllagerEntityModel<?> illager)) return null;
        ModelPart root = illager.getPart();
        return new MobModelParts(
                illager.getHead(),
                root.getChild("body"),
                root.getChild("left_arm"),
                root.getChild("right_arm"),
                root.getChild("left_leg"),
                root.getChild("right_leg"));
    }

    @Override
    public void prepare(EntityModel<?> model) {
        if (!(model instanceof IllagerEntityModel<?> illager)) return;
        ModelPart root = illager.getPart();
        root.getChild("arms").visible = false;
        root.getChild("left_arm").visible = true;
        root.getChild("right_arm").visible = true;
    }

    @Override
    public void finish(EntityModel<?> model) {
        if (model instanceof IllagerEntityModel<?> illager) {
            illager.getHat().copyTransform(illager.getHead());
        }
    }
}
