package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.more_rpg_classes.MRPGCMod;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellExecution;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellPower;

public class DamageToMissingHealthSpellImpact implements SpellHandlers.CustomImpact {

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellExecution.ImpactContext context
    ) {
        if (target instanceof  LivingEntity livingEntity) {
            float missinghealth = (livingEntity.getHealth() - livingEntity.getMaxHealth()) / livingEntity.getMaxHealth();
            float damageMultiplier = MRPGCMod.tweaksConfig.value.custom_spell_impact_damage_to_missing_health_above_50;
            if(missinghealth >= 0.5F){
                damageMultiplier = MRPGCMod.tweaksConfig.value.custom_spell_impact_damage_to_missing_health_under_50;
            } else if(missinghealth >= 0.25F){
                damageMultiplier = MRPGCMod.tweaksConfig.value.custom_spell_impact_damage_to_missing_health_under_25;
            }
            float schoolAttributeValue = (float) caster.getAttributeValue(spell.value().school.attributeEntry);
            livingEntity.timeUntilRegen = 0;
            livingEntity.damage(SpellDamageSource.create(spell.value().school, caster), schoolAttributeValue * damageMultiplier);
        }
        return new SpellHandlers.ImpactResult(true, false);
    }
}

