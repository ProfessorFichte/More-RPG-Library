package com.mrpg_lib.compat.player_animator.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface MobAnimatable {
    MobAnimationState mrpgLib$animationState();

    @Nullable
    MobAnimationState mrpgLib$animationStateIfPresent();
}
