package net.more_rpg_classes.custom;

import net.critical_strike.api.CriticalStrikeAttributes;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.spell_power.SpellPowerMod;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

public class MoreSpellSchools {
    public static final SpellSchool EARTH = SpellSchools.createMagic("earth", 0xbd8b00);
    public static final SpellSchool WATER = SpellSchools.createMagic("water", 0x4dd9ff);
    public static final SpellSchool AIR = SpellSchools.createMagic("air", 0xd4e3fe);
    public static final SpellSchool NATURE = SpellSchools.createMagic("nature", 0x43bf4b);
    public static SpellSchool FROST_RANGED;
    public static SpellSchool FIRE_RANGED;
    public static SpellSchool RAGE_MELEE;

    private static RegistryEntry<EntityAttribute> rangedDamageAttribute() {
        if (FabricLoader.getInstance().isModLoaded("ranged_weapon_api")) {
            return EntityAttributes_RangedWeapon.DAMAGE.entry;
        } else {
            return EntityAttributes.GENERIC_ATTACK_DAMAGE;
        }
    }

    public static void initialize() {
        FROST_RANGED = new SpellSchool(SpellSchool.Archetype.ARCHERY,
                Identifier.of(SpellPowerMod.ID, "frost_ranged"),
                0xccffff,
                DamageTypes.ARROW,
                rangedDamageAttribute());
        FIRE_RANGED = new SpellSchool(SpellSchool.Archetype.ARCHERY,
                Identifier.of(SpellPowerMod.ID, "fire_ranged"),
                0xff3300,
                DamageTypes.ARROW,
                rangedDamageAttribute());
        RAGE_MELEE = new SpellSchool(SpellSchool.Archetype.MELEE,
                Identifier.of(SpellPowerMod.ID, "rage_melee"),
                0xb3b3b3,
                DamageTypes.PLAYER_ATTACK,
                EntityAttributes.GENERIC_ATTACK_DAMAGE);

        FROST_RANGED.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            var second_power = query.entity().getAttributeValue(SpellSchools.FROST.attributeEntry);
            return query.entity().getAttributeValue(rangedDamageAttribute()) + second_power;
        });
        FIRE_RANGED.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            var second_power = query.entity().getAttributeValue(SpellSchools.FIRE.attributeEntry);
            return query.entity().getAttributeValue(rangedDamageAttribute()) + second_power;
        });

        if (FabricLoader.getInstance().isModLoaded("ranged_weapon_api")) {
            FIRE_RANGED.addSource(SpellSchool.Trait.HASTE, SpellSchool.Apply.ADD, query -> {
                var haste = query.entity().getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
                var rate = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste);
                return rate - 1;
            });
            FROST_RANGED.addSource(SpellSchool.Trait.HASTE, SpellSchool.Apply.ADD, query -> {
                var haste = query.entity().getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
                var rate = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste);
                return rate - 1;
            });
            SpellSchools.configureSpellHaste(FROST_RANGED);
            SpellSchools.configureSpellHaste(FIRE_RANGED);
        }

        RAGE_MELEE.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            return query.entity().getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) +
                    ((query.entity().getAttributeValue(MRPGCEntityAttributes.RAGE_MODIFIER)-100) / 10);
        });
        SpellSchools.configureSpellHaste(RAGE_MELEE);

        if (FabricLoader.getInstance().isModLoaded("critical_strike")) {
            FROST_RANGED.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
                var value = query.entity().getAttributeValue(CriticalStrikeAttributes.CHANCE.attributeEntry);
                return (double) CriticalStrikeAttributes.CHANCE.asChance(value);
            });
            FROST_RANGED.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
                var value = query.entity().getAttributeValue(CriticalStrikeAttributes.DAMAGE.attributeEntry);
                return CriticalStrikeAttributes.DAMAGE.asMultiplier(value) - 1;
            });
            FIRE_RANGED.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
                var value = query.entity().getAttributeValue(CriticalStrikeAttributes.CHANCE.attributeEntry);
                return (double) CriticalStrikeAttributes.CHANCE.asChance(value);
            });
            FIRE_RANGED.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
                var value = query.entity().getAttributeValue(CriticalStrikeAttributes.DAMAGE.attributeEntry);
                return CriticalStrikeAttributes.DAMAGE.asMultiplier(value) - 1;
            });
            RAGE_MELEE.addSource(SpellSchool.Trait.CRIT_CHANCE, SpellSchool.Apply.ADD, query ->  {
                var value = query.entity().getAttributeValue(CriticalStrikeAttributes.CHANCE.attributeEntry);
                return (double) CriticalStrikeAttributes.CHANCE.asChance(value);
            });
            RAGE_MELEE.addSource(SpellSchool.Trait.CRIT_DAMAGE, SpellSchool.Apply.ADD, query -> {
                var value = query.entity().getAttributeValue(CriticalStrikeAttributes.DAMAGE.attributeEntry);
                return CriticalStrikeAttributes.DAMAGE.asMultiplier(value) - 1;
            });
            SpellSchools.configureSpellCritDamage(FROST_RANGED);
            SpellSchools.configureSpellCritChance(FROST_RANGED);
            SpellSchools.configureSpellCritDamage(FIRE_RANGED);
            SpellSchools.configureSpellCritChance(FIRE_RANGED);
            SpellSchools.configureSpellCritDamage(RAGE_MELEE);
            SpellSchools.configureSpellCritChance(RAGE_MELEE);
        }

        SpellSchools.register(FROST_RANGED);
        SpellSchools.register(FIRE_RANGED);
        SpellSchools.register(RAGE_MELEE);
    }
}
