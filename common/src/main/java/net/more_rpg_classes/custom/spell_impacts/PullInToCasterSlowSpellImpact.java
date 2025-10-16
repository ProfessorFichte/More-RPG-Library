package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.more_rpg_classes.MRPGCMod;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;


public class PullInToCasterSlowSpellImpact implements SpellHandlers.CustomImpact {

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

        double speed = MRPGCMod.tweaksConfig.value.custom_spell_impact_pull_to_caster_slow_speed;
        Vec3d velocity = moveVec.multiply(speed);

        target.setVelocity(velocity);
        target.velocityModified = true;

        return new SpellHandlers.ImpactResult(true, false);
    }
}
