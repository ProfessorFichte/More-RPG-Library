package net.more_rpg_classes.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpellEngineAdvancementHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    // Helper class for generating Spell Engine advancement JSON files.

    public static JsonObject createSpellBookCreationJson(String spellPool) {
        JsonObject root = new JsonObject();
        JsonObject criteria = new JsonObject();
        JsonObject book = new JsonObject();
        JsonObject conditions = new JsonObject();

        book.addProperty("trigger", "spell_engine:spell_book_creation");
        conditions.addProperty("spell_pool", spellPool);
        book.add("conditions", conditions);
        criteria.add("book", book);
        root.add("criteria", criteria);

        return root;
    }

    public static JsonObject createOneSpellBoundJson(String spellPool) {
        JsonObject root = new JsonObject();
        JsonObject criteria = new JsonObject();
        JsonObject bind = new JsonObject();
        JsonObject conditions = new JsonObject();

        bind.addProperty("trigger", "spell_engine:spell_binding");
        conditions.addProperty("complete", false);
        conditions.addProperty("spell_pool", spellPool);
        bind.add("conditions", conditions);
        criteria.add("bind", bind);
        root.add("criteria", criteria);

        return root;
    }

    public static JsonObject createSpellCastJson(String spellTag) {
        JsonObject root = new JsonObject();
        JsonObject criteria = new JsonObject();
        JsonObject cast = new JsonObject();
        JsonObject conditions = new JsonObject();

        cast.addProperty("trigger", "spell_engine:spell_cast");
        conditions.addProperty("spell", spellTag);
        cast.add("conditions", conditions);
        criteria.add("cast", cast);
        root.add("criteria", criteria);

        return root;
    }

    public static JsonObject createAllSpellsBoundJson(String spellPool) {
        JsonObject root = new JsonObject();
        JsonObject criteria = new JsonObject();
        JsonObject bind = new JsonObject();
        JsonObject conditions = new JsonObject();

        bind.addProperty("trigger", "spell_engine:spell_binding");
        conditions.addProperty("complete", true);
        conditions.addProperty("spell_pool", spellPool);
        bind.add("conditions", conditions);
        criteria.add("bind", bind);
        root.add("criteria", criteria);

        return root;
    }

    public static JsonObject createFullArmorSetJson(String helmetId, String chestplateId, String leggingsId, String bootsId) {
        JsonObject root = new JsonObject();
        JsonObject criteria = new JsonObject();
        JsonObject armorSet = new JsonObject();
        JsonObject conditions = new JsonObject();

        armorSet.addProperty("trigger", "minecraft:inventory_changed");

        JsonArray items = new JsonArray();

        JsonObject helmet = new JsonObject();
        JsonArray helmetItems = new JsonArray();
        helmetItems.add(helmetId);
        helmet.add("items", helmetItems);
        items.add(helmet);

        JsonObject chestplate = new JsonObject();
        JsonArray chestplateItems = new JsonArray();
        chestplateItems.add(chestplateId);
        chestplate.add("items", chestplateItems);
        items.add(chestplate);

        JsonObject leggings = new JsonObject();
        JsonArray leggingsItems = new JsonArray();
        leggingsItems.add(leggingsId);
        leggings.add("items", leggingsItems);
        items.add(leggings);

        JsonObject boots = new JsonObject();
        JsonArray bootsItems = new JsonArray();
        bootsItems.add(bootsId);
        boots.add("items", bootsItems);
        items.add(boots);

        conditions.add("items", items);
        armorSet.add("conditions", conditions);
        criteria.add("armor_set", armorSet);
        root.add("criteria", criteria);

        return root;
    }

    public static void writeJson(Path outputPath, String modId, String relativePath, JsonObject json) throws IOException {
        Path targetPath = outputPath.resolve("data").resolve(modId).resolve(relativePath + ".json");
        Files.createDirectories(targetPath.getParent());

        String jsonString = GSON.toJson(json);
        Files.writeString(targetPath, jsonString);
    }
}
