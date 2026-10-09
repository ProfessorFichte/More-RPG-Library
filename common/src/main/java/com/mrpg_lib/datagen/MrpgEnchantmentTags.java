package com.mrpg_lib.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MrpgEnchantmentTags extends FabricTagProvider.EnchantmentTagProvider {
    private static final TagKey<Enchantment> REQUIRES_MATCHING_ATTRIBUTE =
            TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MrpgRegistryData.SPELL_POWER, "requires_matching_attribute"));

    public MrpgEnchantmentTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        for (var tag : List.of(EnchantmentTags.NON_TREASURE, MrpgRegistryData.MULTI_SCHOOL, REQUIRES_MATCHING_ATTRIBUTE)) {
            var builder = getOrCreateTagBuilder(tag);
            MrpgRegistryData.ENCHANTMENTS.forEach(key -> builder.addOptional(key.getValue()));
        }
    }
}
