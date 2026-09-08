package net.more_rpg_classes;

import net.more_rpg_classes.enchantment.MRPGCEnchantments;
import net.more_rpg_classes.item.MRPGCItemGroups;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;
import net.more_rpg_classes.util.loot.MRPGCLootTableEntityModifiers;
import net.spell_engine.PlatformEvents;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.compat.CriticalStrikeCompat;
import net.more_rpg_classes.entity.MrpgEntityRelationMatcher;
import net.more_rpg_classes.config.LootConfig;
import net.more_rpg_classes.config.TweaksConfig;
import net.more_rpg_classes.config.WeaknessConfig;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.more_rpg_classes.custom.CustomSpellEntityPredicate;
import net.more_rpg_classes.custom.CustomSpellImpacts;
import net.more_rpg_classes.custom.MoreSpellSchoolWeakness;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.MRPGCEntities;
import net.more_rpg_classes.item.MRPGCItems;
import net.more_rpg_classes.sounds.MRPGLibSounds;
import net.more_rpg_classes.util.loot.*;
import net.more_rpg_classes.worldgen.ModStructureProcessorTypes;
import net.more_rpg_classes.worldgen.ModStructureTypes;
import net.spell_engine.Platform;
import net.tiny_config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MRPGCMod {
	public static final String MOD_ID = "more_rpg_classes";
	public static final Logger LOGGER = LoggerFactory.getLogger("more_rpg_classes");

	public static ConfigManager<ConfigFile.Effects> effectsConfig = new ConfigManager<>
			("effects_v4", new ConfigFile.Effects())
			.builder()
			.setDirectory(MOD_ID)
			.sanitize(true)
			.build();
	public static ConfigManager<TweaksConfig> tweaksConfig = new ConfigManager<>
			("tweaks_v2", new TweaksConfig())
			.builder()
			.setDirectory(MOD_ID)
			.sanitize(true)
			.build();
	public static ConfigManager<WeaknessConfig> weaknessConfig = new ConfigManager<>
			("elemental_weaknesses", MoreSpellSchoolWeakness.createDefault())
			.builder()
			.setDirectory(MOD_ID)
			.sanitize(true)
			.validate(WeaknessConfig::isValid)
			.build();
	public static final ConfigManager<LootConfig> lootConfig = new ConfigManager<>
			("loot", LootConfig.example())
			.builder()
			.setDirectory(MOD_ID)
			.sanitize(true)
			.build();


	public static void init() {
			effectsConfig.refresh();
			tweaksConfig.refresh();
			weaknessConfig.refresh();
			lootConfig.refresh();
			CustomSpellImpacts.registerCustomImpacts();
			MrpgEntityRelationMatcher.register();
			MoreSpellSchools.initialize();
			CustomSpellEntityPredicate.registerCustomPredicates();
			CriticalStrikeCompat.init();
	}
	public static void modifyLootTable(Identifier id, LootPoolAdder adder) {
		var tableId = id.toString();
		if (!lootConfig.value.entries.containsKey(tableId)) {
			return;
		}
		LootInjector.configure(id, adder);
	}

	/// Loot-table hooks, routed through Spell Engine's loader-agnostic event (1.20.1 has no Forgified
	/// Fabric API). Creative-tab population stays per-platform: positional inserts are expressed with
	/// loader-specific APIs that the vanilla `ItemGroup.Entries` does not carry.
	public static void registerEvents() {
		PlatformEvents.onLootTableModify(context -> {
			LootPoolAdder adder = pool -> context.addPool(pool.build());
			modifyLootTable(context.tableId(), adder);
			MRPGCLootTableEntityModifiers.modifyLootEntityTables(context.tableId(), adder);
		});
	}

	public static void registerLootFunction() {
		Registry.register(Registries.LOOT_FUNCTION_TYPE, SpecificSpellScrollPoolLootFunction.ID, SpecificSpellScrollPoolLootFunction.TYPE);
		Registry.register(Registries.LOOT_FUNCTION_TYPE, ConditionalItemLootFunction.ID, ConditionalItemLootFunction.TYPE);
		Registry.register(Registries.LOOT_FUNCTION_TYPE, BindSpellFromPoolsLootFunction.ID, BindSpellFromPoolsLootFunction.TYPE);
		Registry.register(Registries.LOOT_FUNCTION_TYPE, ItemTagPickerLootFunction.ID, ItemTagPickerLootFunction.TYPE);
		Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, ConditionalItemEntry.ID, new LootPoolEntryType(new ConditionalItemEntry.Serializer()));
	}
	public static void registerEnchantments() {
		MRPGCEnchantments.register();
	}

	public static void registerSounds() {
		MRPGLibSounds.register();
	}
	public static void registerItems() {
		MRPGCItems.registerModItems();
		MRPGCItemGroups.register();
		if(Platform.util().isDevelopmentEnvironment() ||Platform.util().isModLoaded("armory_rpgs")){
			SmithingIngredients.register();
		}
	}
	public static void registerEffects() {
		MRPGCEffects.register(effectsConfig.value);
		effectsConfig.save();
	}

	public static void registerEntities() {
		MRPGCEntities.register();
	}

	public static void registerStructures() {
		ModStructureTypes.register();
		ModStructureProcessorTypes.register();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

}


