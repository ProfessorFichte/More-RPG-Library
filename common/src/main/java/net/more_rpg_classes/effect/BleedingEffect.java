package net.more_rpg_classes.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.more_rpg_classes.damage.BleedingDamageSource;
import net.more_rpg_classes.util.tags.MRPGCEntityTags;

public class BleedingEffect extends StatusEffect {
    protected BleedingEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public  boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        EntityType<?> type = ((Entity) entity).getType();
        if(type.isIn(MRPGCEntityTags.BLEEDING_IMMUNE)){
            entity.removeStatusEffect(MRPGCEffects.BLEEDING.registryEntry);
        }
        float bleedingTickDamage = 1.0F;
        float currentHealthPercentage = entity.getHealth() / entity.getMaxHealth();
        if(currentHealthPercentage <= 0.75F){
            bleedingTickDamage = bleedingTickDamage + (entity.getMaxHealth() * 0.01F);
        }
        if(currentHealthPercentage <= 0.5F){
            bleedingTickDamage = bleedingTickDamage + (entity.getMaxHealth() * 0.025F);
        }
        if(currentHealthPercentage <= 0.25F){
            bleedingTickDamage = bleedingTickDamage + (entity.getMaxHealth() * 0.05F);
        }
        entity.timeUntilRegen = 0;
        entity.damage(new BleedingDamageSource(entity.getDamageSources().starve().getTypeRegistryEntry()), bleedingTickDamage);
        return true;
    }


    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        if (interval < 20) {
            interval = 20;
        }
        return duration % interval == 0;
    }

}

