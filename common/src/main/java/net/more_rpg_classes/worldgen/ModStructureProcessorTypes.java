package net.more_rpg_classes.worldgen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessorType;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.worldgen.processor.PathAdaptationProcessor;
import net.more_rpg_classes.worldgen.processor.WaterPillarProcessor;

public class ModStructureProcessorTypes {

    public static final StructureProcessorType<PathAdaptationProcessor> PATH_ADAPTATION =
            () -> PathAdaptationProcessor.CODEC;

    public static final StructureProcessorType<WaterPillarProcessor> WATER_PILLAR =
            () -> WaterPillarProcessor.CODEC;

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
    }
}
