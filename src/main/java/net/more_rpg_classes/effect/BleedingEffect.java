package net.more_rpg_classes.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.more_rpg_classes.damage.BleedingDamageSource;

public class BleedingEffect extends StatusEffect {
    protected BleedingEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public  boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 1.0F) {
            entity.damage(new BleedingDamageSource(entity.getDamageSources().starve().getTypeRegistryEntry()), 1.0F);
        }

        return true;
    }


    @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            int i = 35 >> amplifier;
            if (i > 0) {
                return duration % i == 0;
            } else {
                return true;
            }
        }
}

