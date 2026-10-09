package com.mrpg_lib.entity.goal;

import net.minecraft.entity.ai.goal.Goal;

import java.util.function.BooleanSupplier;

public class PredicateGatedGoal extends Goal {
    private final Goal wrapped;
    private final BooleanSupplier blocked;

    public PredicateGatedGoal(BooleanSupplier blocked, Goal wrapped) {
        this.blocked = blocked;
        this.wrapped = wrapped;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public boolean canStart() {
        return !blocked.getAsBoolean() && wrapped.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return !blocked.getAsBoolean() && wrapped.shouldContinue();
    }

    @Override
    public void start() {
        wrapped.start();
    }

    @Override
    public void stop() {
        wrapped.stop();
    }

    @Override
    public void tick() {
        wrapped.tick();
    }
}
