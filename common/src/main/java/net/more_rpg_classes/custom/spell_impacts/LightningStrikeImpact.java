package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.more_rpg_classes.entity.FriendlyLightningEntity;
import net.more_rpg_classes.entity.MRPGCEntities;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellExecution;
import net.spell_power.api.SpellPower;

public class LightningStrikeImpact implements SpellHandlers.CustomImpact {

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellExecution.ImpactContext context
    ) {
        if(target instanceof LivingEntity livingEntity && livingEntity.getWorld() instanceof ServerWorld serverWorld){
            FriendlyLightningEntity.spawnAtBlock(
                serverWorld,
                livingEntity.getBlockPos(),
                caster,
                MRPGCEntities.FRIENDLY_LIGHTNING
            );
        }
        return new SpellHandlers.ImpactResult(true, false);
    }
}
