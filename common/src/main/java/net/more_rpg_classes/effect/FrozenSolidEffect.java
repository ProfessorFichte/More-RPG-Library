package net.more_rpg_classes.effect;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect;

public class FrozenSolidEffect extends SpellVulnerabilityStatusEffect {

    public FrozenSolidEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public void onApplied(LivingEntity livingEntity, AttributeContainer attributes, int amplifier) {
        super.onApplied(livingEntity, attributes, amplifier);
        EntityType<?> type = livingEntity.getType();
        if(type.isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
            livingEntity.removeStatusEffect(MRPGCEffects.FROZEN_SOLID.effect);
        }

    }

    @Override
    public void applyUpdateEffect(LivingEntity livingEntity, int pAmplifier) {
        if(livingEntity.isOnFire() || livingEntity.isInLava()){
           livingEntity.removeStatusEffect(MRPGCEffects.FROZEN_SOLID.effect);
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
