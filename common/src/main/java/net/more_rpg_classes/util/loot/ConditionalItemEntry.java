package net.more_rpg_classes.util.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class ConditionalItemEntry extends LeafEntry {
    public static final String NAME = "conditional_item";
    public static final Identifier ID = Identifier.of(MOD_ID, NAME);

    private final Identifier itemId;
    private final Optional<LootNumberProvider> count;

    public ConditionalItemEntry(
            Identifier itemId,
            Optional<LootNumberProvider> count,
            int weight,
            int quality,
            List<LootCondition> conditions,
            List<LootFunction> functions
    ) {
        super(weight, quality, conditions, functions);
        this.itemId = itemId;
        this.count = count;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.ITEM;
    }

    @Override
    protected void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        if (!Registries.ITEM.containsId(itemId)) return;

        Item item = Registries.ITEM.get(itemId);
        int c = count.map(provider -> provider.nextInt(context)).orElse(1);
        if (c < 1) c = 1;

        ItemStack stack = new ItemStack(item, c);

        for (LootFunction fn : this.functions) {
            stack = fn.apply(stack, context);
        }

        lootConsumer.accept(stack);
    }

    public Identifier getItemId() { return this.itemId; }
    public Optional<LootNumberProvider> getCount() { return this.count; }
    public int getWeightValue() { return this.weight; }
    public int getQualityValue() { return this.quality; }
    public List<LootCondition> getConditionsList() { return this.conditions; }
    public List<LootFunction> getFunctionsList() { return this.functions; }

    public static final MapCodec<ConditionalItemEntry> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.STRING.xmap(Identifier::of, Identifier::toString).fieldOf("item").forGetter(e -> e.itemId),
                    LootNumberProviderTypes.CODEC.optionalFieldOf("count").forGetter(e -> e.count),
                    Codec.INT.optionalFieldOf("weight", 1).forGetter(e -> e.weight),
                    Codec.INT.optionalFieldOf("quality", 0).forGetter(e -> e.quality),
                    LootCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(e -> e.conditions),
                    LootFunctionTypes.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(e -> e.functions)
            ).apply(instance, ConditionalItemEntry::new)
    );

}
