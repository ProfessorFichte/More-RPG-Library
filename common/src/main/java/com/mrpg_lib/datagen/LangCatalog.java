package com.mrpg_lib.datagen;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.armory_rpgs.SmithingIngredients;
import com.mrpg_lib.effect.MRPGCEffects;
import com.mrpg_lib.item.MRPGCItemGroups;
import com.mrpg_lib.item.MRPGCItems;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public final class LangCatalog {
    private LangCatalog() {
    }

    public interface Sink {
        void add(String key, String value);
    }

    public interface Contributor {
        void contribute(Sink sink);
    }

    private static final List<Contributor> CONTRIBUTORS = new ArrayList<>();

    private static final Map<String, String> HUD = orderedMap(
            "frozen", "Frozen",
            "ignited", "Ignited",
            "feared", "Feared",
            "stagger", "Staggered"
    );

    private static final Map<String, String> DEATH_MESSAGES = orderedMap(
            "mrpgc.bleeding", "%1$s has bled out.",
            "mrpgc.fatal_poison", "%1$s was fatally poisoned.",
            "mrpgc.fatal_poison.teemo", "%1$s stepped on a suspicious green and purple mushroom.",
            "mrpgc.molten", "%1$s is burned by his molten armor."
    );

    private static final Map<String, String> ATTRIBUTES = orderedMap(
            "rage_modifier", "Rage Power",
            "lifesteal_modifier", "Lifesteal",
            "damage_reflect_modifier", "Damage Reflect",
            "spell_vampire", "Spell Vampire",
            "air_fuse_modifier", "Air Power Fuse",
            "arcane_fuse_modifier", "Arcane Power Fuse",
            "earth_fuse_modifier", "Earth Power Fuse",
            "fire_fuse_modifier", "Fire Power Fuse",
            "frost_fuse_modifier", "Frost Power Fuse",
            "healing_fuse_modifier", "Healing Power Fuse",
            "water_fuse_modifier", "Water Power Fuse",
            "burning_chance", "Burning Chance",
            "stagger_chance", "Stagger Chance",
            "armor_piercing", "Armor Piercing",
            "stun_chance", "Stun Chance",
            "poison_chance", "Poison Chance",
            "freeze_chance", "Freeze Chance",
            "bleeding_chance", "Bleeding Chance",
            "tenacity", "Tenacity"
    );

    private static final Map<String, String> ENTITIES = orderedMap(
            "friendly_lightning", "Friendly Lightning",
            "custom_cloud", "Custom Cloud"
    );

    private static final Map<String, String[]> ENCHANTMENTS = Map.of(
            "typhoon", new String[]{"Typhoon", "Increases air and water spell damage you deal."},
            "stonebloom", new String[]{"Stonebloom", "Increases earth and nature spell damage you deal."}
    );

    public static void contribute(Contributor contributor) {
        CONTRIBUTORS.add(contributor);
    }

    public static Map<String, String> build() {
        var lang = new TreeMap<String, String>();
        Sink sink = (key, value) -> {
            if (lang.putIfAbsent(key, value) != null) {
                throw new IllegalStateException("Duplicate lang key " + key);
            }
        };

        sink.add("itemGroup." + MOD_ID + ".general", "MRPGClasses");
        sink.add(itemGroupKey(MRPGCItemGroups.ARSENAL_ID), "More Arsenal");
        sink.add(itemGroupKey(MRPGCItemGroups.ARMORY_ID), "More Armory");
        HUD.forEach((name, text) -> sink.add("hud." + MOD_ID + "." + name, text));
        DEATH_MESSAGES.forEach((id, text) -> sink.add("death.attack." + id, text));
        ATTRIBUTES.forEach((name, text) -> sink.add(attributeKey(name), text));
        ENTITIES.forEach((name, text) -> sink.add("entity." + MOD_ID + "." + name, text));

        sink.add(MrpgAdvancements.ROOT_TITLE_KEY, "More RPG Content!");
        sink.add(MrpgAdvancements.ROOT_DESCRIPTION_KEY, "Explore the different paths you can choose!");

        MrpgRegistryData.ENCHANTMENTS.forEach(key -> {
            var texts = ENCHANTMENTS.get(key.getValue().getPath());
            var base = enchantmentKey(key.getValue());
            sink.add(base, texts[0]);
            sink.add(base + ".desc", texts[1]);
        });

        if (MrpgCompat.RUNES) {
            addItem(sink, MRPGCItems.AQUA_STONE, "Water Rune");
            addItem(sink, MRPGCItems.TERRA_STONE, "Earth Rune");
            addItem(sink, MRPGCItems.STORM_STONE, "Air Rune");
            addItem(sink, MRPGCItems.NATURE_STONE, "Nature Rune");
        }
        addItem(sink, MRPGCItems.POLAR_BEAR_FUR, "Bear Fur");
        addItem(sink, MRPGCItems.WOLF_FUR, "Wolf Fur");
        addItem(sink, MRPGCItems.HARDENED_LEATHER, "Hardened Leather");
        SmithingIngredients.ENTRIES.forEach(entry -> {
            sink.add(itemKey(entry.id()), entry.translations().itemName());
            sink.add(entry.appliesToTranslationKey(), entry.appliesToClassesTranslation());
        });

        MRPGCEffects.entries.forEach(entry -> {
            var base = "effect." + entry.id.getNamespace() + "." + entry.id.getPath();
            sink.add(base, entry.title);
            sink.add(base + ".description", entry.description);
        });

        sink.add("message." + MOD_ID + ".spellthief.spell_stolen", "Stole and cast %s from %s");
        sink.add("message." + MOD_ID + ".spellthief.effect_stolen", "Stole %s from %s");

        CONTRIBUTORS.forEach(contributor -> contributor.contribute(sink));
        assertCoverage(lang);
        return lang;
    }

    public static void assertCoverage(Map<String, String> lang) {
        var missing = new ArrayList<String>();
        Registries.ITEM.getIds().stream().filter(LangCatalog::isOwn).forEach(id -> require(lang, missing, itemKey(id)));
        Registries.STATUS_EFFECT.getIds().stream().filter(LangCatalog::isOwn).forEach(id -> {
            var base = "effect." + id.getNamespace() + "." + id.getPath();
            require(lang, missing, base);
            require(lang, missing, base + ".description");
        });
        Registries.ATTRIBUTE.getIds().stream().filter(LangCatalog::isOwn)
                .forEach(id -> require(lang, missing, attributeKey(id.getPath())));
        Registries.ENTITY_TYPE.getIds().stream().filter(LangCatalog::isOwn)
                .forEach(id -> require(lang, missing, "entity." + id.getNamespace() + "." + id.getPath()));
        MrpgRegistryData.ENCHANTMENTS.forEach(key -> require(lang, missing, enchantmentKey(key.getValue())));
        MrpgRegistryData.OWN_DAMAGE_MESSAGE_IDS.values().forEach(messageId -> require(lang, missing, "death.attack." + messageId));
        require(lang, missing, itemGroupKey(MRPGCItemGroups.ARSENAL_ID));
        require(lang, missing, itemGroupKey(MRPGCItemGroups.ARMORY_ID));
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing English lang entries: " + missing);
        }
    }

    private static void require(Map<String, String> lang, List<String> missing, String key) {
        if (!lang.containsKey(key)) {
            missing.add(key);
        }
    }

    private static boolean isOwn(Identifier id) {
        return id.getNamespace().equals(MOD_ID);
    }

    private static void addItem(Sink sink, Item item, String name) {
        sink.add(itemKey(Registries.ITEM.getId(item)), name);
    }

    private static String itemKey(Identifier id) {
        return Util.createTranslationKey("item", id);
    }

    private static String itemGroupKey(Identifier id) {
        return "itemGroup." + id.getNamespace() + "." + id.getPath();
    }

    private static String attributeKey(String name) {
        return "attribute.name." + MOD_ID + "." + name;
    }

    private static String enchantmentKey(Identifier id) {
        return Util.createTranslationKey("enchantment", id);
    }

    private static Map<String, String> orderedMap(String... pairs) {
        var map = new LinkedHashMap<String, String>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
