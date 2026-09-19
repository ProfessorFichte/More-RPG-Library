package net.more_rpg_classes.worldgen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.worldgen.structure.ConditionalJigsawStructure;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModStructureTypes {

    public static final StructureType<ConditionalJigsawStructure> CONDITIONAL_JIGSAW = () -> ConditionalJigsawStructure.CODEC;

    /// Creation only — the structure types keyed by the id they register under. Forge iterates this from
    /// its `worldgen/structure_type` `RegisterEvent` window.
    public static Map<Identifier, StructureType<?>> typesToRegister() {
        var toRegister = new LinkedHashMap<Identifier, StructureType<?>>();
        put(toRegister, MRPGCMod.id("conditional_jigsaw"), CONDITIONAL_JIGSAW);
        return Collections.unmodifiableMap(toRegister);
    }

    private static void put(Map<Identifier, StructureType<?>> toRegister, Identifier id, StructureType<?> type) {
        if (Registries.STRUCTURE_TYPE.containsId(id)) { return; }
        toRegister.put(id, type);
    }

    /// The vanilla registration path, used on Fabric.
    public static void register() {
        typesToRegister().forEach((id, type) -> Registry.register(Registries.STRUCTURE_TYPE, id, type));
    }
}
