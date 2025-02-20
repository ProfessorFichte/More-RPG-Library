package net.more_rpg_classes;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.config.EffectsConfig;
import net.more_rpg_classes.config.EnchantingConfig;
import net.more_rpg_classes.config.TweaksConfig;
import net.more_rpg_classes.custom.Enchantment_CustomSpellSchool;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.item.MRPGCGroup;
import net.more_rpg_classes.item.MRPGCItems;
import net.more_rpg_classes.sounds.ModSounds;
import net.more_rpg_classes.util.loot.MRPGCLootTableEntityModifiers;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import net.spell_power.api.enchantment.SpellPowerEnchanting;
import net.tinyconfig.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MRPGCMod implements ModInitializer {
	public static final String MOD_ID = "more_rpg_classes";
	public static final Logger LOGGER = LoggerFactory.getLogger("more_rpg_classes");

	public static ConfigManager<EffectsConfig> effectsConfig = new ConfigManager<EffectsConfig>
			("effects", new EffectsConfig())
			.builder()
			.setDirectory(MOD_ID)
			.sanitize(true)
			.build();
	public static final ConfigManager<EnchantingConfig> enchantmentConfig = new ConfigManager<EnchantingConfig>
			("enchantments", new EnchantingConfig())
			.builder()
			.setDirectory(MOD_ID)
			.sanitize(true)
			.schemaVersion(4)
			.build();
	public static ConfigManager<TweaksConfig> tweaksConfig = new ConfigManager<>
			("tweaks_v1", new TweaksConfig())
			.builder()
			.setDirectory(MOD_ID)
			.sanitize(true)
			.build();


		@Override
	public void onInitialize() {
		effectsConfig.refresh();
		enchantmentConfig.refresh();
		tweaksConfig.refresh();
		MRPGCItems.registerModItems();
		MRPGCGroup.registerItemGroups();
		MRPGCLootTableEntityModifiers.modifyLootEntityTables();
		MRPGCEffects.register();
		MoreParticles.register();
		ModSounds.register();
		MoreSpellSchools.initialize();

			for(var entry: Enchantment_CustomSpellSchool.all.entrySet()) {
				Registry.register(Registries.ENCHANTMENT, entry.getKey(), entry.getValue());
			}
			attachEnchantmentsToSchools();
	}
	private void attachEnchantmentsToSchools() {
		for(var school: SpellSchools.all()) {
			var poweringEnchantments = Enchantment_CustomSpellSchool.all.entrySet().stream()
					.filter(entry -> entry.getValue().poweredSchools().contains(school))
					.map(Map.Entry::getValue)
					.toList();
			school.addSource(SpellSchool.Trait.POWER, new SpellSchool.Source(SpellSchool.Apply.MULTIPLY, query -> {
				double value = 0;
				for (var enchantment: poweringEnchantments) {
					var level = SpellPowerEnchanting.getEnchantmentLevel(enchantment, query.entity(), null);
					value = enchantment.amplified(value, level);
				}
				return value;
			}));
		}
	}

}


