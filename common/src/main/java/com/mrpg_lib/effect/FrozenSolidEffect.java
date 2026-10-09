package com.mrpg_lib.effect;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.tag.EntityTypeTags;

public class FrozenSolidEffect extends StatusEffect {

    public FrozenSolidEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public static void handleApplied(LivingEntity livingEntity) {
        EntityType<?> type = livingEntity.getType();
        if(type.isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
            livingEntity.removeStatusEffect(MRPGCEffects.FROZEN_SOLID.entry);
        }
    }

    public static boolean handleUpdate(LivingEntity livingEntity) {
        if(livingEntity.isOnFire() || livingEntity.isInLava()){
           return livingEntity.removeStatusEffect(MRPGCEffects.FROZEN_SOLID.entry);
        }
        return true;
    }

    @Override
    public void onApplied(LivingEntity livingEntity,  int amplifier) {
        super.onApplied(livingEntity, amplifier);
        handleApplied(livingEntity);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity livingEntity, int pAmplifier) {
        return handleUpdate(livingEntity);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

}
