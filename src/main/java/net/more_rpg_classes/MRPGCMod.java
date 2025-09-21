package net.more_rpg_classes;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.config.EffectsConfig;
import net.more_rpg_classes.config.TweaksConfig;
import net.more_rpg_classes.custom.CustomSpellEntityPredicate;
import net.more_rpg_classes.custom.CustomSpellImpacts;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.item.MRPGCItems;
import net.more_rpg_classes.sounds.ModSounds;
import net.more_rpg_classes.util.loot.MRPGCLootTableEntityModifiers;
import net.more_rpg_classes.util.loot.SpecificSpellScrollPoolLootFunction;
import net.tiny_config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MRPGCMod implements ModInitializer {
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


		@Override
	public void onInitialize() {
			effectsConfig.refresh();
			tweaksConfig.refresh();
			Registry.register(Registries.LOOT_FUNCTION_TYPE,
					SpecificSpellScrollPoolLootFunction.ID,
					SpecificSpellScrollPoolLootFunction.TYPE);
			CustomSpellImpacts.registerCustomImpacts();
			MRPGCItems.registerModItems();
			MRPGCLootTableEntityModifiers.modifyLootEntityTables();
			MRPGCEffects.register();
			MoreParticles.register();
			ModSounds.register();
			MoreSpellSchools.initialize();
			CustomSpellImpacts.registerCustomImpacts();
			CustomSpellEntityPredicate.registerCustomPredicates();
		}
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

}


