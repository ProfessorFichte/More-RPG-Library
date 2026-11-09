package com.mrpg_lib.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrpg_lib.worldgen.ModStructureProcessorTypes;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

import java.util.ArrayList;
import java.util.List;

public class ItemFrameItemsProcessor extends StructureProcessor implements EntityNbtProcessor {

    private static final List<String> FRAMES = List.of("minecraft:item_frame", "minecraft:glow_item_frame");

    public static final MapCodec<ItemFrameItemsProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.listOf().fieldOf("items").forGetter(p -> p.items),
                    Codec.STRING.listOf().optionalFieldOf("entities", FRAMES).forGetter(p -> p.entities),
                    Codec.BOOL.optionalFieldOf("only_empty", true).forGetter(p -> p.onlyEmpty),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(p -> p.chance)
            ).apply(instance, ItemFrameItemsProcessor::new)
    );

    private final List<String> items;
    private final List<String> entities;
    private final boolean onlyEmpty;
    private final float chance;

    public ItemFrameItemsProcessor(List<String> items, List<String> entities, boolean onlyEmpty, float chance) {
        this.items = items;
        this.entities = entities;
        this.onlyEmpty = onlyEmpty;
        this.chance = chance;
    }

    @Override
    public void processEntity(ServerWorldAccess world, NbtCompound nbt, Random random) {
        if (!entities.contains(nbt.getString("id"))) return;
        reattach(nbt);
        if (onlyEmpty && !nbt.getCompound("Item").isEmpty()) return;
        if (random.nextFloat() >= chance) return;
        List<Item> pool = resolvePool();
        if (pool.isEmpty()) return;
        Item item = pool.get(random.nextInt(pool.size()));
        NbtCompound stack = new NbtCompound();
        stack.putString("id", Registries.ITEM.getId(item).toString());
        stack.putInt("count", 1);
        nbt.put("Item", stack);
    }

    // Vanilla moves template entities with setPos and keeps the old TileX/Y/Z from where the template was saved.
    // A frame that got an item from us then didn't spawn at all, so we point it at the block it really sits in.
    private static void reattach(NbtCompound nbt) {
        NbtList pos = nbt.getList("Pos", NbtElement.DOUBLE_TYPE);
        if (pos.size() != 3 || !nbt.contains("TileX")) return;
        BlockPos block = BlockPos.ofFloored(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
        nbt.putInt("TileX", block.getX());
        nbt.putInt("TileY", block.getY());
        nbt.putInt("TileZ", block.getZ());
    }

    private List<Item> resolvePool() {
        List<Item> pool = new ArrayList<>();
        for (String entry : items) {
            if (entry.startsWith("#")) {
                Identifier tag = Identifier.tryParse(entry.substring(1));
                if (tag == null) continue;
                for (RegistryEntry<Item> item : Registries.ITEM.iterateEntries(TagKey.of(RegistryKeys.ITEM, tag))) {
                    pool.add(item.value());
                }
            } else {
                Identifier id = Identifier.tryParse(entry);
                if (id != null && Registries.ITEM.containsId(id)) pool.add(Registries.ITEM.get(id));
            }
        }
        return pool;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.ITEM_FRAME_ITEMS;
    }
}
