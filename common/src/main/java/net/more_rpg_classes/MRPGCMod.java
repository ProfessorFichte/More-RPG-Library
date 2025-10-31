package net.more_rpg_classes;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.config.EffectsConfig;
import net.more_rpg_classes.config.TweaksConfig;
import net.more_rpg_classes.config.WeaknessConfig;
import net.more_rpg_classes.custom.CustomSpellEntityPredicate;
import net.more_rpg_classes.custom.CustomSpellImpacts;
import net.more_rpg_classes.custom.MoreSpellSchoolWeakness;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.item.MRPGCItems;
import net.more_rpg_classes.sounds.ModSounds;
import net.more_rpg_classes.util.loot.MRPGCLootTableEntityModifiers;
import net.more_rpg_classes.util.loot.SpecificSpellScrollPoolLootFunction;
import net.tiny_config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MRPGCMod {
	public static final String MOD_ID = "more_rpg_classes";
	public static final Logger LOGGER = LoggerFactory.getLogger("more_rpg_classes");

	public static ConfigManager<EffectsConfig> effectsConfig = new ConfigManager<EffectsConfig>
			("effects_v2", new EffectsConfig())
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


	public static void init() {
			effectsConfig.refresh();
			tweaksConfig.refresh();
			weaknessConfig.refresh();
			CustomSpellImpacts.registerCustomImpacts();
			MRPGCLootTableEntityModifiers.modifyLootEntityTables();
			MoreSpellSchools.initialize();
			CustomSpellImpacts.registerCustomImpacts();
			CustomSpellEntityPredicate.registerCustomPredicates();
	}
	public static void registerLootFunction() {
		Registry.register(Registries.LOOT_FUNCTION_TYPE,
				SpecificSpellScrollPoolLootFunction.ID,
				SpecificSpellScrollPoolLootFunction.TYPE);
	}
	public static void registerSounds() {
		ModSounds.register();
	}
	public static void registerItems() {
		MRPGCItems.registerModItems();
	}
	public static void registerEffects() {
		MRPGCEffects.register();
		effectsConfig.save();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

}


