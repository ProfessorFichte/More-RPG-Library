package com.mrpg_lib.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class MrpgDamageTypeTags extends FabricTagProvider<DamageType> {
    public static final TagKey<DamageType> SPELL_POWER_ALL =
            TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MrpgRegistryData.SPELL_POWER, "all"));

    public MrpgDamageTypeTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR)
                .addOptional(MrpgRegistryData.MOLTEN)
                .addOptional(MrpgRegistryData.BLEEDING)
                .addOptional(MrpgRegistryData.FATAL_POISON);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_EFFECTS)
                .addOptional(MrpgRegistryData.MOLTEN)
                .addOptional(MrpgRegistryData.BLEEDING)
                .addOptional(MrpgRegistryData.FATAL_POISON);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ENCHANTMENTS)
                .addOptional(MrpgRegistryData.BLEEDING)
                .addOptional(MrpgRegistryData.FATAL_POISON);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_SHIELD)
                .addOptional(MrpgRegistryData.MOLTEN)
                .addOptional(MrpgRegistryData.BLEEDING)
                .addOptional(MrpgRegistryData.FATAL_POISON);
        var all = getOrCreateTagBuilder(SPELL_POWER_ALL);
        MrpgRegistryData.SPELL_POWER_DAMAGE_TYPES.forEach(key -> all.addOptional(key.getValue()));
    }
}
