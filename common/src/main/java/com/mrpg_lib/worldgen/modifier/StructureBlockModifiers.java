package com.mrpg_lib.worldgen.modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.platform.MrpgPlatform;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StructureBlockModifiers extends JsonDataLoader {
    public static final String DIRECTORY = "mrpg_lib/structure_block_modifier";
    public static final Identifier ID = MRPGCMod.id("structure_block_modifiers");
    private static final Gson GSON = new GsonBuilder().create();

    private static volatile List<StructureBlockModifier.Compiled> modifiers = List.of();

    public StructureBlockModifiers() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, Profiler profiler) {
        List<StructureBlockModifier.Compiled> loaded = new ArrayList<>();
        files.forEach((id, json) -> {
            if (!requiredModsLoaded(json)) return;
            StructureBlockModifier.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> MRPGCMod.LOGGER.error("Skipping structure block modifier {}: {}", id, error))
                    .ifPresent(modifier -> loaded.add(modifier.compile(id)));
        });
        modifiers = List.copyOf(loaded);
        MRPGCMod.LOGGER.info("Loaded {} structure block modifiers", loaded.size());
    }

    private static boolean requiredModsLoaded(JsonElement json) {
        if (!(json instanceof JsonObject object) || !object.has("required_mods")) return true;
        for (JsonElement mod : object.getAsJsonArray("required_mods")) {
            if (!MrpgPlatform.isModLoaded(mod.getAsString())) return false;
        }
        return true;
    }

    public static void afterPiece(StructureWorldAccess world, Structure structure, StructurePiece piece, BlockBox chunkBox) {
        List<StructureBlockModifier.Compiled> current = modifiers;
        if (current.isEmpty()) return;
        BlockBox pieceBox = piece.getBoundingBox();
        if (!pieceBox.intersects(chunkBox)) return;
        RegistryEntry<Structure> entry = world.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntry(structure);
        BlockBox box = new BlockBox(
                Math.max(pieceBox.getMinX(), chunkBox.getMinX()), Math.max(pieceBox.getMinY(), chunkBox.getMinY()),
                Math.max(pieceBox.getMinZ(), chunkBox.getMinZ()), Math.min(pieceBox.getMaxX(), chunkBox.getMaxX()),
                Math.min(pieceBox.getMaxY(), chunkBox.getMaxY()), Math.min(pieceBox.getMaxZ(), chunkBox.getMaxZ()));
        for (StructureBlockModifier.Compiled modifier : current) {
            if (modifier.matches(entry, piece)) modifier.apply(world, box);
        }
    }
}
