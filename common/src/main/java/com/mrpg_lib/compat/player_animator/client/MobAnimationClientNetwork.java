package com.mrpg_lib.compat.player_animator.client;

import com.mrpg_lib.compat.player_animator.network.MobAnimationPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class MobAnimationClientNetwork {
    private MobAnimationClientNetwork() {
    }

    public static void handle(MobAnimationPacket packet, @Nullable World world) {
        if (world == null) return;
        if (!(world.getEntityById(packet.entityId()) instanceof LivingEntity entity)
                || entity instanceof AbstractClientPlayerEntity
                || !(entity instanceof MobAnimatable animatable)) {
            return;
        }
        MobAnimationPlayer.apply(entity, animatable.mrpgLib$animationState(), packet);
    }
}
