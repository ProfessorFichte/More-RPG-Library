package net.more_rpg_classes.entity.attribute;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class MRPGCEntityAttributes{

    public static final RegistryEntry<EntityAttribute> DAMAGE_REFLECT_MODIFIER = register("damage_reflect_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> LIFESTEAL_MODIFIER = register("lifesteal_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute>RAGE_MODIFIER = register("rage_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute>SPELL_VAMPIRE = register("spell_vampire", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> AIR_FUSE_MODIFIER= register("air_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> ARCANE_FUSE_MODIFIER= register("arcane_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> EARTH_FUSE_MODIFIER= register("earth_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> FIRE_FUSE_MODIFIER= register("fire_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> FROST_FUSE_MODIFIER= register("frost_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> HEALING_FUSE_MODIFIER= register("healing_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> WATER_FUSE_MODIFIER= register("water_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> BURNING_CHANCE = register("burning_chance", 100.0, 100.0, 200.0);
    public static final RegistryEntry<EntityAttribute> STAGGER_CHANCE = register("stagger_chance", 100.0, 100.0, 200.0);
    public static final RegistryEntry<EntityAttribute> STUN_CHANCE = register("stun_chance", 100.0, 100.0, 200.0);
    public static final RegistryEntry<EntityAttribute> POISON_CHANCE = register("poison_chance", 100.0, 100.0, 200.0);
    public static final RegistryEntry<EntityAttribute> FREEZE_CHANCE = register("freeze_chance", 100.0, 100.0, 200.0);
    public static final RegistryEntry<EntityAttribute> BLEEDING_CHANCE = register("bleeding_chance", 100.0, 100.0, 200.0);
    public static final RegistryEntry<EntityAttribute> ARMOR_PIERCING = register("armor_piercing", 100.0, 100.0, 200.0);
    public static final RegistryEntry<EntityAttribute> TENACITY = register("tenacity", 100.0, 100.0, 200.0);

    private static RegistryEntry<EntityAttribute> register(final String name, double base, double min, double max) {
        EntityAttribute attribute = new ClampedEntityAttribute("attribute.name." + MOD_ID + '.' + name, base, min, max).setTracked(true);
        return Registry.registerReference(Registries.ATTRIBUTE, Identifier.of(MOD_ID, name), attribute);
    }

    public static void registerAttributes(){

    }
}
