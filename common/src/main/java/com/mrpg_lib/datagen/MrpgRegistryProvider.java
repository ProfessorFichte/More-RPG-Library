package com.mrpg_lib.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class MrpgRegistryProvider extends FabricDynamicRegistryProvider {
    public MrpgRegistryProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        var damageTypes = registries.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE);
        entries.addAll(damageTypes);
        MrpgRegistryData.SPELL_POWER_DAMAGE_TYPES.forEach(key -> entries.add(damageTypes, key));
        entries.addAll(registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT));
    }

    @Override
    public String getName() {
        return "Damage Types and Enchantments";
    }

    public static ConditionalJson.Rules conditions() {
        var rules = ConditionalJson.rules();
        MrpgRegistryData.SPELL_POWER_DAMAGE_TYPES.forEach(key ->
                rules.add("damage_type", key.getValue(), MrpgRegistryData.SPELL_POWER));
        MrpgRegistryData.ENCHANTMENTS.forEach(key ->
                rules.add("enchantment", key.getValue(), MrpgRegistryData.SPELL_POWER));
        return rules;
    }
}
