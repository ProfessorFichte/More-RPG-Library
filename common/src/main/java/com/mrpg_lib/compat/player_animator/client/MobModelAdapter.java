package com.mrpg_lib.compat.player_animator.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModel;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface MobModelAdapter {
    @Nullable
    MobModelParts parts(EntityModel<?> model);

    default void prepare(EntityModel<?> model) {
    }

    default void finish(EntityModel<?> model) {
    }
}
