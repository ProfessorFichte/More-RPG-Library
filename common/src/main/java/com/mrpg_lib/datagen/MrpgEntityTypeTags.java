package com.mrpg_lib.datagen;

import com.mrpg_lib.util.tags.MRPGCEntityTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class MrpgEntityTypeTags extends FabricTagProvider.EntityTypeTagProvider {
    public MrpgEntityTypeTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(MRPGCEntityTags.BLEEDING_IMMUNE)
                .addOptionalTag(Identifier.of("spell_engine", "mechanical"))
                .addOptionalTag(Identifier.ofVanilla("undead"));
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(MRPGCEntityTags.POISON_IMMUNE), Registries.ENTITY_TYPE,
                EntityType.BOGGED, EntityType.WITCH);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(MRPGCEntityTags.RESISTANT_TO_WATER), Registries.ENTITY_TYPE,
                EntityType.ELDER_GUARDIAN, EntityType.GUARDIAN, EntityType.DROWNED);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(MRPGCEntityTags.STUN_IMMUNE), Registries.ENTITY_TYPE,
                EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ELDER_GUARDIAN, EntityType.WARDEN)
                .addOptionalTag(Identifier.of("c", "bosses"));
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(MRPGCEntityTags.WEAK_TO_EARTH), Registries.ENTITY_TYPE,
                EntityType.BREEZE);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(MRPGCEntityTags.WEAK_TO_WATER), Registries.ENTITY_TYPE,
                EntityType.ENDERMAN, EntityType.BLAZE);
    }
}
