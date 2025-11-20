package net.more_rpg_classes.compat.armory_rpgs;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class SmithingIngredients {
    public static class UpgradeCrystal extends Item {
        public static final Text APPLIES_TO_TEXT = Text.translatable(
                        Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.applies_to")))
                .formatted(Formatting.GRAY);
        public static final String HINT_TRANSLATION_KEY = Util.createTranslationKey("item", Identifier.of("armory_rpgs", "smithing_template.hint"));
        public static final Text HINT_TEXT = Text.translatable(HINT_TRANSLATION_KEY)
                .formatted(Formatting.GRAY);

        private final String appliesToTranslationKey;
        public UpgradeCrystal(Item.Settings settings, String appliesToTranslationKey) {
            super(settings);
            this.appliesToTranslationKey = appliesToTranslationKey;
        }

        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            super.appendTooltip(stack, context, tooltip, type);
            tooltip.add(HINT_TEXT);
            tooltip.add(ScreenTexts.EMPTY);
            tooltip.add(APPLIES_TO_TEXT);
            tooltip.add(ScreenTexts.space().append(Text.translatable(appliesToTranslationKey)).formatted(Formatting.BLUE));
        }
    }

    public record Translations(String itemName) { }
    public record Entry(String name, List<FightClass> classes, Translations translations, Supplier<UpgradeCrystal> item) {
        public static Entry of(String name, List<FightClass> classes, Translations translations) {
            var factory = Suppliers.memoize(() ->
                    new UpgradeCrystal(new Item.Settings()
                            .rarity(Rarity.EPIC)
                            .fireproof(),
                            appliesToTranslationKey(name)
                    ));
            return new Entry(name, classes, translations, factory);
        }
        public Identifier id() {
            return Identifier.of(MOD_ID, name + "_upgrade_crystal");
        }
        public static String appliesToTranslationKey(String name) {
            return Util.createTranslationKey("item", Identifier.of(MOD_ID, "upgrade_crystal." + name + ".applies_to"));
        }
        public String appliesToTranslationKey() {
            return appliesToTranslationKey(name);
        }
        public String appliesToClassesTranslation() {
            var classNames = classes.stream().map(c -> c.translation).toList();
            var list = "";
            if (classNames.size() == 1) {
                list = classNames.get(0);
            } else if (classNames.size() == 2) {
                list = classNames.get(0) + " and " + classNames.get(1);
            } else {
                var allButLast = classNames.subList(0, classNames.size() - 1);
                var last = classNames.get(classNames.size() - 1);
                list = String.join(", ", allButLast) + ", and " + last;
            }
            return list + " armor";
        }
    }
    public static final ArrayList<Entry> ENTRIES = new ArrayList<>();
    public static Entry add(Entry entry) {
        ENTRIES.add(entry);
        return entry;
    }
    public static Entry WARDEN;
    public static Entry ASCETIC;
    public static Entry RAVAGER;
    public static Entry GENERAL;
    public static Entry VIRTUOSO;

    public static final boolean devEnvo = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static final boolean archersExpansionLoaded = FabricLoader.getInstance().isModLoaded("archers_expansion");
    public static final boolean elementalWizardsLoaded = FabricLoader.getInstance().isModLoaded("elemental_wizards_rpg");
    public static final boolean berserkerLoaded = FabricLoader.getInstance().isModLoaded("berserker_rpg");
    public static final boolean forcemasterLoaded = FabricLoader.getInstance().isModLoaded("forcemaster_rpg");
    public static void register() {
        if (devEnvo || forcemasterLoaded ||elementalWizardsLoaded) {
            ASCETIC = add(Entry.of("ascetic", List.of(FightClass.AIR_WIZARD, FightClass.FORCEMASTER),
                    new Translations("Ascetic's Lost Crystal")));
        }
        if (devEnvo ||  elementalWizardsLoaded) {
            WARDEN = add(Entry.of("warden", List.of(FightClass.EARTH_WIZARD, FightClass.WATER_WIZARD),
                    new Translations("Warden's Lost Crystal")));
        }
        if (devEnvo || berserkerLoaded /*|| archersExpansionLoaded */) {
            RAVAGER = add(Entry.of("ravager", List.of(FightClass.BERSERKER, FightClass.TUNDRA_HUNTER),
                    new Translations("Ravager's Lost Crystal")));
        }
                /*
        if (devEnvo || archersExpansionLoaded) {
            GENERAL = add(Entry.of("general", List.of( FightClass.DEADEYE, FightClass.WAR_ARCHER),
                    new Translations("General's Lost Crystal")));
        }
         */




        for (var entry : ENTRIES) {
            Registry.register(Registries.ITEM, entry.id(), entry.item().get());
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((content) -> {
            for (var entry : ENTRIES) {
                content.add(entry.item().get());
            }
        });

    }
}