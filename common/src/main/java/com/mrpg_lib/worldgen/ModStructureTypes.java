package com.mrpg_lib.worldgen;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.worldgen.structure.ConditionalJigsawStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.StructureType;

public class ModStructureTypes {

    public static final StructureType<ConditionalJigsawStructure> CONDITIONAL_JIGSAW = () -> ConditionalJigsawStructure.CODEC;

    public static void register() {
        Registry.register(
                Registries.STRUCTURE_TYPE,
                MRPGCMod.id("conditional_jigsaw"),
                CONDITIONAL_JIGSAW
        );
    }
}
