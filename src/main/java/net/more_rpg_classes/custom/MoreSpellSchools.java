package net.more_rpg_classes.custom;

import net.minecraft.enchantment.EnchantmentHelper;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import net.spell_power.api.enchantment.Enchantments_SpellPower;

public class MoreSpellSchools {
    public static final SpellSchool EARTH = SpellSchools.register(SpellSchools.createMagic("earth", true,0xbd8b00));
    public static final SpellSchool WATER = SpellSchools.register(SpellSchools.createMagic("water", true,0x4dd9ff));
    public static final SpellSchool AIR = SpellSchools.register(SpellSchools.createMagic("air", true,0xd4e3fe));

    public static void initialize() {

        AIR.attributeManagement = SpellSchool.Manage.INTERNAL;
        AIR.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            var enchantment = Enchantments_SpellPower.SPELL_POWER;
            var power = query.entity().getAttributeValue(MoreSpellSchools.AIR.attribute);
            var level = EnchantmentHelper.getLevel(enchantment, query.entity().getMainHandStack());
            power *=  (0.05 * level);
            return power;
        });
        EARTH.attributeManagement = SpellSchool.Manage.INTERNAL;
        EARTH.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            var enchantment = Enchantments_SpellPower.SPELL_POWER;
            var power = query.entity().getAttributeValue(MoreSpellSchools.EARTH.attribute);
            var level = EnchantmentHelper.getLevel(enchantment, query.entity().getMainHandStack());
            power *=  (0.05 * level);
            return power;
        });
        WATER.attributeManagement = SpellSchool.Manage.INTERNAL;
        WATER.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            var enchantment = Enchantments_SpellPower.SPELL_POWER;
            var power = query.entity().getAttributeValue(MoreSpellSchools.WATER.attribute);
            var level = EnchantmentHelper.getLevel(enchantment, query.entity().getMainHandStack());
            power *=  (0.05 * level);
            return power;
        });

        SpellSchools.register(EARTH);
        SpellSchools.register(WATER);
        SpellSchools.register(AIR);
    }
}
