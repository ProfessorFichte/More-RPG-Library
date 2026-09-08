package net.more_rpg_classes.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Set;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

// 1.20.1: loot functions serialize through JsonSerializer, not a Codec.
public class ConditionalItemLootFunction extends ConditionalLootFunction {
    public static final String NAME = "conditional_item";
    public static final Identifier ID = new Identifier(MOD_ID, NAME);

    public static final LootFunctionType TYPE = new LootFunctionType(new Serializer());

    private final Identifier conditionalItemId;

    public ConditionalItemLootFunction(
            LootCondition[] conditions,
            Identifier conditionalItemId
    ) {
        super(conditions);
        this.conditionalItemId = conditionalItemId;
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
        if (Registries.ITEM.containsId(conditionalItemId)) {
            Item item = Registries.ITEM.get(conditionalItemId);
            return new ItemStack(item);
        }
        return stack;
    }

    public static Builder<?> builder(Identifier conditionalItem) {
        return builder(conditions -> new ConditionalItemLootFunction(conditions, conditionalItem));
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<ConditionalItemLootFunction> {
        @Override
        public void toJson(JsonObject json, ConditionalItemLootFunction function, JsonSerializationContext context) {
            super.toJson(json, function, context);
            json.addProperty("conditional_item", function.conditionalItemId.toString());
        }

        @Override
        public ConditionalItemLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new ConditionalItemLootFunction(conditions, new Identifier(JsonHelper.getString(json, "conditional_item")));
        }
    }
}
