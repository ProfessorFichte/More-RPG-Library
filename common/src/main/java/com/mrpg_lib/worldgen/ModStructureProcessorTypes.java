package com.mrpg_lib.worldgen;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.worldgen.processor.PathAdaptationProcessor;
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

    public static void register() {
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
