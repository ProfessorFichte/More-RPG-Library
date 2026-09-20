package net.more_rpg_classes.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import static net.more_rpg_classes.MRPGCMod.MOD_ID;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MRPGCItems {
    private static final Map<Identifier, Item> TO_REGISTER = new LinkedHashMap<>();

    public static final Item WOLF_FUR = registerItem("wolf_fur", new Item(new Item.Settings()));
    public static final Item POLAR_BEAR_FUR = registerItem("polar_bear_fur", new Item(new Item.Settings()));
    public static final Item HARDENED_LEATHER= registerItem("hardened_leather", new Item(new Item.Settings()));
    public static final Item AQUA_STONE= registerItem("aqua_stone", new Item(new Item.Settings()));
    public static final Item TERRA_STONE= registerItem("terra_stone", new Item(new Item.Settings()));
    public static final Item STORM_STONE= registerItem("storm_stone", new Item(new Item.Settings()));
        public static final Item NATURE_STONE= registerItem("nature_stone", new Item(new Item.Settings()));

    public static final List<Item> INGREDIENTS_GROUP_ITEMS = List.of(WOLF_FUR, POLAR_BEAR_FUR, HARDENED_LEATHER);
    public static final List<Item> COMBAT_GROUP_ITEMS = List.of(AQUA_STONE, TERRA_STONE, STORM_STONE, NATURE_STONE);

    private static Item registerItem(String name, Item item) {
        TO_REGISTER.put(new Identifier(MOD_ID, name), item);
        return item;
    }

    public static Map<Identifier, Item> itemsToRegister() {
        var toRegister = new LinkedHashMap<Identifier, Item>();
        TO_REGISTER.forEach((id, item) -> {
            if (Registries.ITEM.containsId(id)) { return; }
            toRegister.put(id, item);
        });
        return Collections.unmodifiableMap(toRegister);
    }

    public static void registerModItems(){
        itemsToRegister().forEach((id, item) -> Registry.register(Registries.ITEM, id, item));
    }
}
