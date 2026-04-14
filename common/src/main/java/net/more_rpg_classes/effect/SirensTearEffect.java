package net.more_rpg_classes.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class SirensTearEffect extends StatusEffect {
    protected SirensTearEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        float currentHealthPercent = entity.getHealth() / entity.getMaxHealth();
        float healPercent;
        if (currentHealthPercent < 0.05F) {
            healPercent = 0.05F;
        } else {
            healPercent = 0.05F * (1.0F - currentHealthPercent);
        }
        entity.heal(entity.getMaxHealth() * healPercent);
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
