package com.mrpg_lib.datagen;

import com.google.gson.JsonObject;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.item.MRPGCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.data.DataProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public class MrpgRecipes extends FabricRecipeProvider {
    public static final String RUNES_MOD = "runes";

    public record Rune(String name, Item stone) {
    }

    public record Size(String name, int handCount, int altarCount) {
    }

    public static final List<Rune> RUNES = MrpgCompat.RUNES ? List.of(
            new Rune("aqua", MRPGCItems.AQUA_STONE),
            new Rune("nature", MRPGCItems.NATURE_STONE),
            new Rune("storm", MRPGCItems.STORM_STONE),
            new Rune("terra", MRPGCItems.TERRA_STONE)
    ) : List.of();
    public static final List<Size> SIZES = List.of(
            new Size("small", 2, 8),
            new Size("medium", 4, 16)
    );

    public MrpgRecipes(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    public static String recipeName(Rune rune, Size size, String kind) {
        return rune.name() + "_rune_" + size.name() + "_" + kind;
    }

    public static ConditionalJson.Rules handConditions() {
        var rules = ConditionalJson.rules();
        for (var rune : RUNES) {
            for (var size : SIZES) {
                rules.add("recipe", Identifier.of(MOD_ID, recipeName(rune, size, "hand")), RUNES_MOD);
            }
        }
        return rules;
    }

    public static ConditionalJson.Rules altarConditions() {
        var rules = ConditionalJson.rules();
        for (var rune : RUNES) {
            for (var size : SIZES) {
                rules.add("recipe", Identifier.of(MOD_ID, recipeName(rune, size, "altar")), RUNES_MOD);
            }
        }
        return rules;
    }

    public static DataProvider altarProvider(FabricDataOutput output) {
        Map<Identifier, JsonObject> files = new LinkedHashMap<>();
        for (var rune : RUNES) {
            for (var size : SIZES) {
                var recipe = new JsonObject();
                recipe.addProperty("type", "runes:crafting");
                recipe.add("base", tag(MrpgItemTags.RUNE_BASE_STONE.id()));
                recipe.add("addition", tag(MrpgItemTags.reagent(rune.name(), size.name()).id()));
                var result = new JsonObject();
                result.addProperty("id", Registries.ITEM.getId(rune.stone()).toString());
                result.addProperty("count", size.altarCount());
                recipe.add("result", result);
                files.put(Identifier.of(MOD_ID, recipeName(rune, size, "altar")), recipe);
            }
        }
        return ConditionalJson.raw(output, "Rune Altar Recipes (" + MOD_ID + ")", files, "recipe", altarConditions());
    }

    private static JsonObject tag(Identifier id) {
        var json = new JsonObject();
        json.addProperty("tag", id.toString());
        return json;
    }

    @Override
    public String getName() {
        return "Recipes (" + MOD_ID + ")";
    }

    @Override
    public void generate(RecipeExporter exporter) {
        RecipeExporter quiet = new RecipeExporter() {
            @Override
            public void accept(Identifier recipeId, Recipe<?> recipe, AdvancementEntry advancement) {
                exporter.accept(recipeId, recipe, null);
            }

            @Override
            public Advancement.Builder getAdvancementBuilder() {
                return exporter.getAdvancementBuilder();
            }
        };

        for (var rune : RUNES) {
            for (var size : SIZES) {
                ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, rune.stone(), size.handCount())
                        .input(MrpgItemTags.RUNE_BASE_STONE)
                        .input(MrpgItemTags.reagent(rune.name(), size.name()))
                        .criterion("has_base", conditionsFromTag(MrpgItemTags.RUNE_BASE_STONE))
                        .offerTo(quiet, Identifier.of(MOD_ID, recipeName(rune, size, "hand")));
            }
        }

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, MRPGCItems.HARDENED_LEATHER)
                .pattern(" W ")
                .pattern("TRT")
                .pattern(" W ")
                .input('W', Items.HONEYCOMB)
                .input('R', Items.LEATHER)
                .input('T', Items.COAL)
                .criterion("has_leather", conditionsFromItem(Items.LEATHER))
                .offerTo(quiet, Identifier.of(MOD_ID, "hardened_leather"));
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.BOOK, 2)
                .input(Items.PAPER, 3)
                .input(MRPGCItems.HARDENED_LEATHER)
                .criterion("has_hardened_leather", conditionsFromItem(MRPGCItems.HARDENED_LEATHER))
                .offerTo(quiet, Identifier.of(MOD_ID, "hardened_leather_book"));
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.WHITE_WOOL)
                .input(MRPGCItems.POLAR_BEAR_FUR)
                .criterion("has_polar_bear_fur", conditionsFromItem(MRPGCItems.POLAR_BEAR_FUR))
                .offerTo(quiet, Identifier.of(MOD_ID, "polar_bear_fur_wool"));
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.WHITE_WOOL)
                .input(MRPGCItems.WOLF_FUR)
                .criterion("has_wolf_fur", conditionsFromItem(MRPGCItems.WOLF_FUR))
                .offerTo(quiet, Identifier.of(MOD_ID, "wolf_fur_wool"));
    }
}
