package net.more_rpg_classes.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;


public class MRPGCItems {
    public static final Item WOLF_FUR = registerItem("wolf_fur", new Item(new Item.Settings()));
    public static final Item POLAR_BEAR_FUR = registerItem("polar_bear_fur", new Item(new Item.Settings()));
    public static final Item HARDENED_LEATHER= registerItem("hardened_leather", new Item(new Item.Settings()));
    public static final Item AQUA_STONE= registerItem("aqua_stone", new Item(new Item.Settings()));
    public static final Item TERRA_STONE= registerItem("terra_stone", new Item(new Item.Settings()));
    public static final Item STORM_STONE= registerItem("storm_stone", new Item(new Item.Settings()));
        public static final Item NATURE_STONE= registerItem("nature_stone", new Item(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    public static void registerModItems(){

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.RABBIT_HIDE,MRPGCItems.WOLF_FUR);
            entries.addAfter(Items.RABBIT_HIDE,MRPGCItems.POLAR_BEAR_FUR);
            entries.addAfter(Items.RABBIT_HIDE,MRPGCItems.HARDENED_LEATHER);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
           entries.add(MRPGCItems.AQUA_STONE);
            entries.add(MRPGCItems.TERRA_STONE);
            entries.add(MRPGCItems.STORM_STONE);
            entries.add(MRPGCItems.NATURE_STONE);
        });

        MRPGCMod.LOGGER.info("Registering Mod Items for " + MOD_ID);
    }
}
