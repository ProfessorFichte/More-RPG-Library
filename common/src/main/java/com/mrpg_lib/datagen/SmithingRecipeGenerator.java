package com.mrpg_lib.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.spell_engine.rpg_series.item.Armor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SmithingRecipeGenerator implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final FabricDataOutput output;
    protected final String modId;
    private final List<RecipeData> recipes = new ArrayList<>();

    public SmithingRecipeGenerator(FabricDataOutput output, String modId) {
        this.output = output;
        this.modId = modId;
    }

    public abstract void generate();

    public void createSmithingTransformRecipe(
            String name,
            Item base,
            Object template,
            Object addition,
            Item result,
            String requiredMod
    ) {
        createSmithingTransformRecipe(name, base, template, addition, result, new String[]{requiredMod});
    }

    public void createSmithingTransformRecipe(
            String name,
            Item base,
            Object template,
            Object addition,
            Item result,
            String[] requiredMods
    ) {
        recipes.add(new RecipeData(name, base, template, addition, result, requiredMods, true));
    }

    public void createArmorSetUpgrade(
            String recipeBaseName,
            Armor.Set baseSet,
            Object template,
            Object addition,
            Armor.Set resultSet,
            String requiredMod
    ) {
        String resultSetName = extractArmorSetName(resultSet);

        createSmithingTransformRecipe(
                recipeBaseName + "_" + resultSetName + "_head",
                (Item) baseSet.head,
                template,
                addition,
                (Item) resultSet.head,
                requiredMod
        );

        createSmithingTransformRecipe(
                recipeBaseName + "_" + resultSetName + "_chest",
                (Item) baseSet.chest,
                template,
                addition,
                (Item) resultSet.chest,
                requiredMod
        );

        createSmithingTransformRecipe(
                recipeBaseName + "_" + resultSetName + "_legs",
                (Item) baseSet.legs,
                template,
                addition,
                (Item) resultSet.legs,
                requiredMod
        );

        createSmithingTransformRecipe(
                recipeBaseName + "_" + resultSetName + "_feet",
                (Item) baseSet.feet,
                template,
                addition,
                (Item) resultSet.feet,
                requiredMod
        );
    }

    private String extractArmorSetName(Armor.Set armorSet) {
        Identifier id = Registries.ITEM.getId((Item) armorSet.head);
        String path = id.getPath();
        if (path.endsWith("_head")) {
            return path.substring(0, path.length() - 5);
        }
        return path;
    }

    public void createArmorSetUpgrade(
            String recipeBaseName,
            Armor.Set baseSet,
            Object template,
            Object addition,
            Armor.Set resultSet,
            String[] requiredMods
    ) {
        String resultSetName = extractArmorSetName(resultSet);

        createSmithingTransformRecipe(
                recipeBaseName + "_" + resultSetName + "_head",
                (Item) baseSet.head,
                template,
                addition,
                (Item) resultSet.head,
                requiredMods
        );

        createSmithingTransformRecipe(
                recipeBaseName + "_" + resultSetName + "_chest",
                (Item) baseSet.chest,
                template,
                addition,
                (Item) resultSet.chest,
                requiredMods
        );

        createSmithingTransformRecipe(
                recipeBaseName + "_" + resultSetName + "_legs",
                (Item) baseSet.legs,
                template,
                addition,
                (Item) resultSet.legs,
                requiredMods
        );

        createSmithingTransformRecipe(
                recipeBaseName + "_" + resultSetName + "_feet",
                (Item) baseSet.feet,
                template,
                addition,
                (Item) resultSet.feet,
                requiredMods
        );
    }

    public void createSimpleSmithingRecipe(
            String name,
            Item base,
            Object template,
            Object addition,
            Item result
    ) {
        recipes.add(new RecipeData(name, base, template, addition, result, null, false));
    }

    public void createSimpleArmorSetUpgrade(
            String recipeBaseName,
            Armor.Set baseSet,
            Object template,
            Object addition,
            Armor.Set resultSet
    ) {
        String resultSetName = extractArmorSetName(resultSet);

        createSimpleSmithingRecipe(
                recipeBaseName + "_" + resultSetName + "_head",
                (Item) baseSet.head,
                template,
                addition,
                (Item) resultSet.head
        );

        createSimpleSmithingRecipe(
                recipeBaseName + "_" + resultSetName + "_chest",
                (Item) baseSet.chest,
                template,
                addition,
                (Item) resultSet.chest
        );

        createSimpleSmithingRecipe(
                recipeBaseName + "_" + resultSetName + "_legs",
                (Item) baseSet.legs,
                template,
                addition,
                (Item) resultSet.legs
        );

        createSimpleSmithingRecipe(
                recipeBaseName + "_" + resultSetName + "_feet",
                (Item) baseSet.feet,
                template,
                addition,
                (Item) resultSet.feet
        );
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        generate();

        return CompletableFuture.allOf(recipes.stream().map(recipeData -> {
            JsonObject recipe = buildRecipeJson(recipeData);
            Path path = output.getResolver(net.minecraft.data.DataOutput.OutputType.DATA_PACK, "recipe")
                    .resolveJson(Identifier.of(modId, recipeData.name));

            return DataProvider.writeToPath(writer, recipe, path);
        }).toArray(CompletableFuture[]::new));
    }

    private JsonObject buildRecipeJson(RecipeData data) {
        JsonObject recipe = new JsonObject();

        if (data.withLoadConditions && data.requiredMods != null && data.requiredMods.length > 0) {
            JsonArray fabricLoadConditions = new JsonArray();
            JsonObject fabricCondition = new JsonObject();
            fabricCondition.addProperty("condition", "fabric:all_mods_loaded");
            JsonArray modValues = new JsonArray();
            for (String mod : data.requiredMods) {
                modValues.add(mod);
            }
            fabricCondition.add("values", modValues);
            fabricLoadConditions.add(fabricCondition);
            recipe.add("fabric:load_conditions", fabricLoadConditions);

            JsonArray neoforgeConditions = new JsonArray();
            if (data.requiredMods.length == 1) {
                JsonObject neoforgeCondition = new JsonObject();
                neoforgeCondition.addProperty("type", "neoforge:mod_loaded");
                neoforgeCondition.addProperty("modid", data.requiredMods[0]);
                neoforgeConditions.add(neoforgeCondition);
            } else {
                JsonObject andCondition = new JsonObject();
                andCondition.addProperty("type", "neoforge:and");
                JsonArray innerConditions = new JsonArray();
                for (String mod : data.requiredMods) {
                    JsonObject modCondition = new JsonObject();
                    modCondition.addProperty("type", "neoforge:mod_loaded");
                    modCondition.addProperty("modid", mod);
                    innerConditions.add(modCondition);
                }
                andCondition.add("conditions", innerConditions);
                neoforgeConditions.add(andCondition);
            }
            recipe.add("neoforge:conditions", neoforgeConditions);
        }

        recipe.addProperty("type", "minecraft:smithing_transform");

        JsonObject templateObj = new JsonObject();
        templateObj.addProperty("item", getItemId(data.template));
        recipe.add("template", templateObj);

        JsonObject baseObj = new JsonObject();
        baseObj.addProperty("item", Registries.ITEM.getId(data.base).toString());
        recipe.add("base", baseObj);

        JsonObject additionObj = new JsonObject();
        additionObj.addProperty("item", getItemId(data.addition));
        recipe.add("addition", additionObj);

        JsonObject resultObj = new JsonObject();
        resultObj.addProperty("id", Registries.ITEM.getId(data.result).toString());
        resultObj.addProperty("count", 1);
        recipe.add("result", resultObj);

        return recipe;
    }

    private String getItemId(Object itemOrId) {
        if (itemOrId instanceof Identifier id) {
            return id.toString();
        } else if (itemOrId instanceof String str) {
            return str;
        } else if (itemOrId instanceof Item item) {
            Identifier id = Registries.ITEM.getId(item);
            if (id.equals(Registries.ITEM.getId(net.minecraft.item.Items.AIR))) {
                throw new IllegalStateException("Item resolved to minecraft:air - use Identifier instead of Item for cross-mod items!");
            }
            return id.toString();
        }
        throw new IllegalArgumentException("Template/Addition must be Item, Identifier, or String, got: " + itemOrId.getClass());
    }

    @Override
    public String getName() {
        return "Smithing Recipes (" + modId + ")";
    }

    private record RecipeData(
            String name,
            Item base,
            Object template,
            Object addition,
            Item result,
            String[] requiredMods,
            boolean withLoadConditions
    ) {}
}
