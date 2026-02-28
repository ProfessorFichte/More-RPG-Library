package net.more_rpg_classes.datagen;

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

/**
 * Generic generator for Smithing Transform recipes
 */
public abstract class SmithingRecipeGenerator implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final FabricDataOutput output;
    protected final String modId;
    private final List<RecipeData> recipes = new ArrayList<>();

    /**
     * @param output FabricDataOutput
     * @param modId Mod ID for the recipe paths
     */
    public SmithingRecipeGenerator(FabricDataOutput output, String modId) {
        this.output = output;
        this.modId = modId;
    }

    /**
     * Implement this method to generate your recipes
     */
    public abstract void generate();

    // ==========================================
    // RECIPES WITH MOD LOAD CONDITIONS
    // ==========================================

    /**
     * Creates a Smithing Transform recipe WITH Fabric and NeoForge load conditions
     *
     * @param name Recipe name (without namespace)
     * @param base Base item
     * @param template Template item or Identifier (for items from unloaded mods)
     * @param addition Addition item or Identifier (for items from unloaded mods)
     * @param result Result item
     * @param requiredMod Required mod (e.g., "armory_rpgs")
     */
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

    /**
     * Creates a Smithing Transform recipe with multiple required mods
     */
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

    /**
     * Automatically creates Smithing recipes for ALL 4 armor pieces WITH load conditions
     *
     * @param recipeBaseName Base name for the recipes (e.g., "bounty_hunter_from_deadeye")
     * @param baseSet Base armor set
     * @param template Template item (can also be Identifier if item is not loaded)
     * @param addition Addition item (can also be Identifier if item is not loaded)
     * @param resultSet Result armor set
     * @param requiredMod Required mod
     */
    public void createArmorSetUpgrade(
            String recipeBaseName,
            Armor.Set baseSet,
            Object template,
            Object addition,
            Armor.Set resultSet,
            String requiredMod
    ) {
        // Extract result set name for better recipe naming
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

    /**
     * Helper method to extract armor set name from result set
     */
    private String extractArmorSetName(Armor.Set armorSet) {
        Identifier id = Registries.ITEM.getId((Item) armorSet.head);
        String path = id.getPath();
        // Remove "_head" suffix if present
        if (path.endsWith("_head")) {
            return path.substring(0, path.length() - 5);
        }
        return path;
    }

    /**
     * Overloaded version with multiple required mods
     */
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

    // ==========================================
    // RECIPES WITHOUT MOD LOAD CONDITIONS
    // ==========================================

    /**
     * Creates a Smithing Transform recipe WITHOUT load conditions
     * For items from your own mod that are always available
     *
     * @param name Recipe name
     * @param base Base item
     * @param template Template item or Identifier
     * @param addition Addition item or Identifier
     * @param result Result item
     */
    public void createSimpleSmithingRecipe(
            String name,
            Item base,
            Object template,
            Object addition,
            Item result
    ) {
        recipes.add(new RecipeData(name, base, template, addition, result, null, false));
    }

    /**
     * Automatically creates Smithing recipes for ALL 4 armor pieces WITHOUT load conditions
     *
     * @param recipeBaseName Base name for the recipes
     * @param baseSet Base armor set
     * @param template Template item or Identifier
     * @param addition Addition item or Identifier
     * @param resultSet Result armor set
     */
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

    // ==========================================
    // INTERNAL LOGIC
    // ==========================================

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        generate(); // Call generate to populate recipes

        return CompletableFuture.allOf(recipes.stream().map(recipeData -> {
            JsonObject recipe = buildRecipeJson(recipeData);
            Path path = output.getResolver(net.minecraft.data.DataOutput.OutputType.DATA_PACK, "recipe")
                    .resolveJson(Identifier.of(modId, recipeData.name));

            return DataProvider.writeToPath(writer, recipe, path);
        }).toArray(CompletableFuture[]::new));
    }

    private JsonObject buildRecipeJson(RecipeData data) {
        JsonObject recipe = new JsonObject();

        // Add Load Conditions if requested
        if (data.withLoadConditions && data.requiredMods != null && data.requiredMods.length > 0) {
            // Fabric Load Conditions
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

            // NeoForge Conditions
            JsonArray neoforgeConditions = new JsonArray();
            if (data.requiredMods.length == 1) {
                JsonObject neoforgeCondition = new JsonObject();
                neoforgeCondition.addProperty("type", "neoforge:mod_loaded");
                neoforgeCondition.addProperty("modid", data.requiredMods[0]);
                neoforgeConditions.add(neoforgeCondition);
            } else {
                // Multiple mods: use "and" condition
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

        // Recipe Type
        recipe.addProperty("type", "minecraft:smithing_transform");

        // Template
        JsonObject templateObj = new JsonObject();
        templateObj.addProperty("item", getItemId(data.template));
        recipe.add("template", templateObj);

        // Base
        JsonObject baseObj = new JsonObject();
        baseObj.addProperty("item", Registries.ITEM.getId(data.base).toString());
        recipe.add("base", baseObj);

        // Addition
        JsonObject additionObj = new JsonObject();
        additionObj.addProperty("item", getItemId(data.addition));
        recipe.add("addition", additionObj);

        // Result
        JsonObject resultObj = new JsonObject();
        resultObj.addProperty("id", Registries.ITEM.getId(data.result).toString());
        resultObj.addProperty("count", 1);
        recipe.add("result", resultObj);

        return recipe;
    }

    /**
     * Helper method to get item ID from either Item or Identifier
     * Prevents minecraft:air when mod is not loaded
     */
    private String getItemId(Object itemOrId) {
        if (itemOrId instanceof Identifier id) {
            return id.toString();
        } else if (itemOrId instanceof String str) {
            return str;
        } else if (itemOrId instanceof Item item) {
            Identifier id = Registries.ITEM.getId(item);
            // Check if it resolved to AIR (means item doesn't exist)
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
