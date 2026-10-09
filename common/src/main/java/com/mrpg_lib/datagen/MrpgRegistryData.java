package com.mrpg_lib.datagen;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public final class MrpgRegistryData {
    private MrpgRegistryData() {
    }

    public static final String SPELL_POWER = "spell_power";

    public static final RegistryKey<DamageType> BLEEDING = damageType(MOD_ID, "bleeding");
    public static final RegistryKey<DamageType> FATAL_POISON = damageType(MOD_ID, "fatal_poison");
    public static final RegistryKey<DamageType> MOLTEN = damageType(MOD_ID, "molten");
    public static final Map<RegistryKey<DamageType>, String> OWN_DAMAGE_MESSAGE_IDS = new LinkedHashMap<>();

    static {
        OWN_DAMAGE_MESSAGE_IDS.put(BLEEDING, "mrpgc.bleeding");
        OWN_DAMAGE_MESSAGE_IDS.put(FATAL_POISON, "mrpgc.fatal_poison");
        OWN_DAMAGE_MESSAGE_IDS.put(MOLTEN, "mrpgc.molten");
    }

    public static final RegistryKey<DamageType> AIR = damageType(SPELL_POWER, "air");
    public static final RegistryKey<DamageType> WATER = damageType(SPELL_POWER, "water");
    public static final RegistryKey<DamageType> EARTH = damageType(SPELL_POWER, "earth");
    public static final RegistryKey<DamageType> NATURE = damageType(SPELL_POWER, "nature");
    public static final List<RegistryKey<DamageType>> SPELL_POWER_DAMAGE_TYPES = List.of(AIR, WATER, EARTH, NATURE);

    public static final RegistryKey<Enchantment> TYPHOON = enchantment("typhoon");
    public static final RegistryKey<Enchantment> STONEBLOOM = enchantment("stonebloom");
    public static final List<RegistryKey<Enchantment>> ENCHANTMENTS = List.of(TYPHOON, STONEBLOOM);

    public static final TagKey<Enchantment> MULTI_SCHOOL = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(SPELL_POWER, "multi_school"));

    private static RegistryKey<DamageType> damageType(String namespace, String path) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(namespace, path));
    }

    private static RegistryKey<Enchantment> enchantment(String path) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, path));
    }

    public static void bootstrapDamageTypes(Registerable<DamageType> registerable) {
        OWN_DAMAGE_MESSAGE_IDS.forEach((key, messageId) ->
                registerable.register(key, new DamageType(messageId, DamageScaling.NEVER, 0.0F)));
        for (var key : SPELL_POWER_DAMAGE_TYPES) {
            registerable.register(key, new DamageType("player", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F));
        }
    }

    public static void bootstrapEnchantments(Registerable<Enchantment> registerable) {
        var items = registerable.getRegistryLookup(RegistryKeys.ITEM);
        var enchantments = registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT);
        registerSchoolEnchantment(registerable, items, enchantments, TYPHOON, AIR, WATER);
        registerSchoolEnchantment(registerable, items, enchantments, STONEBLOOM, EARTH, NATURE);
    }

    private static void registerSchoolEnchantment(
            Registerable<Enchantment> registerable,
            RegistryEntryLookup<Item> items,
            RegistryEntryLookup<Enchantment> enchantments,
            RegistryKey<Enchantment> key,
            RegistryKey<DamageType> first,
            RegistryKey<DamageType> second
    ) {
        var supported = items.getOrThrow(TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "enchantable/" + key.getValue().getPath())));
        var builder = Enchantment.builder(Enchantment.definition(
                supported,
                2,
                5,
                Enchantment.leveledCost(1, 11),
                Enchantment.leveledCost(12, 11),
                1,
                AttributeModifierSlot.ARMOR
        )).exclusiveSet(enchantments.getOrThrow(MULTI_SCHOOL));
        for (var school : List.of(first, second)) {
            builder.addEffect(EnchantmentEffectComponentTypes.ATTRIBUTES, new AttributeEnchantmentEffect(
                    key.getValue(),
                    Registries.ATTRIBUTE.getEntry(Identifier.of(SPELL_POWER, school.getValue().getPath())).orElseThrow(),
                    EnchantmentLevelBasedValue.linear(0.03F, 0.03F),
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
        registerable.register(key, builder.build(key.getValue()));
    }
}
