package net.more_rpg_classes.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.more_rpg_classes.entity.ISpellCasterEntity;

import java.util.EnumSet;

public class BackAwayGoal<T extends MobEntity & ISpellCasterEntity> extends Goal {

    private final T mob;
    private final float minDistance;
    private final double speed;
    private int navigationTimer;

    public BackAwayGoal(T mob, float minDistance, double speed) {
        this.mob = mob;
        this.minDistance = minDistance;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (mob.isCastingSpell()) return false;
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        return mob.squaredDistanceTo(target) < minDistance * minDistance;
    }

    @Override
    public boolean shouldContinue() {
        if (mob.isCastingSpell()) return false;
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        return mob.squaredDistanceTo(target) < minDistance * minDistance;
    }

    @Override
    public void start() {
        navigationTimer = 0;
        updateNavigation();
    }

    @Override
    public void tick() {
        if (--navigationTimer <= 0) {
            navigationTimer = 5;
            updateNavigation();
        }
    }

    private void updateNavigation() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        Vec3d awayDir = mob.getPos().subtract(target.getPos()).normalize();
        Vec3d dest = mob.getPos().add(awayDir.multiply(minDistance + 1.0));
        mob.getNavigation().startMovingTo(dest.x, dest.y, dest.z, speed);
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }
}
