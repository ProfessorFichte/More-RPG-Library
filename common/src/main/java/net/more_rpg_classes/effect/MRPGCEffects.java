package net.more_rpg_classes.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.spell_engine.api.config.AttributeModifier;
import net.spell_engine.api.config.ConfigFile;
import net.spell_engine.api.config.EffectConfig;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.entity.SpellEngineAttributes;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;

import java.util.ArrayList;
import java.util.List;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class MRPGCEffects {
    public static final List<Effects.Entry> entries = new ArrayList<>();
    private static Effects.Entry add(Effects.Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static final Effects.Entry MOLTEN_ARMOR = add(new Effects.Entry(
            Identifier.of(MOD_ID, "molten_armor"),
            "Molten Armor",
            "Reduces armor, armor toughness and damages the target if it wears armor.",
            new MoltenArmorEffect(StatusEffectCategory.HARMFUL, 0xdd4e00),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            EntityAttributes.GENERIC_ARMOR.getIdAsString(),
                            -0.1F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ),
                    new AttributeModifier(
                            EntityAttributes.GENERIC_ARMOR_TOUGHNESS.getIdAsString(),
                            -1.0F,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            ))
    ));

    public static final Effects.Entry FROZEN_SOLID = add(new Effects.Entry(
            Identifier.of(MOD_ID, "frozen_solid"),
            "Frozen Solid",
            "Cant move, attack or jump, takes additional damage if hit during active effect.",
            new FrozenSolidEffect(StatusEffectCategory.HARMFUL, 0x3beeff)
                    .setVulnerability(SpellSchools.FROST, new SpellPower.Vulnerability(0, 0.1F, 0.2F)),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            SpellEngineAttributes.DAMAGE_TAKEN.id.toString(),
                            0.15F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static final Effects.Entry COLLECTED_SOUL = add(new Effects.Entry(
            Identifier.of(MOD_ID, "collected_soul"),
            "Collected Soul",
            "Increases soul spell power per stack.",
            new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0x01d9cf),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            SpellSchools.SOUL.attributeEntry.getIdAsString(),
                            0.10F,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            ))
    ));

    public static final Effects.Entry GRIEVOUS_WOUNDS = add(new Effects.Entry(
            Identifier.of(MOD_ID, "grievous_wounds"),
            "Grievous Wounds",
            "Reduced Healing and increased incoming damage.",
            new CustomStatusEffect(StatusEffectCategory.HARMFUL, 0x01d9cf),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            SpellEngineAttributes.DAMAGE_TAKEN.id.toString(),
                            0.05F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ),
                    new AttributeModifier(
                            SpellEngineAttributes.HEALING_TAKEN.id.toString(),
                            -0.1F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static final Effects.Entry FROSTED = add(new Effects.Entry(
            Identifier.of(MOD_ID, "frosted"),
            "Frosted",
            "Decreased Movement speed.",
            new FrostedEffect(StatusEffectCategory.HARMFUL, 0x3beeff),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            EntityAttributes.GENERIC_MOVEMENT_SPEED.getIdAsString(),
                            -0.05F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static final Effects.Entry BLEEDING = add(new Effects.Entry(
            Identifier.of(MOD_ID, "bleeding"),
            "Bleeding",
            "Damages the target overtime.",
            new BleedingEffect(StatusEffectCategory.HARMFUL, 0xdd4e00),
            new EffectConfig(List.of())
    ));

    public static final Effects.Entry FEAR = add(new Effects.Entry(
            Identifier.of(MOD_ID, "fear"),
            "Fear",
            "Reduces attack damage.",
            new CustomStatusEffect(StatusEffectCategory.HARMFUL, 0x01d9cf),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            EntityAttributes.GENERIC_ATTACK_DAMAGE.getIdAsString(),
                            -0.25F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static final Effects.Entry STAGGER = add(new Effects.Entry(
            Identifier.of(MOD_ID, "stagger"),
            "Stagger",
            "Reduces Armor, Attack Damage & Movement Speed and incapacitates the target.",
            new CustomStatusEffect(StatusEffectCategory.HARMFUL, 0xb3b3b3),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            EntityAttributes.GENERIC_ATTACK_DAMAGE.getIdAsString(),
                            -0.80F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ),
                    new AttributeModifier(
                            EntityAttributes.GENERIC_ARMOR.getIdAsString(),
                            -0.80F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ),
                    new AttributeModifier(
                            EntityAttributes.GENERIC_MOVEMENT_SPEED.getIdAsString(),
                            -0.80F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static final Effects.Entry SOAKED = add(new Effects.Entry(
            Identifier.of(MOD_ID, "soaked"),
            "Soaked",
            "Soaking the target with water extinguishing fire, more vulnerable to frost, lightning and water spells.",
            new SoakedEffect(StatusEffectCategory.HARMFUL, 0x01d9cf)
                    .setVulnerability(SpellSchools.LIGHTNING, new SpellPower.Vulnerability(0.15F, 0.1F, 0))
                    .setVulnerability(SpellSchools.FROST, new SpellPower.Vulnerability(0.15F, 0, 0.3F)),
            new EffectConfig(List.of())
    ));

    public static final Effects.Entry CARVE = add(new Effects.Entry(
            Identifier.of(MOD_ID, "carve"),
            "Carve",
            "Reduces armor and increases damage taken.",
            new CustomStatusEffect(StatusEffectCategory.HARMFUL, 0xdd4e00),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            EntityAttributes.GENERIC_ARMOR.getIdAsString(),
                            -0.1F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    new AttributeModifier(
                            SpellEngineAttributes.DAMAGE_TAKEN.id.toString(),
                            0.05F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
            ))
    ));
      public static final Effects.Entry FATAL_POISON = add(new Effects.Entry(
            Identifier.of(MOD_ID, "fatal_poison"),
            "Fatal Poison",
            "Inflicts damage over time, and can kill both undead and non-undead mobs.",
            new FatalPoisonEffect(StatusEffectCategory.HARMFUL, 0x5d2f8c).interval(3),
            new EffectConfig(List.of(
            ))
    ));

    public static final Effects.Entry IGNITED = add(new Effects.Entry(
            Identifier.of(MOD_ID, "ignited"),
            "Ignited",
            "Burns the target, dealing damage over time and reduces healing. The target cannot move or attack.",
            new IgnitedEffect(StatusEffectCategory.HARMFUL, 0xFF6600),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            SpellEngineAttributes.HEALING_TAKEN.id.toString(),
                            -0.1F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )))
    ));

    public static void register(ConfigFile.Effects config) {
        for (var entry : entries) {
            Synchronized.configure(entry.effect, true);
        }

        RemoveOnHit.configure(FROZEN_SOLID.effect, RemoveOnHit.Trigger.DIRECT_HIT, 1, 1);

        ActionImpairing.configure(FROZEN_SOLID.effect, MRPGCActionImpairing.FROZEN);
        ActionImpairing.configure(IGNITED.effect, MRPGCActionImpairing.IGNITED);
        ActionImpairing.configure(FEAR.effect, EntityActionsAllowed.INCAPACITATE);
        ActionImpairing.configure(STAGGER.effect, EntityActionsAllowed.INCAPACITATE);


        Effects.register(entries, config.effects);
    }
}
