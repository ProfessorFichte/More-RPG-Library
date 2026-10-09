package com.mrpg_lib.compat.spell_engine.datagen;

import com.mrpg_lib.compat.armory_rpgs.SmithingIngredients;
import com.mrpg_lib.datagen.LangCatalog;
import com.mrpg_lib.compat.spell_engine.MrpgLibSpells;
import com.mrpg_lib.sounds.MRPGLibSounds;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.spell_engine.api.datagen.SimpleSoundGeneratorV2;
import net.spell_engine.api.datagen.SpellGenerator;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.rpg_series.datagen.RPGSeriesDataGen;
import net.spell_engine.rpg_series.tags.RPGSeriesItemTags;

import java.util.concurrent.CompletableFuture;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public final class SpellEngineDatagen {
    private SpellEngineDatagen() {
    }

    public static void contributeTranslations(LangCatalog.Sink sink) {
        MrpgLibSpells.entries.forEach(entry -> {
            var id = entry.id();
            sink.add("spell." + id.getNamespace() + "." + id.getPath() + ".name", entry.title());
            sink.add("spell." + id.getNamespace() + "." + id.getPath() + ".description", entry.description());
        });
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
