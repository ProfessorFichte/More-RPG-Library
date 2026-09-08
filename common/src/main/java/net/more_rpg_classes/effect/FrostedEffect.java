package net.more_rpg_classes.effect;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.EntityTypeTags;
import net.more_rpg_classes.MRPGCMod;

public class FrostedEffect extends StatusEffect {
    public FrostedEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public void onApplied(LivingEntity livingEntity, AttributeContainer attributes, int amplifier) {
        super.onApplied(livingEntity, attributes, amplifier);
        EntityType<?> type = livingEntity.getType();
        if(!type.isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
            if(!livingEntity.hasStatusEffect(MRPGCEffects.FROZEN_SOLID.effect)){
                if(amplifier == MRPGCMod.tweaksConfig.value.effect_frosted_amplifier_frozen_solid_conversion){
                    livingEntity.addStatusEffect(new StatusEffectInstance(MRPGCEffects.FROZEN_SOLID.effect,
                            100,0,false,false,true));
                    livingEntity.removeStatusEffect(MRPGCEffects.FROSTED.effect);
                }
            }else{
                livingEntity.removeStatusEffect(MRPGCEffects.FROSTED.effect);
            }
        } else{
            livingEntity.removeStatusEffect(MRPGCEffects.FROSTED.effect);
        }
    }

    @Override
    public void applyUpdateEffect(LivingEntity livingEntity, int pAmplifier) {
        if(livingEntity.isOnFire() || livingEntity.isInLava()){
            livingEntity.removeStatusEffect(MRPGCEffects.FROSTED.effect);
            return;
        }
        super.applyUpdateEffect(livingEntity, pAmplifier);
        return;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;

    }

}
