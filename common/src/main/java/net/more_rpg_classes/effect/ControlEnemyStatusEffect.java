package net.more_rpg_classes.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.more_rpg_classes.entity.ControlledOwnerAccess;

import java.util.UUID;

public abstract class ControlEnemyStatusEffect extends StatusEffect {
    protected ControlEnemyStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public double controlRange() {
        return 24.0;
    }

    public boolean flipsCasterRelations() {
        return true;
    }

    public boolean followsOwner() {
        return true;
    }

    public boolean attacksOwnerTarget() {
        return true;
    }

    public boolean attacksOwnerAttacker() {
        return true;
    }

    public double followSpeed() {
        return 1.0;
    }

    public double followStopDistance() {
        return 3.0;
    }

    protected int retargetInterval() {
        return 10;
    }

    protected boolean canControl(MobEntity mob) {
        return true;
    }

    protected boolean isValidTarget(LivingEntity controlled, Entity target) {
        return target instanceof HostileEntity && target != controlled;
    }

    protected boolean isImmune(LivingEntity entity) {
        return false;
    }

    protected void onImmune(LivingEntity entity) {
    }

    protected void onControlledNonMob(LivingEntity entity) {
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        if (!entity.getWorld().isClient() && isImmune(entity)) {
            onImmune(entity);
        }
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient()) {
            LivingEntity owner = resolveOwner(entity);
            if (entity instanceof MobEntity mob) {
                if (canControl(mob)) {
                    updateControlledMob(mob, owner);
                }
            } else {
                onControlledNonMob(entity);
            }
        }
        return true;
    }

    private void updateControlledMob(MobEntity mob, LivingEntity owner) {
        LivingEntity preferred = ownerPriorityTarget(mob, owner);
        if (preferred != null) {
            if (mob.getTarget() != preferred) {
                mob.setTarget(preferred);
            }
            return;
        }
        LivingEntity current = mob.getTarget();
        boolean needsTarget = current == null || !current.isAlive() || !isValidTarget(mob, current);
        if (!needsTarget) return;
        int interval = retargetInterval();
        if (interval > 1 && mob.age % interval != 0) return;
        LivingEntity nearest = findNearestTarget(mob, owner);
        if (nearest != null) {
            mob.setTarget(nearest);
        } else if (followsOwner() && owner != null) {
            followOwner(mob, owner);
        }
    }

    private LivingEntity ownerPriorityTarget(MobEntity mob, LivingEntity owner) {
        if (owner == null) return null;
        if (attacksOwnerTarget()) {
            LivingEntity ownerTarget = owner.getAttacking();
            if (isOwnerCombatTarget(mob, owner, ownerTarget)) return ownerTarget;
        }
        if (attacksOwnerAttacker()) {
            LivingEntity ownerAttacker = owner.getAttacker();
            if (isOwnerCombatTarget(mob, owner, ownerAttacker)) return ownerAttacker;
        }
        return null;
    }

    private boolean isOwnerCombatTarget(MobEntity mob, LivingEntity owner, LivingEntity candidate) {
        if (candidate == null || !candidate.isAlive() || candidate == owner || candidate == mob) return false;
        return !isControlledBySameOwner(owner, candidate);
    }

    private void followOwner(MobEntity mob, LivingEntity owner) {
        double stop = followStopDistance();
        if (mob.squaredDistanceTo(owner) > stop * stop) {
            mob.getNavigation().startMovingTo(owner, followSpeed());
        }
    }

    private LivingEntity findNearestTarget(MobEntity mob, LivingEntity owner) {
        var box = mob.getBoundingBox().expand(controlRange());
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity other : mob.getEntityWorld().getOtherEntities(mob, box, EntityPredicates.VALID_LIVING_ENTITY)) {
            if (!(other instanceof LivingEntity living)) continue;
            if (!isValidTarget(mob, living)) continue;
            if (isControlledBySameOwner(owner, living)) continue;
            double distance = mob.squaredDistanceTo(living);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = living;
            }
        }
        return nearest;
    }

    // Prevents two entities controlled by the same owner (e.g. two charmed mobs, or a charmed mob and its caster's other minion) from targeting each other.
    private boolean isControlledBySameOwner(LivingEntity owner, Entity candidate) {
        if (owner == null || !(candidate instanceof ControlledOwnerAccess access)) return false;
        UUID candidateOwnerId = access.mrpg$getControlOwner();
        return candidateOwnerId != null && candidateOwnerId.equals(owner.getUuid());
    }

    private LivingEntity resolveOwner(LivingEntity entity) {
        if (!(entity instanceof ControlledOwnerAccess access)) return null;
        UUID ownerId = access.mrpg$getControlOwner();
        if (ownerId == null) return null;
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return null;
        if (serverWorld.getEntity(ownerId) instanceof LivingEntity owner && owner.isAlive()) {
            return owner;
        }
        return null;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    public static boolean isControlled(LivingEntity entity) {
        for (var instance : entity.getStatusEffects()) {
            if (instance.getEffectType().value() instanceof ControlEnemyStatusEffect control
                    && control.flipsCasterRelations()) {
                return true;
            }
        }
        return false;
    }
}
