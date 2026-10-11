package com.mrpg_lib.entity.config;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
        public LinkedHashMap<String, EquipmentValue> equipment = null;

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

        public Entry setEquipment(String slot, String itemOrTag) {
            return setEquipment(slot, new EquipmentValue(itemOrTag));
        }

        public Entry setEquipment(String slot, String itemOrTag, double chance, double enchantChance) {
            return setEquipment(slot, new EquipmentValue(itemOrTag).chance(chance).enchant(enchantChance));
        }

        public Entry setEquipment(String slot, EquipmentValue value) {
            if (equipment == null) equipment = new LinkedHashMap<>();
            equipment.put(slot, value);
            return this;
        }

        public Entry setEquipment(EquipmentSlot slot, String itemOrTag) {
            return setEquipment(slot.getName(), itemOrTag);
        }

        public Entry setEquipment(EquipmentSlot slot, String itemOrTag, double chance, double enchantChance) {
            return setEquipment(slot.getName(), itemOrTag, chance, enchantChance);
        }

        public Entry setEquipment(EquipmentSlot slot, EquipmentValue value) {
            return setEquipment(slot.getName(), value);
        }

        public EquipmentValue getEquipment(String slot) {
            return equipment != null ? equipment.get(slot) : null;
        }
    }

    public static class EquipmentValue {
        public static final String EMPTY = "minecraft:air";
        public static final int DEFAULT_ENCHANT_LEVELS = 20;
        public static final String DEFAULT_ENCHANT_TAG = "#minecraft:in_enchanting_table";

        public String item = null;
        public Double chance = null;
        public Double enchant_chance = null;
        public Integer enchant_levels = null;
        public String enchant_tag = null;
        public Double drop_chance = null;
        public List<String> required_mods = null;
        public String fallback_item = null;

        public EquipmentValue() {
        }

        public EquipmentValue(String itemOrTag) {
            this.item = itemOrTag;
        }

        public static EquipmentValue of(String itemOrTag) {
            return new EquipmentValue(itemOrTag);
        }

        public EquipmentValue requiresAnyMod(String... modIds) {
            this.required_mods = modIds.length > 0 ? new ArrayList<>(List.of(modIds)) : null;
            return this;
        }

        public EquipmentValue fallback(String itemOrTag) {
            this.fallback_item = itemOrTag;
            return this;
        }

        public EquipmentValue chance(double chance) {
            this.chance = chance < 1.0 ? chance : null;
            return this;
        }

        public EquipmentValue enchant(double enchantChance) {
            this.enchant_chance = enchantChance > 0.0 ? enchantChance : null;
            return this;
        }

        public EquipmentValue enchant(double enchantChance, int levels) {
            this.enchant_levels = levels;
            return enchant(enchantChance);
        }

        public EquipmentValue enchantTag(String enchantmentTag) {
            this.enchant_tag = enchantmentTag;
            return this;
        }

        public EquipmentValue dropChance(double dropChance) {
            this.drop_chance = dropChance;
            return this;
        }

        public double chanceOrDefault() {
            return chance != null ? chance : 1.0;
        }

        public double enchantChanceOrDefault() {
            return enchant_chance != null ? enchant_chance : 0.0;
        }

        public int enchantLevelsOrDefault() {
            return enchant_levels != null ? enchant_levels : DEFAULT_ENCHANT_LEVELS;
        }

        public String enchantTagOrDefault() {
            return enchant_tag != null ? enchant_tag : DEFAULT_ENCHANT_TAG;
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
