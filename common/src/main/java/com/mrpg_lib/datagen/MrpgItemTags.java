package com.mrpg_lib.datagen;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.item.MRPGCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public class MrpgItemTags extends FabricTagProvider.ItemTagProvider {
    public static final TagKey<Item> RUNES = TagKey.of(RegistryKeys.ITEM, Identifier.of("runes", "runes"));
    public static final TagKey<Item> RUNE_BASE_STONE = TagKey.of(RegistryKeys.ITEM, Identifier.of("runes", "rune_crafting/base/stone"));
    private static final Identifier SPELL_POWER_SPECIALIZED = Identifier.of("spell_power", "enchantable/spell_power_specialized");

    public MrpgItemTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    public static TagKey<Item> tag(String path) {
        return TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, path));
    }

    public static TagKey<Item> reagent(String rune, String size) {
        return tag("rune_crafting/reagent/" + rune + "_" + size);
    }

    @Override
    public String getName() {
        return "Item Tags (" + MOD_ID + ")";
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(tag("coral_blocks")), Registries.ITEM,
                Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK,
                Items.FIRE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(tag("coral_plants")), Registries.ITEM,
                Items.TUBE_CORAL, Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL, Items.HORN_CORAL,
                Items.TUBE_CORAL_FAN, Items.BRAIN_CORAL_FAN, Items.BUBBLE_CORAL_FAN,
                Items.FIRE_CORAL_FAN, Items.HORN_CORAL_FAN);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(tag("water_plants")), Registries.ITEM,
                Items.KELP, Items.SEAGRASS, Items.MANGROVE_PROPAGULE, Items.SEA_PICKLE, Items.LILY_PAD);
        getOrCreateTagBuilder(tag("polar_bear_fur"))
                .addOptional(Registries.ITEM.getId(MRPGCItems.POLAR_BEAR_FUR))
                .addOptional(Identifier.of("frostiful", "polar_bear_fur_tuft"))
                .addOptional(Identifier.of("environmentz", "polar_bear_fur"))
                .addOptional(Identifier.of("untamedwilds", "hide_white"));
        getOrCreateTagBuilder(tag("wolf_fur"))
                .addOptional(Registries.ITEM.getId(MRPGCItems.WOLF_FUR))
                .addOptional(Identifier.of("frostiful", "wolf_fur_tuft"))
                .addOptional(Identifier.of("environmentz", "wolf_pelt"));

        getOrCreateTagBuilder(tag("enchantable/stonebloom")).addOptionalTag(SPELL_POWER_SPECIALIZED);
        getOrCreateTagBuilder(tag("enchantable/typhoon")).addOptionalTag(SPELL_POWER_SPECIALIZED);

        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(reagent("aqua", "medium")), Registries.ITEM,
                Items.PRISMARINE_CRYSTALS, Items.PRISMARINE_SHARD, Items.GLOW_INK_SAC, Items.NAUTILUS_SHELL);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(reagent("aqua", "small")), Registries.ITEM,
                Items.KELP, Items.SEAGRASS, Items.INK_SAC, Items.SEA_PICKLE);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(reagent("nature", "medium")), Registries.ITEM, Items.EMERALD);
        getOrCreateTagBuilder(reagent("nature", "small")).addOptionalTag(ItemTags.LOGS).addOptionalTag(ItemTags.FLOWERS);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(reagent("storm", "medium")), Registries.ITEM, Items.PHANTOM_MEMBRANE, Items.WIND_CHARGE);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(reagent("storm", "small")), Registries.ITEM, Items.FEATHER);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(reagent("terra", "medium")), Registries.ITEM, Items.OBSIDIAN, Items.CRYING_OBSIDIAN, Items.BLACKSTONE);
        MrpgTagBuilders.addOptional(getOrCreateTagBuilder(reagent("terra", "small")), Registries.ITEM,
                Items.CLAY_BALL, Items.SAND, Items.RED_SAND, Items.GRAVEL, Items.FLINT);

        if (MrpgCompat.RUNES) {
            MrpgTagBuilders.addOptional(getOrCreateTagBuilder(RUNES), Registries.ITEM,
                    MRPGCItems.AQUA_STONE, MRPGCItems.TERRA_STONE, MRPGCItems.STORM_STONE, MRPGCItems.NATURE_STONE);
        }
    }
}
