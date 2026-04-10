package net.more_rpg_classes.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class LowHealthFleeGoal<T extends PathAwareEntity & IConditionalFleeEntity> extends FleeEntityGoal<PlayerEntity> {

    private static final float LOW_HEALTH_THRESHOLD = 0.35f;
    private final T mob;

    public LowHealthFleeGoal(T mob) {
        super(mob, PlayerEntity.class, mob.getLowHealthFleeDistance(), 1.0, 1.2);
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if (mob.getHealth() > mob.getMaxHealth() * LOW_HEALTH_THRESHOLD) return false;
        if (shouldSuppressFlee()) return false;
        return super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        if (mob.getHealth() > mob.getMaxHealth() * LOW_HEALTH_THRESHOLD) return false;
        if (shouldSuppressFlee()) return false;
        return super.shouldContinue();
    }

    private boolean shouldSuppressFlee() {
        for (Identifier effectId : mob.getFleeImmuneEffects()) {
            var effectEntry = Registries.STATUS_EFFECT.getEntry(effectId).orElse(null);
            if (effectEntry != null && mob.hasStatusEffect(effectEntry)) return true;
        }

        LivingEntity target = mob.getTarget();
        if (target != null) {
            float threshold = mob.getFleeIgnoreTargetHpThreshold();
            if (threshold > 0 && target.getHealth() / target.getMaxHealth() < threshold) return true;

            for (Identifier effectId : mob.getFleeIgnoreIfTargetHasEffects()) {
                var effectEntry = Registries.STATUS_EFFECT.getEntry(effectId).orElse(null);
                if (effectEntry != null && target.hasStatusEffect(effectEntry)) return true;
            }
        }

        return false;
    }
}
