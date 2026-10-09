package com.mrpg_lib.compat.player_animator.api;

public enum MobAnimationLayer {
    CASTING(900),
    RELEASE(950),
    MISC(200),
    POSE(100),
    ATTACK(700),
    DODGE(1000),
    OFF_HAND_POSE(90);

    private final int priority;

    MobAnimationLayer(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
