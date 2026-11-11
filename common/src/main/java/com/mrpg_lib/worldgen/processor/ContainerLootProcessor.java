package com.mrpg_lib.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrpg_lib.worldgen.ModStructureProcessorTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

import java.util.ArrayList;
import java.util.List;

public class ContainerLootProcessor extends StructureProcessor {

    private static final String LOOT_TABLE_KEY = "LootTable";
    private static final String LOOT_TABLE_SEED_KEY = "LootTableSeed";

    public static final MapCodec<ContainerLootProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("target").forGetter(p -> p.target),
                    Identifier.CODEC.fieldOf("loot_table").forGetter(p -> p.lootTable),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(p -> p.chance),
                    Codec.BOOL.optionalFieldOf("replace_existing", false).forGetter(p -> p.replaceExisting)
            ).apply(instance, ContainerLootProcessor::new)
    );

    private final String target;
    private final Identifier lootTable;
    private final float chance;
    private final boolean replaceExisting;
    private final ProcessorBlocks.Matcher matcher;
    private final long salt;

    public ContainerLootProcessor(String target, Identifier lootTable, float chance, boolean replaceExisting) {
        this.target = target;
        this.lootTable = lootTable;
        this.chance = chance;
        this.replaceExisting = replaceExisting;
        this.matcher = ProcessorBlocks.Matcher.parse(target, "mrpg_lib:container_loot", "target");
        this.salt = ProcessorBlocks.salt(target, lootTable.toString());
    }

    @Override
    public List<StructureBlockInfo> reprocess(ServerWorldAccess world, BlockPos pos, BlockPos pivot,
                                              List<StructureBlockInfo> originalBlockInfos,
                                              List<StructureBlockInfo> currentBlockInfos,
                                              StructurePlacementData data) {
        long seed = world.toServerWorld().getSeed() ^ salt;
        List<StructureBlockInfo> result = null;
        for (int i = 0; i < currentBlockInfos.size(); i++) {
            StructureBlockInfo info = currentBlockInfos.get(i);
            if (!info.state().hasBlockEntity() || !matcher.matches(info.state())) continue;
            if (!replaceExisting && info.nbt() != null && info.nbt().contains(LOOT_TABLE_KEY)) continue;
            Random random = ProcessorBlocks.blockRandom(seed, info.pos());
            if (random.nextFloat() >= chance) continue;
            NbtCompound nbt = info.nbt() != null ? info.nbt().copy() : new NbtCompound();
            nbt.putString(LOOT_TABLE_KEY, lootTable.toString());
            nbt.putLong(LOOT_TABLE_SEED_KEY, random.nextLong());
            if (result == null) result = new ArrayList<>(currentBlockInfos);
            result.set(i, new StructureBlockInfo(info.pos(), info.state(), nbt));
        }
        return result != null ? result : currentBlockInfos;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.CONTAINER_LOOT;
    }
}
