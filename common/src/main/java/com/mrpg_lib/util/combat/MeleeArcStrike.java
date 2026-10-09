package com.mrpg_lib.util.combat;

import com.mrpg_lib.util.AllyHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public final class MeleeArcStrike {
    private MeleeArcStrike() {
    }

    public static int strike(PathAwareEntity mob, double radius, float centerDegrees, float halfArcDegrees) {
        return strike(mob, radius, centerDegrees, halfArcDegrees, new HashSet<>());
    }

    public static int strike(PathAwareEntity mob, double radius, float centerDegrees, float halfArcDegrees, Set<LivingEntity> alreadyHit) {
        int hits = 0;
        for (LivingEntity entity : mob.getWorld().getNonSpectatingEntities(LivingEntity.class, mob.getBoundingBox().expand(radius, 1.5, radius))) {
            if (entity == mob || !entity.isAlive() || !AllyHelper.canHurt(mob, entity) || alreadyHit.contains(entity)) continue;
            if (!(entity instanceof PlayerEntity) && entity != mob.getTarget()) continue;
            Vec3d toEntity = entity.getPos().subtract(mob.getPos());
            double horizontal = Math.sqrt(toEntity.x * toEntity.x + toEntity.z * toEntity.z) - entity.getWidth() * 0.5;
            if (horizontal > radius) continue;
            float angle = MathHelper.wrapDegrees((float) (Math.atan2(toEntity.z, toEntity.x) * (180.0 / Math.PI)) - 90.0F - mob.getYaw());
            if (Math.abs(MathHelper.wrapDegrees(angle - centerDegrees)) <= halfArcDegrees && mob.tryAttack(entity)) {
                alreadyHit.add(entity);
                hits++;
            }
        }
        return hits;
    }
}
