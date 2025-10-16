package net.more_rpg_classes.custom;

import net.minecraft.util.Identifier;
import net.spell_engine.api.entity.SpellEntityPredicates;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class CustomSpellEntityPredicate {
    public static void registerCustomPredicates() {
        SpellEntityPredicates.register(
                Identifier.of(MOD_ID, "is_on_ground"),
                entity -> !entity.entity().isOnGround()
        );
        SpellEntityPredicates.register(
                Identifier.of(MOD_ID, "is_wet"),
                entity -> !entity.entity().isWet()
        );
        SpellEntityPredicates.register(
                Identifier.of(MOD_ID, "is_inside_water"),
                entity -> !entity.entity().isInsideWaterOrBubbleColumn()
        );
    }
}
