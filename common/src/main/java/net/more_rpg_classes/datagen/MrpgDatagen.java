package net.more_rpg_classes.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.spell_engine.rpg_series.datagen.RPGSeriesDataGen;
import net.minecraft.registry.*;
import net.spell_engine.rpg_series.tags.RPGSeriesItemTags;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;


import java.util.concurrent.CompletableFuture;


public class MrpgDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ItemTagGenerator::new);
        pack.addProvider(LangGenerator::new);
    }

    public static class ItemTagGenerator extends RPGSeriesDataGen.ItemTagGenerator {
        public ItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            var tierTag = RPGSeriesItemTags.LootTiers.get(5, RPGSeriesItemTags.LootCategory.ARMORS);
            SmithingIngredients.ENTRIES.forEach(entry -> {
                var tag = getOrCreateTagBuilder(tierTag);
                tag.addOptional(entry.id());
            });
        }
    }
    public static class LangGenerator extends FabricLanguageProvider {
        protected LangGenerator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
            SmithingIngredients.ENTRIES.forEach(entry -> {
                translationBuilder.add(entry.id().toTranslationKey("item"), entry.translations().itemName());
                translationBuilder.add(entry.appliesToTranslationKey(), entry.appliesToClassesTranslation());
            });
        }
    }

    public static class ModelProvider extends FabricModelProvider {
        public ModelProvider(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

        }

        @Override
        public void generateItemModels(ItemModelGenerator itemModelGenerator) {
            SmithingIngredients.ENTRIES.forEach(entry -> {
                itemModelGenerator.register(entry.item().get(), Models.GENERATED);
            });
        }
    }

}
