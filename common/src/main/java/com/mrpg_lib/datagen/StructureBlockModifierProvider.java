package com.mrpg_lib.datagen;

import com.mrpg_lib.worldgen.modifier.StructureBlockModifier;
import com.mrpg_lib.worldgen.modifier.StructureBlockModifiers;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public abstract class StructureBlockModifierProvider implements DataProvider {
    private final DataOutput.PathResolver paths;

    protected StructureBlockModifierProvider(DataOutput output) {
        this.paths = output.getResolver(DataOutput.OutputType.DATA_PACK, StructureBlockModifiers.DIRECTORY);
    }

    protected abstract void generate(BiConsumer<Identifier, StructureBlockModifier> exporter);

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        generate((id, modifier) -> futures.add(DataProvider.writeToPath(writer,
                StructureBlockModifier.CODEC.encodeStart(JsonOps.INSTANCE, modifier).getOrThrow(), paths.resolveJson(id))));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Structure Block Modifiers";
    }
}
