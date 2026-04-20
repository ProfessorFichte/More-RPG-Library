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
    public static Spell.TargetCondition healthRangeCondition(float below, float above) {
        var deadCondition = new Spell.TargetCondition();
        deadCondition.health_percent_below = below;
        deadCondition.health_percent_above = above;
        return deadCondition;
    }

    /// COLORS
    public static final Color ORANGE_COLOR = new Color(255.0F, 165.0F, 0.0F);
    public static final Color GOLD = Color.from(0xffd700);
    public static final Color CYAN = Color.from(0x00ffff);
    public static final Color BRIGHT_GREEN = Color.from(0x8efea1);
    public static final Color BRIGHT_CYAN = Color.from(0x83f4e6);
    public static final Color MID_MAGENTA = Color.from(0xa082ce);
    public static final Color MAGENTA = Color.from(0x7c37f5);
    public static final Color BRIGHT_MAGENTA = Color.from(0xdd6af7);
    public static final Color YELLOW_GREEN = Color.from(0xaffe0d);

}
