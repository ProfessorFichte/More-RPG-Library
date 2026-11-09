package com.mrpg_lib.worldgen.processor;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

public interface EntityNbtProcessor {

    void processEntity(ServerWorldAccess world, NbtCompound nbt, Random random);

    ThreadLocal<StructurePlacementData> PLACING = new ThreadLocal<>();

    static void apply(ServerWorldAccess world, NbtCompound nbt) {
        StructurePlacementData data = PLACING.get();
        if (data == null) return;
        BlockPos pos = BlockPos.ORIGIN;
        NbtList list = nbt.getList("Pos", NbtElement.DOUBLE_TYPE);
        if (list.size() == 3) {
            pos = BlockPos.ofFloored(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
        for (StructureProcessor processor : data.getProcessors()) {
            if (processor instanceof EntityNbtProcessor entityProcessor) {
                entityProcessor.processEntity(world, nbt, data.getRandom(pos));
            }
        }
    }
}
