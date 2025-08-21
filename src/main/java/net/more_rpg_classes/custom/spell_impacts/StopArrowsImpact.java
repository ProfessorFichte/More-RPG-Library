package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;
import net.minecraft.util.math.Box;

public class StopArrowsImpact implements SpellHandlers.CustomImpact {

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellHelper.ImpactContext context
    ) {

        float range = spell.value().range * 1.5F;
        Box radius = new Box(
                caster.getX() - range, caster.getY() - range / 3, caster.getZ() - range,
                caster.getX() + range, caster.getY() + range / 3, caster.getZ() + range
        );
        for (Entity entity : caster.getEntityWorld().getOtherEntities(caster, radius, e -> e instanceof PersistentProjectileEntity)) {
            PersistentProjectileEntity arrow = (PersistentProjectileEntity) entity;
            arrow.setVelocity(Vec3d.ZERO);
            arrow.velocityModified = true;


        }
        return new SpellHandlers.ImpactResult(true,false);
    }
}


