package com.mrpg_lib.compat.spell_power;

import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.spell_power.SpellPowerMod;
import net.spell_power.api.SpellSchool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TavernPotions {
    public static final Identifier BARTENDER = Identifier.of("village_taverns", "bartender");
    public static final int LEVEL = 3;
    private static final int PRICE = 24;
    private static final int MAX_USES = 3;
    private static final int EXPERIENCE = 30;

    private TavernPotions() {
    }

    public static boolean isBartender(VillagerProfession profession) {
        return BARTENDER.equals(Registries.VILLAGER_PROFESSION.getId(profession));
    }

    public static List<TradeOffers.Factory> offers() {
        List<TradeOffers.Factory> offers = new ArrayList<>();
        for (SpellSchool school : List.of(MoreSpellSchools.EARTH, MoreSpellSchools.WATER, MoreSpellSchools.AIR, MoreSpellSchools.NATURE)) {
            potion(school).ifPresent(potion -> offers.add(
                    new TradeOffers.SellItemFactory(PotionContentsComponent.createStack(Items.POTION, potion), PRICE, 1, MAX_USES, EXPERIENCE)));
        }
        return offers;
    }

    public static void registerBrewing(BrewingRecipeRegistry.Builder builder) {
        brew(builder, MoreSpellSchools.EARTH, Items.CLAY_BALL);
        brew(builder, MoreSpellSchools.WATER, Items.PRISMARINE_SHARD);
        brew(builder, MoreSpellSchools.AIR, Items.WIND_CHARGE);
        brew(builder, MoreSpellSchools.NATURE, Items.MOSS_BLOCK);
        invert(builder, MoreSpellSchools.EARTH, MoreSpellSchools.AIR);
        invert(builder, MoreSpellSchools.WATER, MoreSpellSchools.NATURE);
    }

    private static void brew(BrewingRecipeRegistry.Builder builder, SpellSchool school, Item ingredient) {
        potion(school).ifPresent(result -> builder.registerPotionRecipe(Potions.THICK, ingredient, result));
    }

    private static void invert(BrewingRecipeRegistry.Builder builder, SpellSchool first, SpellSchool second) {
        potion(first).ifPresent(a -> potion(second).ifPresent(b -> {
            builder.registerPotionRecipe(a, Items.FERMENTED_SPIDER_EYE, b);
            builder.registerPotionRecipe(b, Items.FERMENTED_SPIDER_EYE, a);
        }));
    }

    private static Optional<RegistryEntry.Reference<Potion>> potion(SpellSchool school) {
        return Registries.POTION.getEntry(SpellPowerMod.potionIdFrom(school.id));
    }
}
