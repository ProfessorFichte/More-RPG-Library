package com.mrpg_lib.worldgen;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.worldgen.processor.ContainerLootProcessor;
import com.mrpg_lib.worldgen.processor.ItemFrameItemsProcessor;
import com.mrpg_lib.worldgen.processor.PathAdaptationProcessor;
import com.mrpg_lib.worldgen.processor.RandomReplaceProcessor;
import com.mrpg_lib.worldgen.processor.TerrainBlendingProcessor;
import com.mrpg_lib.worldgen.processor.WaterPillarProcessor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessorType;

public class ModStructureProcessorTypes {

    public static final StructureProcessorType<PathAdaptationProcessor> PATH_ADAPTATION =
            () -> PathAdaptationProcessor.CODEC;

    public static final StructureProcessorType<WaterPillarProcessor> WATER_PILLAR =
            () -> WaterPillarProcessor.CODEC;

    public static final StructureProcessorType<TerrainBlendingProcessor> TERRAIN_BLENDING =
            () -> TerrainBlendingProcessor.CODEC;

    public static final StructureProcessorType<RandomReplaceProcessor> RANDOM_REPLACE =
            () -> RandomReplaceProcessor.CODEC;

    public static final StructureProcessorType<ItemFrameItemsProcessor> ITEM_FRAME_ITEMS =
            () -> ItemFrameItemsProcessor.CODEC;

    public static final StructureProcessorType<ContainerLootProcessor> CONTAINER_LOOT =
            () -> ContainerLootProcessor.CODEC;

    public static void register() {
        Registry.register(
                Registries.STRUCTURE_PROCESSOR,
                MRPGCMod.id("random_replace"),
                RANDOM_REPLACE
        );
        Registry.register(
                Registries.STRUCTURE_PROCESSOR,
                MRPGCMod.id("item_frame_items"),
                ITEM_FRAME_ITEMS
        );
        Registry.register(
                Registries.STRUCTURE_PROCESSOR,
                MRPGCMod.id("container_loot"),
                CONTAINER_LOOT
        );
        Registry.register(
                Registries.STRUCTURE_PROCESSOR,
                MRPGCMod.id("path_adaptation"),
                PATH_ADAPTATION
        );
        Registry.register(
                Registries.STRUCTURE_PROCESSOR,
                MRPGCMod.id("water_pillar"),
                WATER_PILLAR
        );
        Registry.register(
                Registries.STRUCTURE_PROCESSOR,
                MRPGCMod.id("terrain_blending"),
                TERRAIN_BLENDING
        );
    }
}
