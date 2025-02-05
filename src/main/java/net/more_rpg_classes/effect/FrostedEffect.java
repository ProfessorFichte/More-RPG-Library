package net.more_rpg_classes.effect;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.EntityTypeTags;
import net.more_rpg_classes.MRPGCMod;

import static net.more_rpg_classes.util.CustomMethods.stackFreezeStacks;

public class FrostedEffect extends StatusEffect {
    public FrostedEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public void onApplied(LivingEntity livingEntity,  int amplifier) {
        super.onApplied(livingEntity, amplifier);
        EntityType<?> type = livingEntity.getType();
        if(!type.isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
            if(!livingEntity.hasStatusEffect(MRPGCEffects.FROZEN_SOLID.registryEntry)){
                stackFreezeStacks(livingEntity,20);
                if(amplifier == MRPGCMod.effectsConfig.value.frosted_amplifier_frozen_solid_conversion){
                    livingEntity.addStatusEffect(new StatusEffectInstance(MRPGCEffects.FROZEN_SOLID.registryEntry,
                            100,0,false,false,true));
                    livingEntity.removeStatusEffect(MRPGCEffects.FROSTED.registryEntry);
                }
            }else{
                livingEntity.removeStatusEffect(MRPGCEffects.FROSTED.registryEntry);
            }
        } else{
            livingEntity.removeStatusEffect(MRPGCEffects.FROSTED.registryEntry);
        }

    }

}
