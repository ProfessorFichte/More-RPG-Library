package net.more_rpg_classes.custom;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_power.SpellPowerMod;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import static net.spell_power.api.SpellPowerMechanics.PERCENT_ATTRIBUTE_BASELINE;

public class MoreSpellSchools {
    public static final SpellSchool EARTH = SpellSchools.register(SpellSchools.createMagic("earth", 0xbd8b00));
    public static final SpellSchool WATER = SpellSchools.register(SpellSchools.createMagic("water", 0x4dd9ff));
    public static final SpellSchool AIR = SpellSchools.register(SpellSchools.createMagic("air", 0xd4e3fe));

    public static final SpellSchool FROST_RANGED = new SpellSchool(SpellSchool.Archetype.ARCHERY,
            Identifier.of(SpellPowerMod.ID, "frost_ranged"),
            0xccffff,
            DamageTypes.ARROW,
            EntityAttributes_RangedWeapon.DAMAGE.entry);
    public static final SpellSchool FIRE_RANGED = new SpellSchool(SpellSchool.Archetype.ARCHERY,
            Identifier.of(SpellPowerMod.ID, "fire_ranged"),
            0xff3300,
            DamageTypes.ARROW,
            EntityAttributes_RangedWeapon.DAMAGE.entry);

    public static final SpellSchool BERSERKER_MELEE = new SpellSchool(SpellSchool.Archetype.MELEE,
            Identifier.of(SpellPowerMod.ID, "berserker_melee"),
            0xb3b3b3,
            DamageTypes.PLAYER_ATTACK,
            EntityAttributes.GENERIC_ATTACK_DAMAGE);

    public static void initialize() {
        SpellSchools.register(EARTH);
        SpellSchools.register(WATER);
        SpellSchools.register(AIR);

        FROST_RANGED.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            var second_power = query.entity().getAttributeValue(SpellSchools.FROST.attributeEntry);
            return query.entity().getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry) + second_power;
        });
        FROST_RANGED.addSource(SpellSchool.Trait.HASTE, SpellSchool.Apply.ADD, query -> {
            var haste = query.entity().getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
            var rate = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste);
            return rate - 1;
        });
        SpellSchools.configureSpellHaste(FROST_RANGED);
        SpellSchools.register(FROST_RANGED);

        FIRE_RANGED.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            var second_power = query.entity().getAttributeValue(SpellSchools.FIRE.attributeEntry);
            return query.entity().getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry) + second_power;
        });
        FIRE_RANGED.addSource(SpellSchool.Trait.HASTE, SpellSchool.Apply.ADD, query -> {
            var haste = query.entity().getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
            var rate = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste);
            return rate - 1;
        });
        SpellSchools.configureSpellHaste(FIRE_RANGED);
        SpellSchools.register(FIRE_RANGED);

        BERSERKER_MELEE.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            return query.entity().getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) +
                    ((query.entity().getAttributeValue(MRPGCEntityAttributes.RAGE_MODIFIER)-100) / 50);
        });
        BERSERKER_MELEE.addSource(SpellSchool.Trait.CRIT_CHANCE, new SpellSchool.Source(SpellSchool.Apply.ADD, query ->  {
            var value = SpellPowerMod.attributesConfig.value.base_spell_critical_chance_percentage
                    + query.entity().getAttributeValue(MRPGCEntityAttributes.RAGE_MODIFIER )- 100/ 10;
            return (value/ PERCENT_ATTRIBUTE_BASELINE)-1;
        }));
        BERSERKER_MELEE.addSource(SpellSchool.Trait.CRIT_DAMAGE, new SpellSchool.Source(SpellSchool.Apply.ADD, query -> {
            var value = SpellPowerMod.attributesConfig.value.base_spell_critical_damage_percentage
                    + query.entity().getAttributeValue(MRPGCEntityAttributes.RAGE_MODIFIER )- 100/ 4;
            return (value/ PERCENT_ATTRIBUTE_BASELINE)-1;
        }));
        SpellSchools.configureSpellCritDamage(BERSERKER_MELEE);
        SpellSchools.configureSpellCritChance(BERSERKER_MELEE);
        SpellSchools.configureSpellHaste(BERSERKER_MELEE);
        SpellSchools.register(BERSERKER_MELEE);
    }
}
