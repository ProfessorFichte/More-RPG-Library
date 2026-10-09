package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.util.AllyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.compat.CriticalStrikeCompat;
import net.spell_engine.internals.SpellModifiers;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellPower;

public class SpellEngineMethods {
    public static void spellSchoolDamageCalculation(Spell spell, float damageMultiplication, LivingEntity target, LivingEntity caster){
        var school = spell.school;
        var power = SpellPower.getSpellPower(school, caster);

        var registry = SpellRegistry.from(caster.getWorld());
        var spellId = registry.getId(spell);
        var spellEntry = spellId != null ? registry.getEntry(spellId).orElse(null) : null;

        if (spellEntry != null) {
            var bonusPower = 1F;
            var bonusCritChance = 0F;
            var bonusCritDamage = 0F;
            for (var modifier : SpellModifiers.of(caster, spellEntry, null)) {
                if (modifier.power_modifier != null) {
                    bonusPower += modifier.power_modifier.power_multiplier;
                    bonusCritChance += modifier.power_modifier.critical_chance_bonus;
                    bonusCritDamage += modifier.power_modifier.critical_damage_bonus;
                }
            }
            power = new SpellPower.Result(power.school(),
                    power.baseValue() * bonusPower,
                    power.criticalChance() + bonusCritChance,
                    power.criticalDamage() + bonusCritDamage);
        }

        var vulnerability = SpellPower.getVulnerability(target, school);
        var result = power.random(vulnerability);
        float damageAmount = (float) result.amount() * damageMultiplication;

        var damageSource = SpellDamageSource.create(school, caster);
        if (result.isCritical()) {
            CriticalStrikeCompat.setCriticalStrike(damageSource, (float) power.criticalDamage());
        }
        target.damage(damageSource, damageAmount);
    }

    @Deprecated
    public static boolean isEntityProtectedCheck(Entity other, LivingEntity owner) {
        return other != null && owner != null && AllyHelper.canHelp(owner, other);
    }
}
