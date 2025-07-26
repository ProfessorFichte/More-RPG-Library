package net.more_rpg_classes.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;


public class PullInFrontOfCasterSpellImpact implements SpellHandlers.CustomImpact {

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellHelper.ImpactContext context
    ) {
        if (!(target instanceof LivingEntity)) return null;
        Vec3d lookVec = caster.getRotationVec(1.0F).normalize();
        Vec3d desiredPos = caster.getPos().add(lookVec.multiply(1.0));
        desiredPos = new Vec3d(desiredPos.x, target.getY(), desiredPos.z);

        Vec3d moveVec = desiredPos.subtract(target.getPos()).normalize();

        double speed = 0.8;
        Vec3d velocity = moveVec.multiply(speed);

        target.setVelocity(velocity);
        target.velocityModified = true;

        return new SpellHandlers.ImpactResult(true, false);
    }
}
