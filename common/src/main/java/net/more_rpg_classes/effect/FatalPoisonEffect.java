package net.more_rpg_classes.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.more_rpg_classes.damage.PoisonDamageSource;
import net.more_rpg_classes.util.tags.MRPGCEntityTags;
import net.spell_engine.api.effect.TickingStatusEffect;

public class FatalPoisonEffect extends TickingStatusEffect {
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

