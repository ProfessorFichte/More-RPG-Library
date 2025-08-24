package net.more_rpg_classes.effect;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.more_rpg_classes.util.CustomMethods;

import static net.more_rpg_classes.util.CustomMethods.stackFreezeStacks;

public class FrozenSolidEffect extends StatusEffect {

    public FrozenSolidEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public void onApplied(LivingEntity livingEntity,  int amplifier) {
        super.onApplied(livingEntity, amplifier);
        EntityType<?> type = livingEntity.getType();
        if(!type.isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
            stackFreezeStacks(livingEntity,10);
        } else{
            livingEntity.removeStatusEffect(MRPGCEffects.FROZEN_SOLID.registryEntry);
        }

    }

    @Override
    public boolean applyUpdateEffect(LivingEntity livingEntity, int pAmplifier) {
        EntityType<?> type = livingEntity.getType();
        if(!type.isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
            CustomMethods.freezeDamageTicks(livingEntity);
        }
        if(livingEntity.isOnFire() || livingEntity.isInLava()){
           return livingEntity.removeStatusEffect(MRPGCEffects.FROZEN_SOLID.registryEntry);
        }
        super.applyUpdateEffect(livingEntity, pAmplifier);
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

}
