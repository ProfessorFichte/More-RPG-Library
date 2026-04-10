package net.more_rpg_classes.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

public class WithersCurseEffect extends StatusEffect {
    protected WithersCurseEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public boolean applyUpdateEffect(LivingEntity livingEntity, int amplifier) {
        List<StatusEffectInstance> list = livingEntity.getStatusEffects().stream().toList();
        int amount_negative = -1;
        if (!list.isEmpty()) {
            for (StatusEffectInstance statusEffectInstance : list) {
                StatusEffect statusEffect = statusEffectInstance.getEffectType().value();
                if (!statusEffect.isBeneficial()) {
                }
                amount_negative++;
            }
            if(amplifier < amount_negative){
                livingEntity.addStatusEffect(new StatusEffectInstance(MRPGCEffects.WITHERS_CURSE.entry,
                        livingEntity.getStatusEffect(MRPGCEffects.WITHERS_CURSE.entry).getDuration()+40, amount_negative, false, false, true));
            }
        }
        return true;
    }



    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
