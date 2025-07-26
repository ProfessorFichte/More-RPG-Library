package net.more_rpg_classes.custom;

import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.event.SpellHandlers;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class CustomSpellImpacts {

    public static void registerCustomImpacts(){
        SpellHandlers.registerCustomImpact(
                Identifier.of(MOD_ID, "knock_up"),
                new KnockUpSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                Identifier.of(MOD_ID, "stop_arrows"),
                new StopArrowsImpact()
        );
        SpellHandlers.registerCustomImpact(
                Identifier.of(MOD_ID, "knock_up_fixed"),
                new KnockUpFixedSpellImpact()
        );
        SpellHandlers.registerCustomImpact(
                Identifier.of(MOD_ID, "pull_to_caster_direct"),
                new PullInFrontOfCasterSpellImpact()
        );
    }
}
