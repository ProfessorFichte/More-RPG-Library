package net.more_rpg_classes.custom;

import net.minecraft.util.Identifier;
import net.more_rpg_classes.custom.spell_impacts.*;
import net.spell_engine.api.spell.event.SpellHandlers;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class CustomSpellImpacts {

    public static void registerCustomImpacts(){
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "knock_up"),
                new KnockUpSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "knock_up_fixed"),
                new KnockUpFixedSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "stop_arrows"),
                new StopArrowsImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "pull_to_caster_direct"),
                new PullInToCasterDirectSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "pull_to_caster_slow"),
                new PullInToCasterSlowSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "lightning"),
                new LightningStrikeImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "trembling"),
                new TremblingImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "rush_forward_to_target"),
                new RushForwardToTargetSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "backward_dash_fixed"),
                new BackwardDashFixedSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "backward_dash_range"),
                new BackwardDashRangeSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "range_scaled_knockback"),
                new KnockbackRangeScaledSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "frozen_ticks"),
                new FrozenTicksSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "forward_dash_range"),
                new ForwardDashRangeSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "damage_according_to_missing_health"),
                new DamageToMissingHealthSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                new Identifier(MOD_ID, "spellthief_impact"),
                new SpellthiefImpact()
        );
    }
}
