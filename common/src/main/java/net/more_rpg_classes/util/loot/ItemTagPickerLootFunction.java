package net.more_rpg_classes.util.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class ItemTagPickerLootFunction extends ConditionalLootFunction {
    public static final String NAME = "item_tag_picker";
    public static final Identifier ID = Identifier.of(MOD_ID, NAME);

    public static final MapCodec<ItemTagPickerLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            addConditionsField(instance)
                    .and(instance.group(
                            Codec.STRING.listOf()
                                    .fieldOf("item_tags")
                                    .orElse(List.of())
                                    .forGetter(f -> f.itemTags),
                            LootNumberProviderTypes.CODEC
                                    .fieldOf("count")
                                    .orElse(ConstantLootNumberProvider.create(1))
                                    .forGetter(f -> f.count),
                            Codec.FLOAT
                                    .fieldOf("enchantment_probability")
                                    .orElse(0.0F)
                                    .forGetter(f -> f.enchantmentProbability),
                            LootNumberProviderTypes.CODEC
                                    .fieldOf("enchantment_level_min")
                                    .orElse(ConstantLootNumberProvider.create(1))
                                    .forGetter(f -> f.enchantmentLevelMin),
                            LootNumberProviderTypes.CODEC
                                    .fieldOf("enchantment_level_max")
                                    .orElse(ConstantLootNumberProvider.create(3))
                                    .forGetter(f -> f.enchantmentLevelMax)
                    ))
                    .apply(instance, ItemTagPickerLootFunction::new)
    );

    public static final LootFunctionType<ItemTagPickerLootFunction> TYPE = new LootFunctionType<>(CODEC);

    private final List<String> itemTags;
    private final LootNumberProvider count;
    private final float enchantmentProbability;
    private final LootNumberProvider enchantmentLevelMin;
    private final LootNumberProvider enchantmentLevelMax;

    public ItemTagPickerLootFunction(
            List<LootCondition> conditions,
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
    public LootFunctionType<ItemTagPickerLootFunction> getType() {
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
            TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, Identifier.of(tagStr.substring(1)));
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
            result = EnchantmentHelper.enchant(
                    context.getRandom(),
                    result,
                    level,
                    context.getWorld().getRegistryManager(),
                    Optional.<RegistryEntryList<Enchantment>>empty()
            );
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
}
