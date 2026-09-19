package net.more_rpg_classes.worldgen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.worldgen.processor.PathAdaptationProcessor;
import net.more_rpg_classes.worldgen.processor.TerrainBlendingProcessor;
import net.more_rpg_classes.worldgen.processor.WaterPillarProcessor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModStructureProcessorTypes {

    public static final StructureProcessorType<PathAdaptationProcessor> PATH_ADAPTATION =
            () -> PathAdaptationProcessor.CODEC;

    public static final StructureProcessorType<WaterPillarProcessor> WATER_PILLAR =
            () -> WaterPillarProcessor.CODEC;

    public static final StructureProcessorType<TerrainBlendingProcessor> TERRAIN_BLENDING =
            () -> TerrainBlendingProcessor.CODEC;

    /// Creation only — the processor types keyed by the id they register under. `worldgen/structure_processor`
    /// is its own registry with its own `RegisterEvent` window, separate from `worldgen/structure_type`.
    public static Map<Identifier, StructureProcessorType<?>> typesToRegister() {
        var toRegister = new LinkedHashMap<Identifier, StructureProcessorType<?>>();
        put(toRegister, MRPGCMod.id("path_adaptation"), PATH_ADAPTATION);
        put(toRegister, MRPGCMod.id("water_pillar"), WATER_PILLAR);
        put(toRegister, MRPGCMod.id("terrain_blending"), TERRAIN_BLENDING);
        return Collections.unmodifiableMap(toRegister);
    }

    private static void put(Map<Identifier, StructureProcessorType<?>> toRegister, Identifier id, StructureProcessorType<?> type) {
        if (Registries.STRUCTURE_PROCESSOR.containsId(id)) { return; }
        toRegister.put(id, type);
    }

    /// The vanilla registration path, used on Fabric.
    public static void register() {
        typesToRegister().forEach((id, type) -> Registry.register(Registries.STRUCTURE_PROCESSOR, id, type));
    }
}
