package com.mrpg_lib.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public class WindupMeleeAttackGoal extends MeleeAttackGoal {
    private static final double REACH_TOLERANCE = 1.0;

    public interface ImpactHandler {
        void onImpact(int variant, @Nullable LivingEntity target);
    }

    private final PathAwareEntity mob;
    private final double extraReach;
    private final double extraReachSquared;
    private final int[] impactTicks;
    private final ImpactHandler handler;
    private int swingCount = 0;
    private int pendingVariant = -1;
    private int impactAge = 0;
    private LivingEntity pendingTarget;

    public WindupMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, double extraReach, int[] impactTicks) {
        this(mob, speed, pauseWhenMobIdle, extraReach, impactTicks, (variant, target) -> {
            if (target != null) {
                mob.tryAttack(target);
            }
        });
    }

    public WindupMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, double extraReach, int[] impactTicks, ImpactHandler handler) {
        super(mob, speed, pauseWhenMobIdle);
        this.mob = mob;
        this.extraReach = extraReach;
        this.extraReachSquared = extraReach * extraReach;
        this.impactTicks = impactTicks;
        this.handler = handler;
    }

    @Override
    protected boolean canAttack(LivingEntity target) {
        if (pendingVariant >= 0) {
            return false;
        }
        if (super.canAttack(target)) {
            return true;
        }
        return extraReachSquared > 0 && isCooledDown() && mob.squaredDistanceTo(target) <= extraReachSquared;
    }

    @Override
    protected void attack(LivingEntity target) {
        if (!canAttack(target)) {
            return;
        }
        resetCooldown();
        mob.swingHand(Hand.MAIN_HAND);
        pendingVariant = swingCount++ % impactTicks.length;
        pendingTarget = target;
        impactAge = mob.age + impactTicks[pendingVariant];
    }

    @Override
    public boolean shouldContinue() {
        return pendingVariant >= 0 || super.shouldContinue();
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingVariant >= 0 && mob.age >= impactAge) {
            resolveImpact();
        }
    }

    @Override
    public void stop() {
        super.stop();
        pendingVariant = -1;
        pendingTarget = null;
    }

    private void resolveImpact() {
        int variant = pendingVariant;
        LivingEntity target = pendingTarget;
        pendingVariant = -1;
        pendingTarget = null;
        if (!mob.isAlive() || mob.isRemoved()) {
            return;
        }
        handler.onImpact(variant, isReachable(target) ? target : null);
    }

    private boolean isReachable(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved() || !mob.getVisibilityCache().canSee(target)) {
            return false;
        }
        if (mob.isInAttackRange(target)) {
            return true;
        }
        double reach = Math.max(extraReach, (mob.getWidth() + target.getWidth()) * 0.5 + 1.0) + REACH_TOLERANCE;
        return mob.squaredDistanceTo(target) <= reach * reach;
    }
}
