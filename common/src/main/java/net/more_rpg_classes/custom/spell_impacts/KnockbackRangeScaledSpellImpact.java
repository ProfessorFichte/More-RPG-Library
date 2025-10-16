package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;

public class KnockbackRangeScaledSpellImpact implements SpellHandlers.CustomImpact {

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellHelper.ImpactContext context
    ) {
        if (!caster.getWorld().isClient && target instanceof  LivingEntity livingEntity) {
            LivingEntity attacker = caster;
            float range = spell.value().range;
            int poscasterX = attacker.getBlockPos().getX();
            int poscasterZ = attacker.getBlockPos().getZ();
            int posentityX = livingEntity.getBlockPos().getX();
            int posentityZ = livingEntity.getBlockPos().getZ();
            int distance_to_target = (posentityX-poscasterX) + ( poscasterZ - posentityZ);

            float enchantment_power = 0.0F;
            float power_enchantment_value = 0.0F;
            float knockback_enchantment_value = 0.0F;
            var power_enchantment = attacker.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.PUNCH);
            var knockback_enchantment = attacker.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.KNOCKBACK);
            if (power_enchantment.isPresent()) {
                power_enchantment_value = EnchantmentHelper.getLevel(power_enchantment.get(), attacker.getMainHandStack());
            }
            if (knockback_enchantment.isPresent()) {
                knockback_enchantment_value = EnchantmentHelper.getLevel(knockback_enchantment.get(), attacker.getMainHandStack());
            }
            enchantment_power = power_enchantment_value + knockback_enchantment_value;

            float diff_calc = (range - distance_to_target)/5;
            float knockback = 0.1F + diff_calc + enchantment_power;

            double d = attacker.getX() - livingEntity.getX();
            double e;
            for(e = attacker.getZ() - livingEntity.getZ(); d * d + e * e < 1.0E-4; e = (Math.random() - Math.random()) * 0.01) {
                d = (Math.random() - Math.random()) * 0.01;
            }
            livingEntity.takeKnockback(knockback, d, e);
        }

        return new SpellHandlers.ImpactResult(true, false);
    }
}

