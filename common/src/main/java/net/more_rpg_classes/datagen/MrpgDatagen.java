package net.more_rpg_classes.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.custom.MrpgLibSpells;
import net.more_rpg_classes.sounds.MRPGLibSounds;
import net.spell_engine.api.datagen.SimpleSoundGeneratorV2;
import net.spell_engine.api.datagen.SpellGenerator;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.rpg_series.datagen.RPGSeriesDataGen;
import net.minecraft.registry.*;
import net.spell_engine.rpg_series.tags.RPGSeriesItemTags;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;
import net.spell_engine.api.spell.Spell;

import java.util.concurrent.CompletableFuture;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;


public class MrpgDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ItemTagGenerator::new);
        pack.addProvider(SpellGen::new);
        pack.addProvider(SpellTagGenerator::new);
        pack.addProvider(SoundGen::new);
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

    public static class SpellGen extends SpellGenerator {
        public SpellGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateSpells(Builder builder) {
            for (var entry: MrpgLibSpells.entries) {
                builder.add(entry.id(), entry.spell());
            }
        }
    }

    public static class SpellTagGenerator extends FabricTagProvider<Spell> {
        public SpellTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, SpellRegistry.KEY, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            MrpgLibSpells.entries.forEach(entry -> {
                if (entry.categories() != null) {
                    var tagKey = TagKey.of(SpellRegistry.KEY, Identifier.of("arsenal", entry.categories().toString().toLowerCase()));
                    var tag = getOrCreateTagBuilder(tagKey);
                    tag.addOptional(entry.id());
                }
            });
        }
    }
    public static class SoundGen extends SimpleSoundGeneratorV2 {
        public SoundGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateSounds(Builder builder) {
            builder.entries.add(new Entry(MOD_ID,
                    MRPGLibSounds.entries.stream()
                            .map(entry -> SoundEntry.withVariants(entry.id().getPath(), entry.variants()))
                            .toList()
            ));
        }
    }

}
