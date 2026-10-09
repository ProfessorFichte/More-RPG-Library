package com.mrpg_lib.effect;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.config.AttributeModifier;
import com.mrpg_lib.config.EffectConfig;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public final class EffectEntry {
    public final Identifier id;
    public final String title;
    public final String description;
    public final StatusEffect effect;
    public final EffectConfig defaults;
    public EffectConfig config;
    public RegistryEntry<StatusEffect> entry;

    public EffectEntry(Identifier id, String title, String description, StatusEffect effect) {
        this(id, title, description, effect, EffectConfig.EMPTY);
    }

    public EffectEntry(Identifier id, String title, String description, StatusEffect effect, EffectConfig defaults) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.effect = effect;
        this.defaults = defaults;
        this.config = defaults;
    }

    public EffectConfig config() {
        return config;
    }

    public static void register(List<EffectEntry> entries, Map<String, EffectConfig> configs) {
        for (var entry : entries) {
            var key = entry.id.toString();
            var loaded = configs.get(key);
            if (loaded != null) {
                entry.config = loaded;
            }
            configs.put(key, entry.config);
            applyModifiers(entry);
        }
        for (var entry : entries) {
            entry.entry = Registry.registerReference(Registries.STATUS_EFFECT, entry.id, entry.effect);
        }
    }

    private static void applyModifiers(EffectEntry entry) {
        for (var modifier : entry.config.selectedAttributes()) {
            var attributeId = Identifier.tryParse(modifier.attribute);
            var attribute = attributeId == null ? java.util.Optional.<RegistryEntry.Reference<EntityAttribute>>empty()
                    : Registries.ATTRIBUTE.getEntry(attributeId);
            if (attribute.isEmpty()) {
                MRPGCMod.LOGGER.debug("Skipping unregistered attribute '{}' on effect {}", modifier.attribute, entry.id);
                continue;
            }
            entry.effect.addAttributeModifier(attribute.get(), modifierId(entry, modifier), modifier.value, modifier.operation);
        }
    }

    private static Identifier modifierId(EffectEntry entry, AttributeModifier modifier) {
        if (modifier.id != null && !modifier.id.isEmpty()) {
            return Identifier.of(modifier.id);
        }
        return entry.id;
    }
}
