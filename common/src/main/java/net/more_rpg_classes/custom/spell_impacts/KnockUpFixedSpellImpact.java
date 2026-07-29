package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.more_rpg_classes.MRPGCMod;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;

public class KnockUpFixedSpellImpact implements SpellHandlers.CustomImpact {
    private float base = MRPGCMod.tweaksConfig.value.custom_spell_fixed_impact_knock_up;

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellHelper.ImpactContext context
    ) {
        if (!(target instanceof LivingEntity)) return null;

        ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING,20,
                0,false,false,false));
        Vec3d velocity = target.getVelocity();
        target.setVelocity(velocity.x, 0.0, velocity.z);
        target.addVelocity(0.0, base, 0.0);
        target.velocityModified = true;

        return new SpellHandlers.ImpactResult(true, false);
    }
}