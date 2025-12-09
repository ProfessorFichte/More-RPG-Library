package net.more_rpg_classes.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.damage.PoisonDamageSource;
import net.more_rpg_classes.util.tags.MRPGCEntityTags;

public class FatalPoisonEffect extends StatusEffect {
    protected FatalPoisonEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public  boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        EntityType<?> type = ((Entity) entity).getType();
        if(!type.isIn(MRPGCEntityTags.POISON_IMMUNE)){
            entity.damage(new PoisonDamageSource(entity.getDamageSources().magic().getTypeRegistryEntry()), 1F);
        }
        return true;
    }


    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int i = 25 >> amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }

}

