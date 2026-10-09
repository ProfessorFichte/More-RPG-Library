package com.mrpg_lib.compat.spell_power;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.critical_strike.CriticalStrikeCompat;
import com.mrpg_lib.compat.ranged_weapon_api.RangedWeaponCompat;
import com.mrpg_lib.compat.ranged_weapon_api.RangedWeaponSpellPower;
import com.mrpg_lib.entity.attribute.MRPGCEntityAttributes;
import com.mrpg_lib.util.SchoolColors;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.Identifier;
import net.spell_power.SpellPowerMod;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

public class MoreSpellSchools {
    public static final SpellSchool EARTH = SpellSchools.createMagic("earth", 0xbd8b00);
    public static final SpellSchool WATER = SpellSchools.createMagic("water", 0x4dd9ff);
    public static final SpellSchool AIR = SpellSchools.createMagic("air", SchoolColors.AIR);
    public static final SpellSchool NATURE = SpellSchools.createMagic("nature", 0x43bf4b);
    public static SpellSchool FROST_RANGED;
    public static SpellSchool FIRE_RANGED;
    public static SpellSchool RAGE_MELEE;

    private static RegistryEntry<EntityAttribute> rangedDamageAttribute() {
        if (MrpgCompat.RANGED_WEAPON_API) {
            return RangedWeaponCompat.damageAttribute();
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

        if (MrpgCompat.RANGED_WEAPON_API) {
            RangedWeaponSpellPower.configureHaste();
        }

        RAGE_MELEE.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            return query.entity().getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) +
                    ((query.entity().getAttributeValue(MRPGCEntityAttributes.RAGE_MODIFIER)-100) / 10);
        });
        SpellSchools.configureSpellHaste(RAGE_MELEE);

        if (MrpgCompat.CRITICAL_STRIKE) {
            CriticalStrikeCompat.configureSchoolAttributes();
        }

        SpellSchools.register(FROST_RANGED);
        SpellSchools.register(FIRE_RANGED);
        SpellSchools.register(RAGE_MELEE);
    }
}
