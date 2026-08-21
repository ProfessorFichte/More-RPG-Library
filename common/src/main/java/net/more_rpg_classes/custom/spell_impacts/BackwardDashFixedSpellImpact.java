package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.more_rpg_classes.MRPGCMod;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellExecution;
import net.spell_power.api.SpellPower;

@Deprecated
public class BackwardDashFixedSpellImpact implements SpellHandlers.CustomImpact {

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellExecution.ImpactContext context
    ) {
        if (!caster.getWorld().isClient) {
            float speed_leaping = MRPGCMod.tweaksConfig.value.custom_spell_impact_backward_dash_range;
            float leaping_height = MRPGCMod.tweaksConfig.value.custom_spell_impact_backward_dash_height;
            caster.velocityDirty = true;
            caster.velocityModified = true;
            Vec3d rotationVector = caster.getRotationVector();
            Vec3d velocity = caster.getVelocity();
            caster.addVelocity(rotationVector.x * 0.1 + (rotationVector.x * 2.5 - velocity.x) * speed_leaping,
                    leaping_height, rotationVector.z * 0.1 + (rotationVector.z * 2.5 - velocity.z) * speed_leaping);
        }

        return new SpellHandlers.ImpactResult(true, false);
    }
}
