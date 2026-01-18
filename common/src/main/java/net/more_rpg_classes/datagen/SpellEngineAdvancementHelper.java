package net.more_rpg_classes.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpellEngineAdvancementHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum SpellEngineTrigger {
        SPELL_BOOK_CREATION("spell_engine:spell_book_creation"),
        SPELL_BINDING("spell_engine:spell_binding"),
        SPELL_CAST("spell_engine:spell_cast");

        private final String triggerId;

        SpellEngineTrigger(String triggerId) {
            this.triggerId = triggerId;
        }

        public String getId() {
            return triggerId;
        }
    }

    public record AdvancementEntry(
            String modId,
            String name,
            String title,
            String description
    ) {
        public String titleKey() {
            return "advancements." + modId + "." + name + ".title";
        }

        public String descriptionKey() {
            return "advancements." + modId + "." + name + ".description";
        }

        public Identifier id() {
            return Identifier.of(modId, name);
        }
    }

    public static JsonObject criteriaSpellBookCreation(String spellPool) {
        JsonObject criteria = new JsonObject();
        JsonObject book = new JsonObject();
        JsonObject conditions = new JsonObject();

        book.addProperty("trigger", SpellEngineTrigger.SPELL_BOOK_CREATION.getId());
        conditions.addProperty("spell_pool", spellPool);
        book.add("conditions", conditions);
        criteria.add("book", book);

        return criteria;
    }

    public static JsonObject criteriaOneSpellBound(String spellPool) {
        JsonObject criteria = new JsonObject();
        JsonObject bind = new JsonObject();
        JsonObject conditions = new JsonObject();

        bind.addProperty("trigger", SpellEngineTrigger.SPELL_BINDING.getId());
        conditions.addProperty("complete", false);
        conditions.addProperty("spell_pool", spellPool);
        bind.add("conditions", conditions);
        criteria.add("bind", bind);

        return criteria;
    }

    public static JsonObject criteriaAllSpellsBound(String spellPool) {
        JsonObject criteria = new JsonObject();
        JsonObject bind = new JsonObject();
        JsonObject conditions = new JsonObject();

        bind.addProperty("trigger", SpellEngineTrigger.SPELL_BINDING.getId());
        conditions.addProperty("complete", true);
        conditions.addProperty("spell_pool", spellPool);
        bind.add("conditions", conditions);
        criteria.add("bind", bind);

        return criteria;
    }

    public static JsonObject criteriaSpellCast(String spellOrTag) {
        JsonObject criteria = new JsonObject();
        JsonObject cast = new JsonObject();
        JsonObject conditions = new JsonObject();

        cast.addProperty("trigger", SpellEngineTrigger.SPELL_CAST.getId());
        conditions.addProperty("spell", spellOrTag);
        cast.add("conditions", conditions);
        criteria.add("cast", cast);

        return criteria;
    }

    public static void writeAdvancement(Path outputPath, String modId, String name, JsonObject json) throws IOException {
        Path targetPath = outputPath.resolve("data").resolve(modId).resolve("advancement").resolve(name + ".json");
        Files.createDirectories(targetPath.getParent());
        Files.writeString(targetPath, GSON.toJson(json));
    }
}