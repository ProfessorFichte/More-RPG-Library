package com.mrpg_lib.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class LongReachMeleeAttackGoal extends MeleeAttackGoal {
    private final PathAwareEntity mob;
    private final double extraReachSquared;

    public LongReachMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, double extraReach) {
        super(mob, speed, pauseWhenMobIdle);
        this.mob = mob;
        this.extraReachSquared = extraReach * extraReach;
    }

    @Override
    protected boolean canAttack(LivingEntity target) {
        if (!this.isCooledDown()) {
            return false;
        }
        return this.mob.squaredDistanceTo(target) <= this.extraReachSquared || super.canAttack(target);
    }
}
