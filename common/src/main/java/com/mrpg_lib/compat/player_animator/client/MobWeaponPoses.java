package com.mrpg_lib.compat.player_animator.client;

import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Environment(EnvType.CLIENT)
final class MobWeaponPoses {
    private static final int FADE_TICKS = 5;
    private static final double WALKING_SPEED = 0.03;

    private record Request(Identifier id, KeyframeAnimation animation, boolean twoHanded) {
    }

    private final Channel mainBody = new Channel(true);
    private final Channel mainItem = new Channel(false);
    private final Channel offBody = new Channel(true);
    private final Channel offItem = new Channel(false);
    @Nullable private Request main;
    @Nullable private Request off;
    private double lastX = Double.NaN;
    private double lastZ = Double.NaN;

    MobWeaponPoses(AnimationStack stack) {
        int off = MobAnimationLayer.OFF_HAND_POSE.priority();
        int main = MobAnimationLayer.POSE.priority();
        stack.addAnimLayer(off - 1, offItem.base);
        stack.addAnimLayer(off, offBody.base);
        stack.addAnimLayer(main - 1, mainItem.base);
        stack.addAnimLayer(main, mainBody.base);
    }

    static boolean handles(MobAnimationLayer layer) {
        return layer == MobAnimationLayer.POSE || layer == MobAnimationLayer.OFF_HAND_POSE;
    }

    void set(MobAnimationLayer layer, Identifier id, KeyframeAnimation animation, boolean twoHanded) {
        var request = new Request(id, animation, twoHanded);
        if (layer == MobAnimationLayer.POSE) {
            main = request;
        } else {
            off = request;
        }
    }

    void clear(MobAnimationLayer layer, int fadeTicks) {
        if (layer == MobAnimationLayer.POSE) {
            main = null;
            mainBody.set(null, null, false, fadeTicks);
            mainItem.set(null, null, false, fadeTicks);
        } else {
            off = null;
            offBody.set(null, null, false, fadeTicks);
            offItem.set(null, null, false, fadeTicks);
        }
    }

    void tick(LivingEntity entity, boolean casting) {
        double dx = entity.getX() - lastX;
        double dz = entity.getZ() - lastZ;
        boolean moved = dx * dx + dz * dz > WALKING_SPEED * WALKING_SPEED;
        lastX = entity.getX();
        lastZ = entity.getZ();
        if (main == null && off == null) return;

        boolean leftHanded = entity.getMainArm() == Arm.LEFT;
        if (entity.handSwinging
                || entity.isSwimming()
                || entity.isUsingItem()
                || entity.isClimbing()
                || entity.isFallFlying()
                || casting
                || CrossbowItem.isCharged(entity.getMainHandStack())) {
            mainBody.set(entity, null, leftHanded, FADE_TICKS);
            mainItem.set(entity, null, leftHanded, FADE_TICKS);
            offBody.set(entity, null, leftHanded, FADE_TICKS);
            offItem.set(entity, null, leftHanded, FADE_TICKS);
            return;
        }

        mainItem.set(entity, main, leftHanded, FADE_TICKS);
        offItem.set(entity, off, leftHanded, FADE_TICKS);

        Request mainBodyPose = main;
        Request offBodyPose = off;
        boolean twoHanded = main != null && main.twoHanded();
        boolean walking = !entity.isDead() && (entity.isSwimming() || moved);
        if (!twoHanded && (walking || entity.isSneaking())) {
            mainBodyPose = null;
            offBodyPose = null;
        }
        mainBody.set(entity, mainBodyPose, leftHanded, FADE_TICKS);
        offBody.set(entity, offBodyPose, !leftHanded, FADE_TICKS);
    }

    private static final class Channel {
        final ModifierLayer<IAnimation> base = new ModifierLayer<>();
        private final MirrorModifier mirror = new MirrorModifier();
        private final boolean bodyChannel;
        private boolean started;
        @Nullable private Identifier current;
        private boolean currentMirror;

        Channel(boolean bodyChannel) {
            this.bodyChannel = bodyChannel;
            mirror.setEnabled(false);
            base.addModifier(mirror, 0);
        }

        void set(@Nullable LivingEntity entity, @Nullable Request request, boolean mirrored, int fadeTicks) {
            Identifier id = request != null ? request.id() : null;
            if (started && Objects.equals(id, current) && (id == null || mirrored == currentMirror)) return;
            started = true;
            current = id;
            currentMirror = mirrored;
            if (request == null || entity == null) {
                base.replaceAnimationWithFade(fade(fadeTicks), null);
                return;
            }
            var copy = request.animation().mutableCopy();
            if (bodyChannel) {
                if (entity.hasVehicle() || entity.getPose() == EntityPose.SWIMMING) {
                    copy.rightLeg.setEnabled(false);
                    copy.leftLeg.setEnabled(false);
                }
                copy.rightItem.setEnabled(false);
                copy.leftItem.setEnabled(false);
            } else {
                copy.head.setEnabled(false);
                copy.torso.setEnabled(false);
                copy.body.setEnabled(false);
                copy.rightArm.setEnabled(false);
                copy.leftArm.setEnabled(false);
                copy.rightLeg.setEnabled(false);
                copy.leftLeg.setEnabled(false);
            }
            mirror.setEnabled(mirrored);
            base.replaceAnimationWithFade(fade(fadeTicks), new KeyframeAnimationPlayer(copy.build(), 0));
        }

        private static AbstractFadeModifier fade(int ticks) {
            return AbstractFadeModifier.standardFadeIn(Math.max(1, ticks), Ease.INOUTSINE);
        }
    }
}
