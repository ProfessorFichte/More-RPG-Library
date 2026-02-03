package net.more_rpg_classes.worldgen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.StructureType;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.worldgen.structure.ConditionalJigsawStructure;

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
