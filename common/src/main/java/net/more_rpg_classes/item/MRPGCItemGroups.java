package net.more_rpg_classes.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class MRPGCItemGroups {
    public static final Identifier ARSENAL_ID = Identifier.of(MOD_ID, "arsenal");
    public static final RegistryKey<ItemGroup> ARSENAL_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), ARSENAL_ID);
    public static final Identifier ARMORY_ID = Identifier.of(MOD_ID, "armory");
    public static final RegistryKey<ItemGroup> ARMORY_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), ARMORY_ID);

    public static final boolean devEnvo = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static final boolean arsenalLoaded = FabricLoader.getInstance().isModLoaded("arsenal");
    public static final boolean armoryLoaded = FabricLoader.getInstance().isModLoaded("armory_rpgs");

    public static final boolean archersExpansionLoaded = FabricLoader.getInstance().isModLoaded("archers_expansion");
    public static final boolean elementalWizardsLoaded = FabricLoader.getInstance().isModLoaded("elemental_wizards_rpg");
    public static final boolean berserkerLoaded = FabricLoader.getInstance().isModLoaded("berserker_rpg");
    public static final boolean forcemasterLoaded = FabricLoader.getInstance().isModLoaded("forcemaster_rpg");
    public static final boolean bardsLoaded = FabricLoader.getInstance().isModLoaded("bards_rpg");
    public static final boolean anyContentModLoaded = archersExpansionLoaded || elementalWizardsLoaded
            || berserkerLoaded || forcemasterLoaded || bardsLoaded;

    private static final List<String> ARSENAL_ICON_IDS = List.of(
            "bards_rpg:unique_rapier_0",
            "berserker_rpg:unique_berserker_axe_1",
            "elemental_wizards_rpg:unique_staff_1",
            "forcemaster_rpg:unique_knuckle_0"
    );
    private static final List<String> ARMORY_ICON_IDS = List.of(
            "archers_expansion:sentinel_archer_head",
            "bards_rpg:storyteller_garb_head",
            "berserker_rpg:warlord_head",
            "elemental_wizards_rpg:ocean_robe_head",
            "forcemaster_rpg:billporon_head"
    );

    private static Item resolveIcon(List<String> ids, Supplier<Item> fallback) {
        return ids.stream()
                .sorted()
                .map(id -> Registries.ITEM.getOrEmpty(Identifier.of(id)).orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(fallback);
    }

    public static void register() {
        if (devEnvo || (arsenalLoaded && anyContentModLoaded)) {
            var group = FabricItemGroup.builder()
                    .icon(() -> new ItemStack(resolveIcon(ARSENAL_ICON_IDS, () -> Items.NETHERITE_SWORD)))
                    .displayName(Text.translatable("itemGroup." + MOD_ID + ".arsenal"))
                    .build();
            Registry.register(Registries.ITEM_GROUP, ARSENAL_KEY, group);
        }
        if (devEnvo || (armoryLoaded && anyContentModLoaded)) {
            var group = FabricItemGroup.builder()
                    .icon(() -> new ItemStack(resolveIcon(ARMORY_ICON_IDS, () -> SmithingIngredients.ASCETIC != null
                            ? SmithingIngredients.ASCETIC.item().get()
                            : Items.NETHERITE_CHESTPLATE)))
                    .displayName(Text.translatable("itemGroup." + MOD_ID + ".armory"))
                    .build();
            Registry.register(Registries.ITEM_GROUP, ARMORY_KEY, group);
        }
    }
}
