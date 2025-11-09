package net.more_rpg_classes.config;

import java.util.LinkedHashMap;
import java.util.List;

public class LootConfig {
    public record Item(String itemId, int weight, int minAmount,int maxAmount) {  }
    public record Entry(float rolls, List<Item> items) { }
    public LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();

    public static LootConfig example() {
        LootConfig loot = new LootConfig();
        loot.entries.put(
                "minecraft:entities/polar_bear", new Entry(1.0f, List.of(new Item("more_rpg_classes:polar_bear_fur", 1,1,3))));
        loot.entries.put(
                "minecraft:entities/wolf", new Entry(1.0f, List.of(new Item("more_rpg_classes:wolf_fur", 1,1,3))));
        loot.entries.put(
                "minecraft:entities/ravager", new Entry(1.0f, List.of(new Item("more_rpg_classes:hardened_leather", 1,1,2))));
        loot.entries.put(
                "minecraft:chests/igloo_chest", new Entry(1.0f, List.of(new Item("more_rpg_classes:wolf_fur", 2,1,3),
                        new Item("polar_bear_fur:wolf_fur", 1,1,3))));
        return loot;
    }
}
