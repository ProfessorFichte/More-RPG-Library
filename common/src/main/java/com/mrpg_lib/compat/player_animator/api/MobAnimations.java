package com.mrpg_lib.compat.player_animator.api;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.player_animator.network.MobAnimationPacket;
import com.mrpg_lib.network.MRPGCNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class MobAnimations {
    private static final int SPELL_STOP_FADE_TICKS = 5;
    private static final int WEAPON_POSE_FADE_TICKS = 5;

    private static final Map<LivingEntity, EnumMap<MobAnimationLayer, MobAnimationPacket>> PERSISTENT = new WeakHashMap<>();

    private MobAnimations() {
    }

    public static boolean isAvailable() {
        return MrpgCompat.PLAYER_ANIMATOR;
    }

    public static void play(LivingEntity mob, Identifier animationId) {
        play(mob, animationId, MobAnimationOptions.DEFAULT);
    }

    public static void play(LivingEntity mob, Identifier animationId, MobAnimationOptions options) {
        if (!canAnimate(mob)) return;
        send(mob, new MobAnimationPacket(mob.getId(), animationId, options.layer(), options.speed(),
                Math.max(-1, options.fadeTicks()), options.mirror(), options.length(), options.upswingRate(), options.upswingMultiplier()),
                options.persistent());
    }

    public static void playWeaponPose(LivingEntity mob, MobAnimationLayer layer, @Nullable Identifier poseId, boolean twoHanded) {
        if (!canAnimate(mob)) return;
        if (poseId == null) {
            stop(mob, layer, WEAPON_POSE_FADE_TICKS);
            return;
        }
        var style = twoHanded ? MobAnimationOptions.PoseStyle.WEAPON_TWO_HANDED : MobAnimationOptions.PoseStyle.WEAPON;
        send(mob, new MobAnimationPacket(mob.getId(), poseId, layer, 1.0F, WEAPON_POSE_FADE_TICKS,
                MobAnimationOptions.Mirror.AUTO, 0.0F, 0.0F, 0.0F, style), true);
    }

    private static void send(LivingEntity mob, MobAnimationPacket packet, boolean persistent) {
        if (persistent) {
            PERSISTENT.computeIfAbsent(mob, key -> new EnumMap<>(MobAnimationLayer.class)).put(packet.layer(), packet);
        } else {
            forget(mob, packet.layer());
        }
        MRPGCNetworking.sendToTracking(mob, packet);
    }

    public static void stop(LivingEntity mob, MobAnimationLayer layer, int fadeTicks) {
        if (!canAnimate(mob)) return;
        forget(mob, layer);
        MRPGCNetworking.sendToTracking(mob, stopPacket(mob, layer, fadeTicks));
    }

    public static void stopAll(LivingEntity mob, int fadeTicks) {
        for (MobAnimationLayer layer : MobAnimationLayer.values()) {
            stop(mob, layer, fadeTicks);
        }
    }

    public static void playSpellAnimation(LivingEntity mob, MobAnimationLayer layer, @Nullable String animationName, float speed) {
        Identifier id = animationName == null || animationName.isEmpty() ? null : Identifier.tryParse(animationName);
        if (id == null) {
            stop(mob, layer, SPELL_STOP_FADE_TICKS);
            return;
        }
        play(mob, id, MobAnimationOptions.DEFAULT.withLayer(layer).withSpeed(speed).withPersistent(layer == MobAnimationLayer.CASTING));
    }

    public static void resync(LivingEntity mob, ServerPlayerEntity viewer) {
        if (!isAvailable()) return;
        var layers = PERSISTENT.get(mob);
        if (layers == null) return;
        for (MobAnimationPacket packet : layers.values()) {
            MRPGCNetworking.sendToPlayer(viewer, packet);
        }
    }

    private static boolean canAnimate(LivingEntity mob) {
        return isAvailable() && mob.getWorld() instanceof ServerWorld && !(mob instanceof PlayerEntity);
    }

    private static MobAnimationPacket stopPacket(LivingEntity mob, MobAnimationLayer layer, int fadeTicks) {
        return new MobAnimationPacket(mob.getId(), null, layer, 1.0F, Math.max(-1, fadeTicks), MobAnimationOptions.Mirror.AUTO, 0);
    }

    private static void forget(LivingEntity mob, MobAnimationLayer layer) {
        var layers = PERSISTENT.get(mob);
        if (layers == null) return;
        layers.remove(layer);
        if (layers.isEmpty()) PERSISTENT.remove(mob);
    }
}
