package com.mrpg_lib.entity.config;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityAttributeApplier {
    private static final Logger LOGGER = LoggerFactory.getLogger("mrpg_lib/EntityAttributeApplier");

    public static void apply(LivingEntity entity, EntityAttributeConfig config, EntityAttributeConfig.Entry fallback) {
        apply(entity, entity.getType().getRegistryEntry().getKey().get().getValue(), config, fallback);
    }

    public static void apply(LivingEntity entity, Identifier entityId, EntityAttributeConfig config, EntityAttributeConfig.Entry fallback) {
        EntityAttributeConfig.Entry entry = config != null ? config.entryFor(entityId) : null;
        boolean fromConfig = entry != null;
        if (entry == null) entry = fallback;
        LOGGER.info("[EntityAttributeApplier] {} -> source={} attributeCount={}",
                entityId, fromConfig ? "config" : (config == null ? "fallback (config not loaded)" : "fallback (no entry for id)"),
                entry != null ? entry.attributes.size() : 0);
        apply(entity, entry);
    }

    public static void apply(LivingEntity entity, EntityAttributeConfig.Entry entry) {
        if (entry == null) return;
        Difficulty difficulty = entity.getWorld().getDifficulty();
        boolean maxHealthChanged = false;
        for (var e : entry.attributes.entrySet()) {
            RegistryEntry<EntityAttribute> attribute = resolveAttribute(e.getKey());
            if (attribute == null) {
                LOGGER.warn("[EntityAttributeApplier] unresolvable attribute id '{}', skipping", e.getKey());
                continue;
            }
            var instance = entity.getAttributeInstance(attribute);
            if (instance == null) {
                LOGGER.warn("[EntityAttributeApplier] {} has no attribute instance for '{}', skipping", entity.getType(), e.getKey());
                continue;
            }
            double value = resolve(e.getValue(), difficulty);
            instance.setBaseValue(value);
            LOGGER.info("[EntityAttributeApplier] set {} = {}", e.getKey(), value);
            if (e.getKey().equals(EntityAttributes.GENERIC_MAX_HEALTH.getIdAsString())) {
                maxHealthChanged = true;
            }
        }
        if (maxHealthChanged) {
            entity.setHealth((float) entity.getMaxHealth());
        }
    }

    public static double resolve(EntityAttributeConfig.AttributeValue value, Difficulty difficulty) {
        return value.base * multiplier(value.normal_multiplier, value.hard_multiplier, difficulty);
    }

    public static double multiplier(Double normalMultiplier, Double hardMultiplier, Difficulty difficulty) {
        return switch (difficulty) {
            case NORMAL -> normalMultiplier != null ? normalMultiplier : 1.0;
            case HARD -> hardMultiplier != null ? hardMultiplier : 1.0;
            default -> 1.0;
        };
    }

    public static CustomValues custom(LivingEntity entity, EntityAttributeConfig config, EntityAttributeConfig.Entry fallback) {
        return custom(entity, entity.getType().getRegistryEntry().getKey().get().getValue(), config, fallback);
    }

    public static CustomValues custom(LivingEntity entity, Identifier entityId, EntityAttributeConfig config, EntityAttributeConfig.Entry fallback) {
        EntityAttributeConfig.Entry entry = config != null ? config.entryFor(entityId) : null;
        return new CustomValues(entry, fallback, entity.getWorld().getDifficulty());
    }

    public static void registerCustomAttribute(DefaultAttributeContainer.Builder builder, String attributeId, double placeholder) {
        RegistryEntry<EntityAttribute> attribute = resolveAttribute(attributeId);
        if (attribute != null) {
            builder.add(attribute, placeholder);
        }
    }

    public static RegistryEntry<EntityAttribute> resolveAttribute(String attributeId) {
        return Registries.ATTRIBUTE.getEntry(Identifier.of(attributeId)).orElse(null);
    }
}
