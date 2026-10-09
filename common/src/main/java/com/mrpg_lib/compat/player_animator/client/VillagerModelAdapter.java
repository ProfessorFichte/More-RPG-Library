package com.mrpg_lib.compat.player_animator.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class VillagerModelAdapter implements MobModelAdapter {
    private static final ModelTransform PLAYER_LEFT_ARM = ModelTransform.pivot(5.0F, 2.0F, 0.0F);
    private static final ModelTransform PLAYER_RIGHT_ARM = ModelTransform.pivot(-5.0F, 2.0F, 0.0F);

    @Override
    @Nullable
    public MobModelParts parts(EntityModel<?> model) {
        if (!(model instanceof VillagerResemblingModel<?> villager)) return null;
        ModelPart root = villager.getPart();
        return new MobModelParts(
                villager.getHead(),
                root.getChild("body"),
                proxyArm(PLAYER_LEFT_ARM),
                proxyArm(PLAYER_RIGHT_ARM),
                root.getChild("left_leg"),
                root.getChild("right_leg"));
    }

    @Override
    public void prepare(EntityModel<?> model) {
        if (model instanceof VillagerResemblingModel<?> villager) {
            villager.getPart().getChild("arms").resetTransform();
        }
    }

    @Override
    public void finish(EntityModel<?> model) {
        if (!(model instanceof VillagerResemblingModel<?> villager)) return;
        var binding = MobModelAdapters.resolve(model);
        if (binding == null) return;
        ModelPart proxy = binding.parts().rightArm();
        ModelPart arms = villager.getPart().getChild("arms");
        ModelTransform rest = arms.getDefaultTransform();
        arms.pivotX = rest.pivotX + proxy.pivotX - PLAYER_RIGHT_ARM.pivotX;
        arms.pivotY = rest.pivotY + proxy.pivotY - PLAYER_RIGHT_ARM.pivotY;
        arms.pivotZ = rest.pivotZ + proxy.pivotZ - PLAYER_RIGHT_ARM.pivotZ;
        arms.pitch = rest.pitch + proxy.pitch;
        arms.yaw = rest.yaw + proxy.yaw;
        arms.roll = rest.roll + proxy.roll;
    }

    private static ModelPart proxyArm(ModelTransform rest) {
        ModelPart part = new ModelPart(List.of(), Map.of());
        part.setDefaultTransform(rest);
        part.resetTransform();
        return part;
    }
}
