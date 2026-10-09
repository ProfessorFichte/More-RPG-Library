package com.mrpg_lib.compat.combat_roll;

import com.mrpg_lib.compat.MrpgCompat;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.Optional;

public final class CombatRollCompat {
    public record RollSettings(
            float defensiveHealthThreshold,
            boolean engage,
            double engageMinDistance,
            float engageChance,
            boolean dodgeProjectiles,
            int cooldownTicks,
            int invulnerableTicks,
            boolean animations) {

        public static final RollSettings DEFAULT = new RollSettings(0.35F, false, 5.0, 0.1F, false, 40, -1, true);

        public RollSettings withDefensiveHealthThreshold(float threshold) {
            return new RollSettings(threshold, engage, engageMinDistance, engageChance, dodgeProjectiles, cooldownTicks, invulnerableTicks, animations);
        }

        public RollSettings withEngage(boolean engage, double minDistance, float chance) {
            return new RollSettings(defensiveHealthThreshold, engage, minDistance, chance, dodgeProjectiles, cooldownTicks, invulnerableTicks, animations);
        }

        public RollSettings withDodgeProjectiles(boolean dodgeProjectiles) {
            return new RollSettings(defensiveHealthThreshold, engage, engageMinDistance, engageChance, dodgeProjectiles, cooldownTicks, invulnerableTicks, animations);
        }

        public RollSettings withCooldownTicks(int cooldownTicks) {
            return new RollSettings(defensiveHealthThreshold, engage, engageMinDistance, engageChance, dodgeProjectiles, cooldownTicks, invulnerableTicks, animations);
        }

        public RollSettings withInvulnerableTicks(int invulnerableTicks) {
            return new RollSettings(defensiveHealthThreshold, engage, engageMinDistance, engageChance, dodgeProjectiles, cooldownTicks, invulnerableTicks, animations);
        }

        public RollSettings withAnimations(boolean animations) {
            return new RollSettings(defensiveHealthThreshold, engage, engageMinDistance, engageChance, dodgeProjectiles, cooldownTicks, invulnerableTicks, animations);
        }
    }

    private CombatRollCompat() {
    }

    public static boolean isLoaded() {
        return MrpgCompat.COMBAT_ROLL;
    }

    public static Optional<Goal> createRollGoal(PathAwareEntity mob) {
        return createRollGoal(mob, RollSettings.DEFAULT);
    }

    public static Optional<Goal> createRollGoal(PathAwareEntity mob, RollSettings settings) {
        if (!MrpgCompat.COMBAT_ROLL) {
            return Optional.empty();
        }
        return Optional.of(CombatRollMobGoal.create(mob, settings));
    }
}
