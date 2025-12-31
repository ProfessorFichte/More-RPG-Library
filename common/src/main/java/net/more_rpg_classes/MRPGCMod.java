package net.more_rpg_classes;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.compat.CriticalStrikeCompat;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;
import net.more_rpg_classes.config.LootConfig;
import net.more_rpg_classes.config.TweaksConfig;
import net.more_rpg_classes.config.WeaknessConfig;
import net.spell_engine.api.config.ConfigFile;
import net.more_rpg_classes.custom.CustomSpellEntityPredicate;
import net.more_rpg_classes.custom.CustomSpellImpacts;
import net.more_rpg_classes.custom.MoreSpellSchoolWeakness;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.MRPGCEntities;
import net.more_rpg_classes.item.MRPGCItems;
import net.more_rpg_classes.sounds.ModSounds;
import net.more_rpg_classes.util.loot.*;
import net.tiny_config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MRPGCMod {
	public static final String MOD_ID = "more_rpg_classes";
	public static final Logger LOGGER = LoggerFactory.getLogger("more_rpg_classes");

	public static ConfigManager<ConfigFile.Effects> effectsConfig = new ConfigManager<>
			("effects_v3", new ConfigFile.Effects())
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
			LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			var tableId = key.getValue().toString();
			if (!lootConfig.value.entries.containsKey(tableId)) {
				return;
			}
			LootInjector.configure(registries, key.getValue(), tableBuilder);
			});
			MoreSpellSchools.initialize();
			CustomSpellEntityPredicate.registerCustomPredicates();
			CriticalStrikeCompat.init();
	}
	public static void registerLootFunction() {
		Registry.register(Registries.LOOT_FUNCTION_TYPE, SpecificSpellScrollPoolLootFunction.ID, SpecificSpellScrollPoolLootFunction.TYPE);
		Registry.register(Registries.LOOT_FUNCTION_TYPE, ConditionalItemLootFunction.ID, ConditionalItemLootFunction.TYPE);
		Registry.register(Registries.LOOT_FUNCTION_TYPE, BindSpellFromPoolsLootFunction.ID, BindSpellFromPoolsLootFunction.TYPE);
		Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, ConditionalItemEntry.ID, new LootPoolEntryType(ConditionalItemEntry.CODEC));
	}
	public static void registerSounds() {
		ModSounds.register();
	}
	public static void registerItems() {
		MRPGCItems.registerModItems();
		if(FabricLoader.getInstance().isDevelopmentEnvironment() ||FabricLoader.getInstance().isModLoaded("armory_rpgs")){
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

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

}


