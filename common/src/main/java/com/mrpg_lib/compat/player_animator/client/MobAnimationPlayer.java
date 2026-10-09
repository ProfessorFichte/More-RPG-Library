package com.mrpg_lib.compat.player_animator.client;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.compat.player_animator.api.MobAnimationOptions;
import com.mrpg_lib.compat.player_animator.network.MobAnimationPacket;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public final class MobAnimationPlayer {
    private static final int DEFAULT_STOP_FADE_TICKS = 5;

    private static final Set<Identifier> WARNED = new HashSet<>();

    private MobAnimationPlayer() {
    }

    public static void apply(LivingEntity entity, MobAnimationState state, MobAnimationPacket packet) {
        if (packet.isStop()) {
            stop(state, packet);
        } else if (packet.poseStyle() != MobAnimationOptions.PoseStyle.PLAIN && MobWeaponPoses.handles(packet.layer())) {
            playWeaponPose(state, packet);
        } else {
            if (MobWeaponPoses.handles(packet.layer())) {
                state.weaponPoses().clear(packet.layer(), DEFAULT_STOP_FADE_TICKS);
            }
            play(entity, state, packet);
        }
    }

    private static void stop(MobAnimationState state, MobAnimationPacket packet) {
        int fade = packet.fadeTicks() >= 0 ? packet.fadeTicks() : DEFAULT_STOP_FADE_TICKS;
        if (MobWeaponPoses.handles(packet.layer())) {
            state.weaponPoses().clear(packet.layer(), fade);
        }
        state.slot(packet.layer()).base.replaceAnimationWithFade(fade(fade), null);
    }

    private static void playWeaponPose(MobAnimationState state, MobAnimationPacket packet) {
        Identifier id = packet.animationId();
        if (!(PlayerAnimationRegistry.getAnimation(id) instanceof KeyframeAnimation animation)) {
            warnMissing(id);
            return;
        }
        var slot = state.slot(packet.layer());
        if (slot.base.getAnimation() != null) {
            slot.base.replaceAnimationWithFade(fade(DEFAULT_STOP_FADE_TICKS), null);
        }
        state.weaponPoses().set(packet.layer(), id, animation,
                packet.poseStyle() == MobAnimationOptions.PoseStyle.WEAPON_TWO_HANDED);
        state.markDirty();
    }

    private static void play(LivingEntity entity, MobAnimationState state, MobAnimationPacket packet) {
        Identifier id = packet.animationId();
        if (!(PlayerAnimationRegistry.getAnimation(id) instanceof KeyframeAnimation animation)) {
            warnMissing(id);
            return;
        }
        try {
            var builder = animation.mutableCopy();
            builder.torso.fullyEnablePart(true);
            builder.head.pitch.setEnabled(false);
            builder.head.yaw.setEnabled(true);
            if (entity.hasVehicle()) {
                builder.leftLeg.setEnabled(false);
                builder.rightLeg.setEnabled(false);
            }
            int fade = packet.fadeTicks() >= 0 ? packet.fadeTicks() : builder.beginTick;
            var slot = state.slot(packet.layer());
            slot.mirror.setEnabled(mirrored(entity, packet.mirror()));
            slot.base.replaceAnimationWithFade(fade(fade), new KeyframeAnimationPlayer(builder.build(), 0));
            float scale = Float.isFinite(packet.speed()) && packet.speed() > 0 ? packet.speed() : 1.0F;
            applySpeed(slot.speed, scale, animation.endTick, packet);
            state.markDirty();
        } catch (RuntimeException e) {
            MRPGCMod.LOGGER.warn("Could not play mob animation {}", id, e);
        }
    }

    private static void applySpeed(GearSpeedModifier modifier, float scale, int endTick, MobAnimationPacket packet) {
        float length = packet.length();
        if (!(length > 0) || endTick <= 0) {
            modifier.set(scale, List.of());
            return;
        }
        float speed = endTick / length;
        float upswing = packet.upswingRate();
        float multiplier = packet.upswingMultiplier();
        if (!(upswing > 0) || upswing >= 1 || !(multiplier > 0)) {
            modifier.set(scale * speed, List.of());
            return;
        }
        float upswingSpeed = speed / (upswing / multiplier);
        float downwindSpeed = (float) (speed * MathHelper.lerp(Math.max(multiplier - 0.5, 0) / 0.5, 1F - upswing, upswing / (1F - upswing)));
        modifier.set(scale * upswingSpeed, List.of(
                new GearSpeedModifier.Gear(length * upswing, scale * downwindSpeed),
                new GearSpeedModifier.Gear(length, scale * speed)));
    }

    private static boolean mirrored(LivingEntity entity, MobAnimationOptions.Mirror mirror) {
        return switch (mirror) {
            case ALWAYS -> true;
            case NEVER -> false;
            case AUTO -> entity.getMainArm() == Arm.LEFT;
        };
    }

    private static AbstractFadeModifier fade(int ticks) {
        return AbstractFadeModifier.standardFadeIn(Math.max(1, ticks), Ease.INOUTSINE);
    }

    private static void warnMissing(Identifier id) {
        if (WARNED.add(id)) {
            MRPGCMod.LOGGER.warn("Mob animation {} is not a loaded player animation", id);
        }
    }
}
