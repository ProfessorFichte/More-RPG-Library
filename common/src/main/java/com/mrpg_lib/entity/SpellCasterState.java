package com.mrpg_lib.entity;

import net.minecraft.entity.mob.MobEntity;

public final class SpellCasterState {
    private final MobEntity mob;
    private int castEndAge = Integer.MIN_VALUE;

    public SpellCasterState(MobEntity mob) {
        this.mob = mob;
    }

    public void start(int ticks) {
        castEndAge = mob.age + Math.max(1, ticks);
    }

    public void stop() {
        castEndAge = Integer.MIN_VALUE;
    }

    public boolean isCasting() {
        return mob.age < castEndAge;
    }

    public MobEntity mob() {
        return mob;
    }
}
