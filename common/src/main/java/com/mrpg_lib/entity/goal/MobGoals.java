package com.mrpg_lib.entity.goal;

import com.mrpg_lib.mixin.MobEntityGoalsAccessor;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public final class MobGoals {
    private MobGoals() {
    }

    public static void addGoal(MobEntity mob, int priority, Goal goal) {
        ((MobEntityGoalsAccessor) mob).mrpg$getGoalSelector().add(priority, goal);
    }

    public static void addTargetGoal(MobEntity mob, int priority, Goal goal) {
        ((MobEntityGoalsAccessor) mob).mrpg$getTargetSelector().add(priority, goal);
    }

    public static void removeGoal(MobEntity mob, Goal goal) {
        ((MobEntityGoalsAccessor) mob).mrpg$getGoalSelector().remove(goal);
    }

    public static void removeTargetGoal(MobEntity mob, Goal goal) {
        ((MobEntityGoalsAccessor) mob).mrpg$getTargetSelector().remove(goal);
    }
}
