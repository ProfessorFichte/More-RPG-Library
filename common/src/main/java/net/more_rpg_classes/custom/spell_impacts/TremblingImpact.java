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

public class TremblingImpact implements SpellHandlers.CustomImpact {

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellExecution.ImpactContext context
    ) {
        if(target instanceof LivingEntity livingEntity && target.isOnGround()){
            double range = MRPGCMod.tweaksConfig.value.custom_spell_impact_trembling_range;
            double minx = -range;
            double maxx = range;
            double minz = -range;
            double maxz = range;
            double randx = minx + Math.random() * (maxx - minx);
            double randz = minz + Math.random() * (maxz - minz);
            Vec3d currentMovement = livingEntity.getVelocity();
            livingEntity.setVelocity(currentMovement.x + randx, currentMovement.y + 0.2, currentMovement.z +randz);
            livingEntity.velocityModified = true;
        }
        return new SpellHandlers.ImpactResult(true, false);
    }
}
