package com.mrpg_lib.util;

import com.mrpg_lib.entity.ControlledOwnerAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public final class AllyHelper {
    private static final int MAX_OWNER_DEPTH = 4;

    public interface RelationBridge {
        boolean actionAllowed(LivingEntity caster, Entity target, boolean harmful);
    }

    private static RelationBridge relationBridge;

    private AllyHelper() {
    }

    public static void setRelationBridge(RelationBridge bridge) {
        relationBridge = bridge;
    }

    public static boolean canHurt(Entity attacker, Entity target) {
        if (target == null) {
            return false;
        }
        if (target.isSpectator() || target instanceof PlayerEntity player && player.isCreative()) {
            return false;
        }
        Entity source = resolveOwner(attacker);
        Entity victim = resolveOwner(target);
        if (source == null) {
            return true;
        }
        if (source == victim || source == target || isTeammate(source, target) || isTeammate(source, victim)) {
            return false;
        }
        if (source instanceof MobEntity && victim instanceof MobEntity) {
            return isHostile(source) != isHostile(victim);
        }
        Boolean bridged = askBridge(source, target, true);
        return bridged == null || bridged;
    }

    public static boolean canHelp(Entity caster, Entity target) {
        if (target == null) {
            return false;
        }
        Entity source = resolveOwner(caster);
        Entity beneficiary = resolveOwner(target);
        if (source == null) {
            return false;
        }
        if (source == beneficiary || source == target || isTeammate(source, target) || isTeammate(source, beneficiary)) {
            return true;
        }
        Boolean bridged = askBridge(source, target, false);
        if (bridged != null) {
            return bridged;
        }
        return source instanceof MobEntity && beneficiary instanceof MobEntity && isHostile(source) == isHostile(beneficiary)
                || source instanceof PlayerEntity && beneficiary instanceof PlayerEntity;
    }

    public static boolean isAlly(Entity first, Entity second) {
        if (first == null || second == null) {
            return false;
        }
        return !canHurt(first, second);
    }

    public static Entity resolveOwner(Entity entity) {
        Entity current = entity;
        for (int depth = 0; current != null && depth < MAX_OWNER_DEPTH; depth++) {
            Entity next = directOwner(current);
            if (next == null || next == current) {
                break;
            }
            current = next;
        }
        return current;
    }

    private static Entity directOwner(Entity entity) {
        if (entity instanceof ControlledOwnerAccess access) {
            UUID controller = access.mrpg$getControlOwner();
            if (controller != null && entity.getWorld() instanceof ServerWorld world) {
                Entity controllerEntity = world.getEntity(controller);
                if (controllerEntity != null) {
                    return controllerEntity;
                }
            }
        }
        if (entity instanceof Ownable ownable) {
            return ownable.getOwner();
        }
        if (entity instanceof Tameable tameable) {
            return tameable.getOwner();
        }
        return null;
    }

    private static boolean isHostile(Entity entity) {
        return entity instanceof Monster;
    }

    private static boolean isTeammate(Entity first, Entity second) {
        return first != null && second != null && first.isTeammate(second);
    }

    private static Boolean askBridge(Entity source, Entity target, boolean harmful) {
        RelationBridge bridge = relationBridge;
        if (bridge == null || !(source instanceof LivingEntity livingSource)) {
            return null;
        }
        try {
            return bridge.actionAllowed(livingSource, target, harmful);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
