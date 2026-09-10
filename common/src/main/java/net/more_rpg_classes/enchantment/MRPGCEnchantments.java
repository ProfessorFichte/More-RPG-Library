package net.more_rpg_classes.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.spell_power.api.enchantment.SpellPowerEnchantments;
import net.spell_power.config.EnchantmentsConfig;
import net.spell_power.internals.SchoolFilteredEnchantment;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

/// The two dual-school armor enchantments as Java classes: 1.20.1 has no data-driven enchantments,
/// so `data/more_rpg_classes/enchantment/{typhoon,stonebloom}.json` (and the enchantment tags that
/// referenced them) are replaced by this, mirroring Spell Power's own 1.20.1 port.
///
/// Numbers come straight from the modern JSON: weight 2 → `RARE`, `max_level` 5, cost `1 + 11/level`,
/// `+0.03` multiplied-base per level on both schools, exclusive within `#spell_power:multi_school`.
public class MRPGCEnchantments {

    private static final EnchantmentsConfig.PowerEnchantmentConfig CONFIG =
            new EnchantmentsConfig.PowerEnchantmentConfig(true, 5, 1, 11, 0.03F);

    private static TagKey<Item> enchantableTag(String name) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "enchantable/" + name));
    }

    public static final Identifier TYPHOON_ID = new Identifier(MOD_ID, "typhoon");
    public static final SchoolFilteredEnchantment TYPHOON = (SchoolFilteredEnchantment) new SchoolFilteredEnchantment(
            Enchantment.Rarity.RARE,
            () -> CONFIG,
            Set.of(MoreSpellSchools.AIR, MoreSpellSchools.WATER),
            EnchantmentTarget.ARMOR,
            SpellPowerEnchantments.ARMOR)
            .requireTag(enchantableTag("typhoon"))
            .supportWholeTarget()
            .exclusiveGroup(SpellPowerEnchantments.MULTI_SCHOOL_GROUP);

    public static final Identifier STONEBLOOM_ID = new Identifier(MOD_ID, "stonebloom");
    public static final SchoolFilteredEnchantment STONEBLOOM = (SchoolFilteredEnchantment) new SchoolFilteredEnchantment(
            Enchantment.Rarity.RARE,
            () -> CONFIG,
            Set.of(MoreSpellSchools.EARTH, MoreSpellSchools.NATURE),
            EnchantmentTarget.ARMOR,
            SpellPowerEnchantments.ARMOR)
            .requireTag(enchantableTag("stonebloom"))
            .supportWholeTarget()
            .exclusiveGroup(SpellPowerEnchantments.MULTI_SCHOOL_GROUP);

    public static final Map<Identifier, Enchantment> all = new LinkedHashMap<>(Map.of(
            TYPHOON_ID, TYPHOON,
            STONEBLOOM_ID, STONEBLOOM
    ));

    /// Creation only — the enchantments keyed by the id they register under. Forge iterates this from its
    /// `ENCHANTMENT` `RegisterEvent` window. Skips ids already present, so it is idempotent.
    public static Map<Identifier, Enchantment> enchantmentsToRegister() {
        var toRegister = new LinkedHashMap<>(all);
        toRegister.keySet().removeIf(Registries.ENCHANTMENT::containsId);
        return Collections.unmodifiableMap(toRegister);
    }

    /// The vanilla registration path, used on Fabric.
    public static void register() {
        enchantmentsToRegister().forEach((id, enchantment) -> Registry.register(Registries.ENCHANTMENT, id, enchantment));
    }
}
