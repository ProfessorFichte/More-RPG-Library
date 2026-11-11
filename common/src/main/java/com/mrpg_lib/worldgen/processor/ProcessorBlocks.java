package com.mrpg_lib.worldgen.processor;

import com.mrpg_lib.MRPGCMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Property;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
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

    public static long salt(String... parts) {
        long salt = 1125899906842597L;
        for (String part : parts) {
            for (int i = 0; i < part.length(); i++) {
                salt = 31 * salt + part.charAt(i);
            }
            salt = 31 * salt + '|';
        }
        return salt;
    }

    public static Random blockRandom(long seed, BlockPos pos) {
        return Random.create(seed ^ MathHelper.hashCode(pos));
    }

    public static BlockState copyProperties(BlockState from, BlockState to) {
        BlockState result = to;
        for (Property<?> property : from.getProperties()) {
            Property<?> target = to.getBlock().getStateManager().getProperty(property.getName());
            if (target != null) result = copyValue(from, property, result, target);
        }
        return result;
    }

    private static <T extends Comparable<T>> BlockState copyValue(BlockState from, Property<T> property, BlockState to, Property<?> target) {
        return parseInto(to, target, property.name(from.get(property)));
    }

    private static <V extends Comparable<V>> BlockState parseInto(BlockState state, Property<V> property, String value) {
        return property.parse(value).map(parsed -> state.with(property, parsed)).orElse(state);
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
