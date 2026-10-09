package com.mrpg_lib.entity.config;

import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;

import java.util.LinkedHashMap;

public class EntityAttributeConfig {
    public LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();

    public Entry entryFor(Identifier entityId) {
        return entries.get(entityId.toString());
    }

    public Entry entryFor(String entityId) {
        return entries.get(entityId);
    }

    public static class Entry {
        public LinkedHashMap<String, AttributeValue> attributes = new LinkedHashMap<>();
        public LinkedHashMap<String, CustomValue> custom = null;

        public Entry set(String attributeId, double base) {
            attributes.put(attributeId, new AttributeValue(base));
            return this;
        }

        public Entry set(String attributeId, double base, double normalMultiplier, double hardMultiplier) {
            attributes.put(attributeId, new AttributeValue(base, normalMultiplier, hardMultiplier));
            return this;
        }

        public Entry setCustom(String id, double base) {
            return putCustom(id, new CustomValue(base, null, null, null, null, null));
        }

        public Entry setCustom(String id, double base, double normalMultiplier, double hardMultiplier) {
            return putCustom(id, new CustomValue(base, normalMultiplier, hardMultiplier, null, null, null));
        }

        public Entry setCustomString(String id, String string) {
            return putCustom(id, new CustomValue(null, null, null, string, null, null));
        }

        public Entry setCustomString(String id, String string, String normalString, String hardString) {
            return putCustom(id, new CustomValue(null, null, null, string, normalString, hardString));
        }

        private Entry putCustom(String id, CustomValue value) {
            if (custom == null) custom = new LinkedHashMap<>();
            custom.put(id, value);
            return this;
        }

        public CustomValue getCustom(String id) {
            return custom != null ? custom.get(id) : null;
        }

        public boolean hasNumber(String id) {
            CustomValue value = getCustom(id);
            return value != null && value.base != null;
        }

        public boolean hasString(String id) {
            CustomValue value = getCustom(id);
            return value != null && value.string != null;
        }

        public double getDouble(String id, Difficulty difficulty, double defaultValue) {
            CustomValue value = getCustom(id);
            return value != null && value.base != null ? value.resolve(difficulty) : defaultValue;
        }

        public int getInt(String id, Difficulty difficulty, int defaultValue) {
            CustomValue value = getCustom(id);
            return value != null && value.base != null ? (int) Math.round(value.resolve(difficulty)) : defaultValue;
        }

        public boolean getBoolean(String id, Difficulty difficulty, boolean defaultValue) {
            CustomValue value = getCustom(id);
            return value != null && value.base != null ? value.resolve(difficulty) != 0 : defaultValue;
        }

        public String getString(String id, Difficulty difficulty, String defaultValue) {
            CustomValue value = getCustom(id);
            return value != null && value.string != null ? value.resolveString(difficulty) : defaultValue;
        }
    }

    public static class AttributeValue {
        public double base = 0;
        public Double normal_multiplier = null;
        public Double hard_multiplier = null;

        public AttributeValue() {
        }

        public AttributeValue(double base) {
            this.base = base;
        }

        public AttributeValue(double base, double normalMultiplier, double hardMultiplier) {
            this.base = base;
            this.normal_multiplier = normalMultiplier;
            this.hard_multiplier = hardMultiplier;
        }
    }

    public static class CustomValue {
        public Double base = null;
        public Double normal_multiplier = null;
        public Double hard_multiplier = null;
        public String string = null;
        public String normal_string = null;
        public String hard_string = null;

        public CustomValue() {
        }

        public CustomValue(Double base, Double normalMultiplier, Double hardMultiplier, String string, String normalString, String hardString) {
            this.base = base;
            this.normal_multiplier = normalMultiplier;
            this.hard_multiplier = hardMultiplier;
            this.string = string;
            this.normal_string = normalString;
            this.hard_string = hardString;
        }

        public double resolve(Difficulty difficulty) {
            if (base == null) return 0;
            return base * EntityAttributeApplier.multiplier(normal_multiplier, hard_multiplier, difficulty);
        }

        public String resolveString(Difficulty difficulty) {
            return switch (difficulty) {
                case NORMAL -> normal_string != null ? normal_string : string;
                case HARD -> hard_string != null ? hard_string : string;
                default -> string;
            };
        }
    }
}
