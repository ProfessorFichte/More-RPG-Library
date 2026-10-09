package com.mrpg_lib.datagen;

import com.mrpg_lib.compat.spell_engine.datagen.SpellEngineDatagen;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class MrpgDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var loader = FabricLoader.getInstance();
        if (!loader.isModLoaded("spell_engine") || !loader.isModLoaded("spell_power")) {
            throw new IllegalStateException("Datagen needs spell_engine and spell_power on the classpath; run it without -PnoCompat");
        }
        LangCatalog.contribute(SpellEngineDatagen::contributeTranslations);

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(SpellEngineDatagen.ItemTagGenerator::new);
        pack.addProvider(SpellEngineDatagen.SpellGen::new);
        pack.addProvider(SpellEngineDatagen.SpellTagGenerator::new);
        pack.addProvider(SpellEngineDatagen.SoundGen::new);
        pack.addProvider(MrpgItemTags::new);
        pack.addProvider(MrpgEntityTypeTags::new);
        pack.addProvider(MrpgDamageTypeTags::new);
        pack.addProvider(MrpgEnchantmentTags::new);
        pack.addProvider((output, registries) -> ConditionalJson.wrap(
                output, new MrpgRegistryProvider(output, registries), MrpgRegistryProvider.conditions()));
        pack.addProvider((output, registries) -> ConditionalJson.wrap(
                output, new MrpgRecipes(output, registries), MrpgRecipes.handConditions()));
        pack.addProvider(MrpgRecipes::altarProvider);
        pack.addProvider(MrpgAdvancements::new);
        pack.addProvider(MrpgModels::new);
        pack.addProvider(MrpgParticles::new);
        pack.addProvider(MrpgLang::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(RegistryKeys.DAMAGE_TYPE, MrpgRegistryData::bootstrapDamageTypes);
        registryBuilder.addRegistry(RegistryKeys.ENCHANTMENT, MrpgRegistryData::bootstrapEnchantments);
    }
}
