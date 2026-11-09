package com.mrpg_lib.worldgen.processor;

import com.mrpg_lib.MRPGCMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ProcessorBlocks {
    private ProcessorBlocks() {
    }

    public static Optional<Block> resolve(String id, String processor, String field) {
        Identifier identifier = Identifier.tryParse(id);
        Optional<Block> block = identifier == null ? Optional.empty() : Registries.BLOCK.getOrEmpty(identifier);
        if (block.isEmpty()) {
            MRPGCMod.LOGGER.warn("Structure processor {}: unknown block '{}' in '{}'", processor, id, field);
        }
        return block;
    }

    public static boolean inChunk(StructurePlacementData data, BlockPos pos) {
        BlockBox box = data.getBoundingBox();
        return box == null || box.contains(pos);
    }

    public record Matcher(@Nullable TagKey<Block> tag, @Nullable Block block) {
        public static Matcher parse(String raw, String processor, String field) {
            if (raw.startsWith("#")) {
                Identifier id = Identifier.tryParse(raw.substring(1));
                if (id == null) {
                    MRPGCMod.LOGGER.warn("Structure processor {}: invalid tag '{}' in '{}'", processor, raw, field);
                    return new Matcher(null, null);
                }
                return new Matcher(TagKey.of(RegistryKeys.BLOCK, id), null);
            }
            return new Matcher(null, resolve(raw, processor, field).orElse(null));
        }

        public boolean matches(BlockState state) {
            if (tag != null) return state.isIn(tag);
            return block != null && state.isOf(block);
        }
    }
}
