package com.mrpg_lib;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.armory_rpgs.SmithingIngredients;
import com.mrpg_lib.compat.ranged_weapon_api.RangedWeaponCompat;
import com.mrpg_lib.compat.spell_engine.SpellEngineCompat;
import com.mrpg_lib.compat.spell_power.SpellPowerCompat;
import com.mrpg_lib.config.ConfigFile;
import com.mrpg_lib.config.LootConfig;
import com.mrpg_lib.config.TweaksConfig;
import com.mrpg_lib.effect.MRPGCEffects;
import com.mrpg_lib.entity.MRPGCEntities;
import com.mrpg_lib.item.MRPGCItemGroups;
import com.mrpg_lib.item.MRPGCItems;
import com.mrpg_lib.platform.MrpgPlatform;
import com.mrpg_lib.sounds.MRPGLibSounds;
import com.mrpg_lib.util.loot.*;
import com.mrpg_lib.worldgen.ModStructureProcessorTypes;
import com.mrpg_lib.worldgen.ModStructureTypes;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.tiny_config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MRPGCMod {
	public static final String MOD_ID = "mrpg_lib";
	public static final Logger LOGGER = LoggerFactory.getLogger("mrpg_lib");

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
	public static final ConfigManager<LootConfig> lootConfig = new ConfigManager<>
			("loot", LootConfig.example())
			.builder()
			.setDirectory(MOD_ID)
			.sanitize(true)
			.build();


	public static void init() {
		effectsConfig.refresh();
		tweaksConfig.refresh();
		lootConfig.refresh();
		if (MrpgCompat.RANGED_WEAPON_API) {
			RangedWeaponCompat.init();
		}
		if (MrpgCompat.SPELL_ENGINE) {
			SpellEngineCompat.init();
		} else if (MrpgCompat.SPELL_POWER) {
			SpellPowerCompat.init();
		}
	}
	public static void modifyLootTable(RegistryWrapper.WrapperLookup registries, RegistryKey<LootTable> key, LootPoolAdder adder) {
		var tableId = key.getValue().toString();
		if (!lootConfig.value.entries.containsKey(tableId)) {
			return;
		}
		LootInjector.configure(registries, key.getValue(), adder);
	}
	public static void registerLootFunction() {
		if (MrpgCompat.SPELL_ENGINE) {
			SpellEngineCompat.registerLootFunctions();
		}
		Registry.register(Registries.LOOT_FUNCTION_TYPE, ConditionalItemLootFunction.ID, ConditionalItemLootFunction.TYPE);
		Registry.register(Registries.LOOT_FUNCTION_TYPE, ItemTagPickerLootFunction.ID, ItemTagPickerLootFunction.TYPE);
		Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, ConditionalItemEntry.ID, new LootPoolEntryType(ConditionalItemEntry.CODEC));
	}
	public static void registerSounds() {
		MRPGLibSounds.register();
	}
	public static void registerItems() {
		MRPGCItems.registerModItems();
		MRPGCItemGroups.register();
		if(MrpgPlatform.isDevelopmentEnvironment() ||MrpgPlatform.isModLoaded("armory_rpgs")){
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
		return Identifier.of(MOD_ID, path);
	}

}


