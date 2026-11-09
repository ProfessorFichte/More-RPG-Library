package com.mrpg_lib.worldgen.processor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TerrainSampler {
    private static final List<String> TERRAIN_WORDS = List.of("dirt", "soil", "grass", "stone", "sand", "gravel", "clay", "mud", "podzol", "mycelium");

    private TerrainSampler() {
    }

    public static boolean isWater(BlockState state) {
        return state.isOf(Blocks.WATER) || state.getFluidState().isIn(FluidTags.WATER);
    }

    public static boolean isTerrain(BlockState state) {
        if (state.isAir() || isWater(state) || state.hasBlockEntity()) return false;
        if (!state.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) return false;
        if (state.isIn(BlockTags.DIRT) || state.isIn(BlockTags.SAND) || state.isIn(BlockTags.BASE_STONE_OVERWORLD)
                || state.isIn(BlockTags.TERRACOTTA) || state.isIn(BlockTags.SNOW)) {
            return true;
        }
        if (state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT_PATH) || state.isOf(Blocks.PODZOL) || state.isOf(Blocks.MYCELIUM)
                || state.isOf(Blocks.MUD) || state.isOf(Blocks.MUDDY_MANGROVE_ROOTS) || state.isOf(Blocks.GRAVEL) || state.isOf(Blocks.CLAY)
                || state.isOf(Blocks.MOSS_BLOCK) || state.isOf(Blocks.ROOTED_DIRT) || state.isOf(Blocks.COARSE_DIRT) || state.isOf(Blocks.FARMLAND)
                || state.isOf(Blocks.COBBLESTONE) || state.isOf(Blocks.MOSSY_COBBLESTONE) || state.isOf(Blocks.DEEPSLATE)
                || state.isOf(Blocks.CALCITE) || state.isOf(Blocks.TUFF) || state.isOf(Blocks.DRIPSTONE_BLOCK)) {
            return true;
        }
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        if (path.contains("brick") || path.contains("ore")) return false;
        for (String word : TERRAIN_WORDS) {
            if (path.contains(word)) return true;
        }
        return false;
    }

    @Nullable
    public static BlockState mostCommon(WorldView world, BlockPos pos, int radius, boolean below, boolean horizontal, Set<BlockPos> structureBlocks) {
        Map<Block, Integer> counts = new HashMap<>();
        Map<Block, BlockState> firstSeen = new HashMap<>();
        if (below) {
            sample(world, pos.down(), 3, counts, firstSeen, structureBlocks);
        }
        if (horizontal) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                for (int distance = 1; distance <= radius; distance++) {
                    BlockPos check = pos.offset(direction, distance);
                    int weight = radius - distance + 1;
                    sample(world, check, weight, counts, firstSeen, structureBlocks);
                    sample(world, check.down(), weight, counts, firstSeen, structureBlocks);
                }
            }
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                int distance = Math.max(Math.abs(dx), Math.abs(dz));
                sample(world, pos.add(dx, -1, dz), Math.max(1, radius - distance + 1), counts, firstSeen, structureBlocks);
            }
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> firstSeen.get(entry.getKey()))
                .orElse(null);
    }

    private static void sample(WorldView world, BlockPos pos, int weight, Map<Block, Integer> counts, Map<Block, BlockState> firstSeen,
                               Set<BlockPos> structureBlocks) {
        if (structureBlocks.contains(pos)) return;
        BlockState state = world.getBlockState(pos);
        if (!isTerrain(state)) return;
        counts.merge(state.getBlock(), weight, Integer::sum);
        firstSeen.putIfAbsent(state.getBlock(), state);
    }
}
