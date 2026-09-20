package net.more_rpg_classes.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class ConditionalItemEntry extends LeafEntry {
    public static final String NAME = "conditional_item";
    public static final Identifier ID = new Identifier(MOD_ID, NAME);

    private final Identifier itemId;
    @Nullable private final LootNumberProvider count;

    public ConditionalItemEntry(
            Identifier itemId,
            @Nullable LootNumberProvider count,
            int weight,
            int quality,
            LootCondition[] conditions,
            LootFunction[] functions
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
        int c = count != null ? count.nextInt(context) : 1;
        if (c < 1) c = 1;

        ItemStack stack = new ItemStack(item, c);

        for (LootFunction fn : this.functions) {
            stack = fn.apply(stack, context);
        }

        lootConsumer.accept(stack);
    }

    public Identifier getItemId() { return this.itemId; }
    @Nullable public LootNumberProvider getCount() { return this.count; }
    public int getWeightValue() { return this.weight; }
    public int getQualityValue() { return this.quality; }
    public LootCondition[] getConditionsList() { return this.conditions; }
    public LootFunction[] getFunctionsList() { return this.functions; }

    public static class Serializer extends LeafEntry.Serializer<ConditionalItemEntry> {
        @Override
        public void addEntryFields(JsonObject json, ConditionalItemEntry entry, JsonSerializationContext context) {
            super.addEntryFields(json, entry, context);
            json.addProperty("item", entry.itemId.toString());
            if (entry.count != null) {
                json.add("count", context.serialize(entry.count));
            }
        }

        @Override
        protected ConditionalItemEntry fromJson(
                JsonObject json, JsonDeserializationContext context, int weight, int quality,
                LootCondition[] conditions, LootFunction[] functions
        ) {
            Identifier itemId = new Identifier(JsonHelper.getString(json, "item"));
            LootNumberProvider count = json.has("count")
                    ? JsonHelper.deserialize(json, "count", context, LootNumberProvider.class)
                    : null;
            return new ConditionalItemEntry(itemId, count, weight, quality, conditions, functions);
        }
    }
}
