package net.more_rpg_classes.effect;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect;

public class FrozenSolidEffect extends SpellVulnerabilityStatusEffect {

    public FrozenSolidEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public void onApplied(LivingEntity livingEntity,  int amplifier) {
        super.onApplied(livingEntity, amplifier);
        EntityType<?> type = livingEntity.getType();
        if(type.isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
            livingEntity.removeStatusEffect(MRPGCEffects.FROZEN_SOLID.entry);
        }

    }

    @Override
    public boolean applyUpdateEffect(LivingEntity livingEntity, int pAmplifier) {
        if(livingEntity.isOnFire() || livingEntity.isInLava()){
           return livingEntity.removeStatusEffect(MRPGCEffects.FROZEN_SOLID.entry);
        }
        super.applyUpdateEffect(livingEntity, pAmplifier);
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

}
