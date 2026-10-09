package com.mrpg_lib.compat.better_combat;

import com.mrpg_lib.compat.MrpgCompat;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public final class BetterCombatCompat {
    public record MeleeSettings(double baseRange, float intervalScale, boolean animations, boolean resetInvulnerability, boolean weaponTrails) {
        public static final MeleeSettings DEFAULT = new MeleeSettings(3.0, 1.0F, true, true, true);

        public MeleeSettings(double baseRange, float intervalScale, boolean animations) {
            this(baseRange, intervalScale, animations, true, true);
        }

        public MeleeSettings withBaseRange(double baseRange) {
            return new MeleeSettings(baseRange, intervalScale, animations, resetInvulnerability, weaponTrails);
        }

        public MeleeSettings withIntervalScale(float intervalScale) {
            return new MeleeSettings(baseRange, intervalScale, animations, resetInvulnerability, weaponTrails);
        }

        public MeleeSettings withAnimations(boolean animations) {
            return new MeleeSettings(baseRange, intervalScale, animations, resetInvulnerability, weaponTrails);
        }

        public MeleeSettings withResetInvulnerability(boolean resetInvulnerability) {
            return new MeleeSettings(baseRange, intervalScale, animations, resetInvulnerability, weaponTrails);
        }

        public MeleeSettings withWeaponTrails(boolean weaponTrails) {
            return new MeleeSettings(baseRange, intervalScale, animations, resetInvulnerability, weaponTrails);
        }
    }

    private BetterCombatCompat() {
    }

    public static boolean isLoaded() {
        return MrpgCompat.BETTER_COMBAT;
    }

    public static MeleeAttackGoal createMeleeGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
        return createMeleeGoal(mob, speed, pauseWhenMobIdle, MeleeSettings.DEFAULT);
    }

    public static MeleeAttackGoal createMeleeGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, MeleeSettings settings) {
        if (MrpgCompat.BETTER_COMBAT) {
            return BCMobMeleeAttackGoal.create(mob, speed, pauseWhenMobIdle, settings);
        }
        return new MeleeAttackGoal(mob, speed, pauseWhenMobIdle);
    }

    public static void onGoalRemoved(Goal goal) {
        if (MrpgCompat.BETTER_COMBAT && goal instanceof BCMobMeleeAttackGoal melee) {
            melee.clearPose();
        }
    }
}
