package net.more_rpg_classes.util.tags;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.more_rpg_classes.MRPGCMod;

public class MRPGCEntityTags {

    public static final TagKey<EntityType<?>> STUN_IMMUNE = register("stun_immune");
    public static final TagKey<EntityType<?>> BLEEDING_IMMUNE = register("bleeding_immune");

    private static TagKey<EntityType<?>> register(String id) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, MRPGCMod.id(id));

    }
}