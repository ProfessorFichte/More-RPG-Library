package com.mrpg_lib.item;

import com.mrpg_lib.compat.MrpgCompat;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import static com.mrpg_lib.MRPGCMod.MOD_ID;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


public class MRPGCItems {
    public static final Item WOLF_FUR = registerItem("wolf_fur", new Item(new Item.Settings()));
    public static final Item POLAR_BEAR_FUR = registerItem("polar_bear_fur", new Item(new Item.Settings()));
    public static final Item HARDENED_LEATHER= registerItem("hardened_leather", new Item(new Item.Settings()));
    public static final Item AQUA_STONE = MrpgCompat.RUNES ? registerItem("aqua_stone", new Item(new Item.Settings())) : null;
    public static final Item TERRA_STONE = MrpgCompat.RUNES ? registerItem("terra_stone", new Item(new Item.Settings())) : null;
    public static final Item STORM_STONE = MrpgCompat.RUNES ? registerItem("storm_stone", new Item(new Item.Settings())) : null;
    public static final Item NATURE_STONE = MrpgCompat.RUNES ? registerItem("nature_stone", new Item(new Item.Settings())) : null;

    public static final List<Item> INGREDIENTS_GROUP_ITEMS = List.of(WOLF_FUR, POLAR_BEAR_FUR, HARDENED_LEATHER);
    public static final List<Item> COMBAT_GROUP_ITEMS = Stream.of(AQUA_STONE, TERRA_STONE, STORM_STONE, NATURE_STONE)
            .filter(Objects::nonNull).toList();

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    public static void registerModItems(){
    }
}
