package net.more_rpg_classes.custom;

import net.spell_engine.api.datagen.SpellBuilder;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.util.TriState;
import net.spell_engine.client.util.Color;

import java.util.List;

public class SpellBuilderHelper {

    public static Spell.Impact.TargetModifier targetModifier(String entityTypeTag, TriState triState) {
        var modifier = new Spell.Impact.TargetModifier();
        var condition = new Spell.TargetCondition();
        condition.entity_type = entityTypeTag;
        modifier.conditions = List.of(condition);
        modifier.execute = triState;
        return modifier;
    }
    public static Spell createModifierAlikePassiveSpell() {
        var spell = SpellBuilder.createSpellPassive();
        spell.range = 0;
        spell.tooltip = new Spell.Tooltip();
        spell.tooltip.show_activation = false;
        return spell;
    }
    public static final Color ORANGE_COLOR = new Color(255.0F, 165.0F, 0.0F);
}
