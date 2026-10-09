package com.mrpg_lib.compat.player_animator.client;

import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;

import java.util.EnumMap;

@Environment(EnvType.CLIENT)
public final class MobAnimationState {
    public static final class Slot {
        final ModifierLayer<IAnimation> base = new ModifierLayer<>();
        final GearSpeedModifier speed = new GearSpeedModifier();
        final MirrorModifier mirror = new MirrorModifier();

        private Slot() {
            mirror.setEnabled(false);
            base.addModifier(speed, 0);
            base.addModifier(mirror, 1);
        }

        boolean isPlaying() {
            return base.getAnimation() != null && base.isActive();
        }
    }

    private final AnimationStack stack = new AnimationStack();
    private final AnimationApplier applier = new AnimationApplier(stack);
    private final EnumMap<MobAnimationLayer, Slot> slots = new EnumMap<>(MobAnimationLayer.class);
    private final MobWeaponPoses weaponPoses;
    private boolean dirty;

    public MobAnimationState() {
        for (MobAnimationLayer layer : MobAnimationLayer.values()) {
            Slot slot = new Slot();
            slots.put(layer, slot);
            stack.addAnimLayer(layer.priority(), slot.base);
        }
        weaponPoses = new MobWeaponPoses(stack);
    }

    public Slot slot(MobAnimationLayer layer) {
        return slots.get(layer);
    }

    MobWeaponPoses weaponPoses() {
        return weaponPoses;
    }

    public AnimationApplier applier() {
        return applier;
    }

    public void tick(LivingEntity entity) {
        weaponPoses.tick(entity, slot(MobAnimationLayer.CASTING).isPlaying());
        stack.tick();
    }

    public boolean isActive() {
        return stack.isActive();
    }

    public boolean needsRender() {
        return dirty || stack.isActive();
    }

    public void markDirty() {
        dirty = true;
    }

    public void markApplied() {
        dirty = stack.isActive();
    }
}
