package net.more_rpg_classes.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;

public class KnockUpSpellImpact implements SpellHandlers.CustomImpact {
    private float base = 0.1f;
    private float multiplier = 0.025f;

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellHelper.ImpactContext context
    ) {
        if (!(target instanceof LivingEntity)) return null;
        int spellTier = spell.value().tier;
        float power = powerResult != null ? (float) powerResult.baseValue() : 1.0f;
        float effectiveMultiplier = multiplier * spellTier;
        double knockAmount = base + (effectiveMultiplier * power);
        knockAmount = Math.min(knockAmount, 2.0);

        Vec3d velocity = target.getVelocity();
        target.setVelocity(velocity.x, 0.0, velocity.z);
        target.addVelocity(0.0, knockAmount, 0.0);
        target.velocityModified = true;

        return new SpellHandlers.ImpactResult(true, false);
    }
}