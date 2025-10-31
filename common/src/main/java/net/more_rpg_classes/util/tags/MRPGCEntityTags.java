package net.more_rpg_classes.util.tags;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.more_rpg_classes.MRPGCMod;

public class MRPGCEntityTags {

    public static final TagKey<EntityType<?>> STUN_IMMUNE = register("stun_immune");
    public static final TagKey<EntityType<?>> BLEEDING_IMMUNE = register("bleeding_immune");

    public static final TagKey<EntityType<?>> WEAK_TO_WATER = register("weak_to_water");
    public static final TagKey<EntityType<?>> WEAK_TO_EARTH = register("weak_to_earth");
    public static final TagKey<EntityType<?>> RESISTANT_TO_WATER = register("resistant_to_water");

    private static TagKey<EntityType<?>> register(String id) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, MRPGCMod.id(id));

    }
}