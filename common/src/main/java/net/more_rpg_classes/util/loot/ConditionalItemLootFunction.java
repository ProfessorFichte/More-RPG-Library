package net.more_rpg_classes.util.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class ConditionalItemLootFunction extends ConditionalLootFunction {
    public static final String NAME = "conditional_item";
    public static final Identifier ID = Identifier.of(MOD_ID, NAME);

    public static final MapCodec<ConditionalItemLootFunction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> addConditionsField(instance)
                    .and(
                            Codec.STRING.fieldOf("conditional_item")
                                    .forGetter(f -> f.conditionalItemId.toString())
                    )
                    .apply(instance, (conditions, id) ->
                            new ConditionalItemLootFunction(conditions, Identifier.of(id))
                    )
            );

    public static final LootFunctionType<ConditionalItemLootFunction> TYPE =
            new LootFunctionType<>(CODEC);

    private final Identifier conditionalItemId;

    public ConditionalItemLootFunction(
            List<LootCondition> conditions,
            Identifier conditionalItemId
    ) {
        super(conditions);
        this.conditionalItemId = conditionalItemId;
    }

    @Override
    public LootFunctionType<ConditionalItemLootFunction> getType() {
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
}