package com.mrpg_lib.effect;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.spell_engine.SpellEngineEffects;
import com.mrpg_lib.compat.spell_power.SpellPowerEffects;
import com.mrpg_lib.config.AttributeModifier;
import com.mrpg_lib.config.ConfigFile;
import com.mrpg_lib.config.EffectConfig;
import com.mrpg_lib.util.SchoolColors;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public class MRPGCEffects {
    public static final List<EffectEntry> entries = new ArrayList<>();
    private static final String DAMAGE_TAKEN = "spell_engine:damage_taken";
    private static final String HEALING_TAKEN = "spell_engine:healing_taken";
    private static final String SOUL_POWER = "spell_power:soul";
    private static final String CRITICAL_CHANCE = "spell_power:critical_chance";
    private static final String CRITICAL_DAMAGE = "spell_power:critical_damage";

    private static EffectEntry add(EffectEntry entry) {
        entries.add(entry);
        return entry;
    }

    public static final EffectEntry MOLTEN_ARMOR = add(new EffectEntry(
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

    public static final EffectEntry FROZEN_SOLID = add(new EffectEntry(
            Identifier.of(MOD_ID, "frozen_solid"),
            "Frozen Solid",
            "Cant move, attack or jump, takes additional damage if hit during active effect.",
            frozenSolid(),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            DAMAGE_TAKEN,
                            0.15F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static final EffectEntry COLLECTED_SOUL = add(new EffectEntry(
            Identifier.of(MOD_ID, "collected_soul"),
            "Collected Soul",
            "Increases soul spell power per stack.",
            new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x01d9cf),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            SOUL_POWER,
                            0.10F,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            ))
    ));

    public static final EffectEntry GRIEVOUS_WOUNDS = add(new EffectEntry(
            Identifier.of(MOD_ID, "grievous_wounds"),
            "Grievous Wounds",
            "Reduced Healing and increased incoming damage.",
            new BasicStatusEffect(StatusEffectCategory.HARMFUL, 0x01d9cf),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            DAMAGE_TAKEN,
                            0.05F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ),
                    new AttributeModifier(
                            HEALING_TAKEN,
                            -0.1F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static final EffectEntry FROSTED = add(new EffectEntry(
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

    // Replaced by spell_engine:bleed.
    // Still registered so old worlds that use this effect id don't break.
    @Deprecated
    public static final EffectEntry BLEEDING = add(new EffectEntry(
            Identifier.of(MOD_ID, "bleeding"),
            "Bleeding",
            "Damages the target overtime.",
            new BleedingEffect(StatusEffectCategory.HARMFUL, 0xdd4e00),
            new EffectConfig(List.of())
    ));

    public static final EffectEntry FEAR = add(new EffectEntry(
            Identifier.of(MOD_ID, "fear"),
            "Fear",
            "Reduces attack damage.",
            new BasicStatusEffect(StatusEffectCategory.HARMFUL, 0x01d9cf),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            EntityAttributes.GENERIC_ATTACK_DAMAGE.getIdAsString(),
                            -0.25F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static final EffectEntry STAGGER = add(new EffectEntry(
            Identifier.of(MOD_ID, "stagger"),
            "Stagger",
            "Reduces Armor, Attack Damage & Movement Speed and incapacitates the target.",
            new BasicStatusEffect(StatusEffectCategory.HARMFUL, 0xb3b3b3),
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

    public static final EffectEntry SOAKED = add(new EffectEntry(
            Identifier.of(MOD_ID, "soaked"),
            "Soaked",
            "Soaking the target with water extinguishing fire, more vulnerable to frost, lightning and water spells.",
            soaked(),
            new EffectConfig(List.of())
    ));

    public static final EffectEntry CARVE = add(new EffectEntry(
            Identifier.of(MOD_ID, "carve"),
            "Carve",
            "Reduces armor and increases damage taken.",
            new BasicStatusEffect(StatusEffectCategory.HARMFUL, 0xdd4e00),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            EntityAttributes.GENERIC_ARMOR.getIdAsString(),
                            -0.1F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    new AttributeModifier(
                            DAMAGE_TAKEN,
                            0.05F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
            ))
    ));
      public static final EffectEntry FATAL_POISON = add(new EffectEntry(
            Identifier.of(MOD_ID, "fatal_poison"),
            "Fatal Poison",
            "Inflicts damage over time, and can kill both undead and non-undead mobs.",
            new FatalPoisonEffect(StatusEffectCategory.HARMFUL, 0x5d2f8c),
            new EffectConfig(List.of(
            ))
    ));

    public static final EffectEntry IGNITED = add(new EffectEntry(
            Identifier.of(MOD_ID, "ignited"),
            "Ignited",
            "Burns the target, dealing damage over time and reduces healing. The target cannot move or attack.",
            new IgnitedEffect(StatusEffectCategory.HARMFUL, 0xFF6600),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            HEALING_TAKEN,
                            -0.1F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )))
    ));
    public static final EffectEntry WITHERS_CURSE = add(new EffectEntry(
            Identifier.of(MOD_ID, "withers_curse"),
            "Wither's Curse",
            "Increases Incoming Damage, the amplifier increases with each harmful status effect.",
            new WithersCurseEffect(StatusEffectCategory.HARMFUL, 0x2a1b01),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            DAMAGE_TAKEN,
                            0.05F,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
            ))
    ));

    public static EffectEntry ARCANE_PRECISION = add(new EffectEntry(Identifier.of(MOD_ID, "arcane_precision"),
            "Arcane Precision",
            "Makes targets more vulnerable to Arcane Spell Damage & Crits",
            arcanePrecision(),
            new EffectConfig(List.of())
    ));
    public static EffectEntry ZEPHYRS_SPEED = add(new EffectEntry(Identifier.of(MOD_ID, "zephyrs_speed"),
            "Zephyrs Speed",
            "Increasing the Crit Chance & Movement Speed of the caster.",
            new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, SchoolColors.AIR),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    CRITICAL_CHANCE,
                                    0.03F,
                                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            ),
                            new AttributeModifier(
                                    EntityAttributes.GENERIC_MOVEMENT_SPEED.getIdAsString(),
                                    0.05F,
                                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));

    public static final EffectEntry SIRENS_TEAR = add(new EffectEntry(
            Identifier.of(MOD_ID, "sirens_tear"),
            "Siren's Tear",
            "Heals a percentage of max health every second, scaling with missing health.",
            new SirensTearEffect(StatusEffectCategory.BENEFICIAL, 0x7abfff),
            new EffectConfig(List.of())
    ));

    public static final EffectEntry DUELISTS_FOCUS_OWNER = add(new EffectEntry(
            Identifier.of(MOD_ID, "duelists_focus_owner"),
            "Duelist's Focus",
            "Reduces incoming damage by 25% from attackers who are not marked.",
            new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xCC6600),
            new EffectConfig(List.of())
    ));
    public static final EffectEntry DUELISTS_FOCUS_TARGET = add(new EffectEntry(
            Identifier.of(MOD_ID, "duelists_focus_target"),
            "Marked by the Duelist",
            "Other entities deal reduced damage to the attacker who marked you and you receive increased damage.",
            new BasicStatusEffect(StatusEffectCategory.HARMFUL, 0xCC6600),
            new EffectConfig(List.of())
    ));
    public static float critDamageIncrease = 0.3F;
    public static EffectEntry DRAGON_SLAYERS_FURY = add(new EffectEntry(Identifier.of(MOD_ID, "dragonslayers_fury"),
            "Dragonslayer's Fury",
            "Increases Critical Damage.",
            new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x9999ff),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    CRITICAL_DAMAGE,
                                    critDamageIncrease,
                                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            ),
                            new AttributeModifier(
                                    "critical_strike:damage",
                                    critDamageIncrease,
                                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));



    private static StatusEffect frozenSolid() {
        return MrpgCompat.SPELL_POWER
                ? SpellPowerEffects.frozenSolid(StatusEffectCategory.HARMFUL, 0x3beeff)
                : new FrozenSolidEffect(StatusEffectCategory.HARMFUL, 0x3beeff);
    }

    private static StatusEffect soaked() {
        return MrpgCompat.SPELL_POWER
                ? SpellPowerEffects.soaked(StatusEffectCategory.HARMFUL, 0x01d9cf)
                : new SoakedEffect(StatusEffectCategory.HARMFUL, 0x01d9cf);
    }

    private static StatusEffect arcanePrecision() {
        return MrpgCompat.SPELL_POWER
                ? SpellPowerEffects.arcanePrecision(StatusEffectCategory.HARMFUL)
                : new BasicStatusEffect(StatusEffectCategory.HARMFUL, SchoolColors.ARCANE);
    }

    public static void register(ConfigFile.Effects config) {
        if (MrpgCompat.SPELL_ENGINE) {
            SpellEngineEffects.configure(entries);
        }
        EffectEntry.register(entries, config.effects);
    }
}
