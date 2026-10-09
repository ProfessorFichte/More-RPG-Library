package com.mrpg_lib.compat.spell_engine.spell_impacts;

import com.mrpg_lib.entity.FriendlyLightningEntity;
import com.mrpg_lib.entity.MRPGCEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
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
