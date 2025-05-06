package net.more_rpg_classes.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.more_rpg_classes.damage.BleedingDamageSource;

public class BleedingEffect extends StatusEffect {
    protected BleedingEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public  boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        EntityType<?> type = ((Entity) entity).getType();
        if(type.isIn(EntityTypeTags.UNDEAD)){
            entity.removeStatusEffect(MRPGCEffects.BLEEDING.registryEntry);
        }
        entity.damage(new BleedingDamageSource(entity.getDamageSources().starve().getTypeRegistryEntry()), 1.0F);
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

