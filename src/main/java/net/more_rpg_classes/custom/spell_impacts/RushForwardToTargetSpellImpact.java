package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;

public class RushForwardToTargetSpellImpact implements SpellHandlers.CustomImpact {

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellHelper.ImpactContext context
    ) {
        if (!caster.getWorld().isClient) {
            Vec3d lookVec = target.getRotationVec(1.0F).normalize();
            Vec3d desiredPos = target.getPos().add(lookVec.multiply(1.0));
            desiredPos = new Vec3d(desiredPos.x, caster.getY(), desiredPos.z);

            Vec3d direction = desiredPos.subtract(caster.getPos());
            double distance = direction.length();
            if (distance > 0.001) {
                Vec3d velocity = direction.normalize().multiply(distance);
                caster.setVelocity(velocity);
                caster.velocityModified = true;
                caster.velocityDirty = true;
            }
        }

        return new SpellHandlers.ImpactResult(true, false);
    }
}

