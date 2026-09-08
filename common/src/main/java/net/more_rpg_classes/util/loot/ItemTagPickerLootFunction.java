package net.more_rpg_classes.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class ItemTagPickerLootFunction extends ConditionalLootFunction {
    public static final String NAME = "item_tag_picker";
    public static final Identifier ID = new Identifier(MOD_ID, NAME);

    public static final LootFunctionType TYPE = new LootFunctionType(new Serializer());

    private final List<String> itemTags;
    private final LootNumberProvider count;
    private final float enchantmentProbability;
    private final LootNumberProvider enchantmentLevelMin;
    private final LootNumberProvider enchantmentLevelMax;

    public ItemTagPickerLootFunction(
            LootCondition[] conditions,
            List<String> itemTags,
            LootNumberProvider count,
            float enchantmentProbability,
            LootNumberProvider enchantmentLevelMin,
            LootNumberProvider enchantmentLevelMax) {
        super(conditions);
        this.itemTags = itemTags;
        this.count = count;
        this.enchantmentProbability = enchantmentProbability;
        this.enchantmentLevelMin = enchantmentLevelMin;
        this.enchantmentLevelMax = enchantmentLevelMax;
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Set.of();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        List<Item> candidates = new ArrayList<>();
        for (String tagStr : itemTags) {
            if (!tagStr.startsWith("#")) continue;
            TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, new Identifier(tagStr.substring(1)));
            Optional<RegistryEntryList.Named<Item>> tagList = Registries.ITEM.getEntryList(tagKey);
            tagList.ifPresent(entries -> entries.forEach(entry -> candidates.add(entry.value())));
        }

        if (candidates.isEmpty()) return stack;

        Item picked = candidates.get(context.getRandom().nextInt(candidates.size()));
        int pickCount = Math.max(1, count.nextInt(context));
        ItemStack result = new ItemStack(picked, pickCount);

        if (enchantmentProbability > 0 && context.getRandom().nextFloat() < enchantmentProbability) {
            int a = enchantmentLevelMin.nextInt(context);
            int b = enchantmentLevelMax.nextInt(context);
            int level = a + (b > a ? context.getRandom().nextInt(b - a + 1) : 0);
            result = EnchantmentHelper.enchant(context.getRandom(), result, level, false);
        }

        return result;
    }

    public static Builder<?> builder(
            List<String> itemTags,
            LootNumberProvider count,
            float enchantmentProbability,
            LootNumberProvider enchantmentLevelMin,
            LootNumberProvider enchantmentLevelMax) {
        return builder(conditions -> new ItemTagPickerLootFunction(
                conditions, itemTags, count, enchantmentProbability, enchantmentLevelMin, enchantmentLevelMax));
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<ItemTagPickerLootFunction> {
        @Override
        public void toJson(JsonObject json, ItemTagPickerLootFunction function, JsonSerializationContext context) {
            super.toJson(json, function, context);
            json.add("item_tags", context.serialize(function.itemTags));
            json.add("count", context.serialize(function.count));
            json.addProperty("enchantment_probability", function.enchantmentProbability);
            json.add("enchantment_level_min", context.serialize(function.enchantmentLevelMin));
            json.add("enchantment_level_max", context.serialize(function.enchantmentLevelMax));
        }

        @Override
        public ItemTagPickerLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            List<String> itemTags = new ArrayList<>();
            if (json.has("item_tags")) {
                for (var element : JsonHelper.getArray(json, "item_tags")) {
                    itemTags.add(element.getAsString());
                }
            }
            LootNumberProvider count = json.has("count")
                    ? JsonHelper.deserialize(json, "count", context, LootNumberProvider.class)
                    : ConstantLootNumberProvider.create(1);
            float probability = JsonHelper.getFloat(json, "enchantment_probability", 0.0F);
            LootNumberProvider levelMin = json.has("enchantment_level_min")
                    ? JsonHelper.deserialize(json, "enchantment_level_min", context, LootNumberProvider.class)
                    : ConstantLootNumberProvider.create(1);
            LootNumberProvider levelMax = json.has("enchantment_level_max")
                    ? JsonHelper.deserialize(json, "enchantment_level_max", context, LootNumberProvider.class)
                    : ConstantLootNumberProvider.create(3);
            return new ItemTagPickerLootFunction(conditions, itemTags, count, probability, levelMin, levelMax);
        }
    }
}
