package com.mrpg_lib.effect;

import com.mrpg_lib.damage.PoisonDamageSource;
import com.mrpg_lib.util.tags.MRPGCEntityTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class FatalPoisonEffect extends StatusEffect {
    protected FatalPoisonEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public  boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        EntityType<?> type = ((Entity) entity).getType();
        if(!type.isIn(MRPGCEntityTags.POISON_IMMUNE)){
            entity.damage(new PoisonDamageSource(entity.getDamageSources().magic().getTypeRegistryEntry()), 1F + amplifier);
        }
        return true;
    }


    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 25 == 0;
    }

}
