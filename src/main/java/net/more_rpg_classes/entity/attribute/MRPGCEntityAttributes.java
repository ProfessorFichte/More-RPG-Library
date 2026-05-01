package net.more_rpg_classes.entity.attribute;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;

public class MRPGCEntityAttributes {
    public static EntityAttribute DAMAGE_REFLECT_MODIFIER = createAttribute("damage_reflect_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute ARCANE_FUSE_MODIFIER = createAttribute("arcane_fuse_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute AIR_FUSE_MODIFIER = createAttribute("air_fuse_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute EARTH_FUSE_MODIFIER = createAttribute("earth_fuse_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute FIRE_FUSE_MODIFIER = createAttribute("fire_fuse_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute FROST_FUSE_MODIFIER = createAttribute("frost_fuse_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute HEALING_FUSE_MODIFIER = createAttribute("healing_fuse_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute WATER_FUSE_MODIFIER = createAttribute("water_fuse_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute LIFESTEAL_MODIFIER = createAttribute("lifesteal_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute RAGE_MODIFIER = createAttribute("rage_modifier", 100.0, 100.0, 1024.0);
    public static EntityAttribute SPELL_VAMPIRE = createAttribute("spell_vampire", 100.0, 100.0, 1024.0);
    public static EntityAttribute BURNING_CHANCE = createAttribute("burning_chance", 100.0, 100.0, 200.0);
    public static EntityAttribute STAGGER_CHANCE = createAttribute("stagger_chance", 100.0, 100.0, 200.0);
    public static EntityAttribute STUN_CHANCE = createAttribute("stun_chance", 100.0, 100.0, 200.0);
    public static EntityAttribute POISON_CHANCE = createAttribute("poison_chance", 100.0, 100.0, 200.0);
    public static EntityAttribute FREEZE_CHANCE = createAttribute("freeze_chance", 100.0, 100.0, 200.0);
    public static EntityAttribute BLEEDING_CHANCE = createAttribute("bleeding_chance", 100.0, 100.0, 200.0);
    public static EntityAttribute ARMOR_PIERCING = createAttribute("armor_piercing", 100.0, 100.0, 200.0);
    public static EntityAttribute TENACITY = createAttribute("tenacity", 100.0, 100.0, 200.0);
    public static EntityAttribute HEALING_TAKEN = createAttribute("healing_taken", 100.0, 0.0, 2048.0);
    public static EntityAttribute DAMAGE_TAKEN = createAttribute("damage_taken", 100.0, 0.0, 2048.0);

    public static void registerAttributes() {
        register("damage_reflect_modifier", DAMAGE_REFLECT_MODIFIER);
        register("arcane_fuse_modifier", ARCANE_FUSE_MODIFIER);
        register("air_fuse_modifier", AIR_FUSE_MODIFIER);
        register("earth_fuse_modifier", EARTH_FUSE_MODIFIER);
        register("fire_fuse_modifier", FIRE_FUSE_MODIFIER);
        register("frost_fuse_modifier", FROST_FUSE_MODIFIER);
        register("healing_fuse_modifier", HEALING_FUSE_MODIFIER);
        register("water_fuse_modifier", WATER_FUSE_MODIFIER);
        register("lifesteal_modifier", LIFESTEAL_MODIFIER);
        register("rage_modifier", RAGE_MODIFIER);
        register("spell_vampire", SPELL_VAMPIRE);
        register("burning_chance", BURNING_CHANCE);
        register("stagger_chance", STAGGER_CHANCE);
        register("stun_chance", STUN_CHANCE);
        register("poison_chance", POISON_CHANCE);
        register("freeze_chance", FREEZE_CHANCE);
        register("bleeding_chance", BLEEDING_CHANCE);
        register("armor_piercing", ARMOR_PIERCING);
        register("tenacity", TENACITY);
        register("healing_taken", HEALING_TAKEN);
        register("damage_taken", DAMAGE_TAKEN);
    }

    public static EntityAttribute register(String id, EntityAttribute attribute) {
        return Registry.register(Registries.ATTRIBUTE, new Identifier(MRPGCMod.MOD_ID, id), attribute);
    }

    private static EntityAttribute createAttribute(final String name, double base, double min, double max) {
        return new ClampedEntityAttribute("attribute.name.generic." + MRPGCMod.MOD_ID + '.' + name, base, min, max).setTracked(true);
    }
}
