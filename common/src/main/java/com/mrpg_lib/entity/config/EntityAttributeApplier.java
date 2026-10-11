package com.mrpg_lib.entity.config;

import com.mrpg_lib.platform.MrpgPlatform;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EntityAttributeApplier {
    private static final Logger LOGGER = LoggerFactory.getLogger("mrpg_lib/EntityAttributeApplier");
    public static final List<EquipmentSlot> CONFIGURABLE_SLOTS = List.of(
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    public static void apply(LivingEntity entity, EntityAttributeConfig config, EntityAttributeConfig.Entry fallback) {
        apply(entity, entity.getType().getRegistryEntry().getKey().get().getValue(), config, fallback);
    }

    public static void apply(LivingEntity entity, Identifier entityId, EntityAttributeConfig config, EntityAttributeConfig.Entry fallback) {
        EntityAttributeConfig.Entry configured = config != null ? config.entryFor(entityId) : null;
        String source = configured != null ? "config" : config == null ? "fallback (config not loaded)" : "fallback";
        int attributes = applyAttributes(entity, configured != null ? configured : fallback);
        int slots = applyEquipment(entity, configured, fallback);
        LOGGER.debug("{} <- {}: {} attributes, {} equipment slots", entityId, source, attributes, slots);
    }

    public static void apply(LivingEntity entity, EntityAttributeConfig.Entry entry) {
        applyAttributes(entity, entry);
        applyEquipment(entity, entry, null);
    }

    public static int applyAttributes(LivingEntity entity, EntityAttributeConfig.Entry entry) {
        if (entry == null) return 0;
        Difficulty difficulty = entity.getWorld().getDifficulty();
        boolean maxHealthChanged = false;
        int applied = 0;
        for (var e : entry.attributes.entrySet()) {
            RegistryEntry<EntityAttribute> attribute = resolveAttribute(e.getKey());
            if (attribute == null) {
                warnOnce("attribute:" + e.getKey(), "unknown attribute '{}', skipping", e.getKey());
                continue;
            }
            var instance = entity.getAttributeInstance(attribute);
            if (instance == null) {
                warnOnce("instance:" + entity.getType() + "/" + e.getKey(), "{} has no attribute '{}', skipping", entity.getType(), e.getKey());
                continue;
            }
            instance.setBaseValue(resolve(e.getValue(), difficulty));
            applied++;
            if (e.getKey().equals(EntityAttributes.GENERIC_MAX_HEALTH.getIdAsString())) {
                maxHealthChanged = true;
            }
        }
        if (maxHealthChanged) {
            entity.setHealth(entity.getMaxHealth());
        }
        return applied;
    }

    public static int applyEquipment(LivingEntity entity, EntityAttributeConfig.Entry configured, EntityAttributeConfig.Entry fallback) {
        if (!(entity instanceof MobEntity mob)) return 0;
        Map<String, EntityAttributeConfig.EquipmentValue> merged = new LinkedHashMap<>();
        if (fallback != null && fallback.equipment != null) merged.putAll(fallback.equipment);
        if (configured != null && configured.equipment != null) merged.putAll(configured.equipment);
        int equipped = 0;
        for (var e : merged.entrySet()) {
            EquipmentSlot slot = resolveSlot(e.getKey());
            if (slot == null) {
                warnOnce("slot:" + e.getKey(), "unknown equipment slot '{}', expected mainhand, offhand, head, chest, legs or feet", e.getKey());
                continue;
            }
            if (e.getValue() != null && equip(mob, slot, e.getValue())) equipped++;
        }
        return equipped;
    }

    private static boolean equip(MobEntity mob, EquipmentSlot slot, EntityAttributeConfig.EquipmentValue value) {
        Random random = mob.getRandom();
        double chance = value.chanceOrDefault();
        if (chance < 1.0 && random.nextDouble() >= chance) return false;
        ItemStack stack = requiredModsLoaded(value.required_mods) ? resolveStack(value.item, random, value.fallback_item == null) : null;
        if (stack == null && value.fallback_item != null) {
            stack = resolveStack(value.fallback_item, random, true);
        }
        if (stack == null) return false;
        if (!stack.isEmpty() && value.enchantChanceOrDefault() > 0.0 && random.nextDouble() < value.enchantChanceOrDefault()) {
            Optional<RegistryEntryList.Named<Enchantment>> pool = resolveEnchantments(mob, value.enchantTagOrDefault());
            if (pool.isPresent()) {
                stack = EnchantmentHelper.enchant(random, stack, value.enchantLevelsOrDefault(), mob.getWorld().getRegistryManager(), pool);
            }
        }
        mob.equipStack(slot, stack);
        if (value.drop_chance != null) {
            mob.setEquipmentDropChance(slot, value.drop_chance.floatValue());
        }
        return true;
    }

    private static boolean requiredModsLoaded(List<String> modIds) {
        if (modIds == null || modIds.isEmpty()) return true;
        for (String modId : modIds) {
            if (MrpgPlatform.isModLoaded(modId)) return true;
        }
        return false;
    }

    public static EquipmentSlot resolveSlot(String key) {
        for (EquipmentSlot slot : CONFIGURABLE_SLOTS) {
            if (slot.getName().equals(key)) return slot;
        }
        return null;
    }

    private static ItemStack resolveStack(String itemOrTag, Random random, boolean warn) {
        if (itemOrTag == null || itemOrTag.isBlank()) {
            if (warn) warnOnce("item:null", "equipment entry without 'item', skipping");
            return null;
        }
        if (itemOrTag.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(itemOrTag.substring(1));
            Optional<RegistryEntry<Item>> picked = tagId != null
                    ? Registries.ITEM.getRandomEntry(TagKey.of(RegistryKeys.ITEM, tagId), random)
                    : Optional.empty();
            if (picked.isEmpty()) {
                if (warn) warnOnce("tag:" + itemOrTag, "item tag '{}' is missing or empty, skipping", itemOrTag);
                return null;
            }
            return new ItemStack(picked.get());
        }
        Identifier itemId = Identifier.tryParse(itemOrTag);
        if (itemId == null || !Registries.ITEM.containsId(itemId)) {
            if (warn) warnOnce("item:" + itemOrTag, "unknown item '{}', skipping", itemOrTag);
            return null;
        }
        Item item = Registries.ITEM.get(itemId);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static Optional<RegistryEntryList.Named<Enchantment>> resolveEnchantments(MobEntity mob, String enchantmentTag) {
        Identifier tagId = Identifier.tryParse(enchantmentTag.startsWith("#") ? enchantmentTag.substring(1) : enchantmentTag);
        Optional<RegistryEntryList.Named<Enchantment>> pool = tagId != null
                ? mob.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntryList(TagKey.of(RegistryKeys.ENCHANTMENT, tagId))
                : Optional.empty();
        if (pool.isEmpty()) {
            warnOnce("enchant_tag:" + enchantmentTag, "enchantment tag '{}' is missing, not enchanting", enchantmentTag);
        }
        return pool;
    }

    private static void warnOnce(String key, String message, Object... args) {
        if (WARNED.add(key)) {
            LOGGER.warn(message, args);
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
        Identifier id = Identifier.tryParse(attributeId);
        return id != null ? Registries.ATTRIBUTE.getEntry(id).orElse(null) : null;
    }
}
