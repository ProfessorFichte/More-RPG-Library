package com.mrpg_lib.mixin;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEntity.class)
public interface MobEntityGoalsAccessor {
    @Accessor("goalSelector")
    GoalSelector mrpg$getGoalSelector();

    @Accessor("targetSelector")
    GoalSelector mrpg$getTargetSelector();
}
