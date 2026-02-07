package net.more_rpg_classes.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.more_rpg_classes.worldgen.ModStructureProcessorTypes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TerrainBlendingProcessor extends StructureProcessor {
    //It samples neighboring blocks and picks an appropriate block that fits the environment.

    public static final MapCodec<TerrainBlendingProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("blend_block").forGetter(p -> p.blendBlockId),
                    Codec.INT.optionalFieldOf("sample_radius", 2).forGetter(p -> p.sampleRadius),
                    Codec.STRING.optionalFieldOf("fallback_block", "minecraft:grass_block").forGetter(p -> p.fallbackBlockId),
                    Codec.BOOL.optionalFieldOf("match_below", true).forGetter(p -> p.matchBelow),
                    Codec.BOOL.optionalFieldOf("match_horizontal", true).forGetter(p -> p.matchHorizontal),
                    Codec.DOUBLE.optionalFieldOf("variation_chance", 0.15).forGetter(p -> p.variationChance)
            ).apply(instance, TerrainBlendingProcessor::new)
    );

    private final String blendBlockId;
    private final int sampleRadius;
    private final String fallbackBlockId;
    private final boolean matchBelow;
    private final boolean matchHorizontal;
    private final double variationChance;

    private final Block blendBlock;
    private final Block fallbackBlock;

    public TerrainBlendingProcessor(
            String blendBlockId,
            int sampleRadius,
            String fallbackBlockId,
            boolean matchBelow,
            boolean matchHorizontal,
            double variationChance
    ) {
        this.blendBlockId = blendBlockId;
        this.sampleRadius = sampleRadius;
        this.fallbackBlockId = fallbackBlockId;
        this.matchBelow = matchBelow;
        this.matchHorizontal = matchHorizontal;
        this.variationChance = variationChance;

        this.blendBlock = Registries.BLOCK.get(Identifier.of(blendBlockId));
        this.fallbackBlock = Registries.BLOCK.get(Identifier.of(fallbackBlockId));
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(
            WorldView world,
            BlockPos pos,
            BlockPos pivot,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            StructurePlacementData placementData
    ) {
        BlockState state = currentBlockInfo.state();

        if (!state.isOf(blendBlock)) {
            return currentBlockInfo;
        }

        BlockPos currentPos = currentBlockInfo.pos();

        BlockState belowState = world.getBlockState(currentPos.down());
        if (isWaterAt(belowState)) {
            boolean hasGround = false;
            for (int y = 2; y <= 3; y++) {
                BlockState checkState = world.getBlockState(currentPos.down(y));
                if (!checkState.isAir() && !isWaterAt(checkState) && checkState.isOpaque()) {
                    hasGround = true;
                    break;
                }
            }
            if (!hasGround) {
                return null;
            }
        }

        BlockState bestMatch = sampleSurroundingTerrain(world, currentPos);

        Random random = Random.create(currentPos.asLong());
        if (random.nextDouble() < variationChance) {
            bestMatch = getVariation(bestMatch, random);
        }

        return new StructureTemplate.StructureBlockInfo(
                currentPos,
                bestMatch,
                null
        );
    }

    private BlockState sampleSurroundingTerrain(WorldView world, BlockPos pos) {
        Map<Block, Integer> blockCounts = new HashMap<>();
        List<BlockState> candidates = new ArrayList<>();

        if (matchBelow) {
            BlockState belowState = world.getBlockState(pos.down());
            if (isTerrainBlock(belowState)) {
                addToCount(blockCounts, belowState.getBlock(), 3);
                candidates.add(belowState);
            }
        }

        if (matchHorizontal) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                for (int dist = 1; dist <= sampleRadius; dist++) {
                    BlockPos checkPos = pos.offset(dir, dist);
                    BlockState checkState = world.getBlockState(checkPos);

                    int weight = sampleRadius - dist + 1;

                    if (isTerrainBlock(checkState)) {
                        addToCount(blockCounts, checkState.getBlock(), weight);
                        candidates.add(checkState);
                    }

                    BlockState belowCheckState = world.getBlockState(checkPos.down());
                    if (isTerrainBlock(belowCheckState)) {
                        addToCount(blockCounts, belowCheckState.getBlock(), weight);
                        candidates.add(belowCheckState);
                    }
                }
            }
        }

        for (int dx = -sampleRadius; dx <= sampleRadius; dx++) {
            for (int dz = -sampleRadius; dz <= sampleRadius; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (Math.abs(dx) + Math.abs(dz) > sampleRadius * 2) continue;

                BlockPos checkPos = pos.add(dx, -1, dz);
                BlockState checkState = world.getBlockState(checkPos);

                if (isTerrainBlock(checkState)) {
                    int dist = Math.max(Math.abs(dx), Math.abs(dz));
                    int weight = sampleRadius - dist + 1;
                    addToCount(blockCounts, checkState.getBlock(), Math.max(1, weight));
                    candidates.add(checkState);
                }
            }
        }

        Block mostCommon = null;
        int highestCount = 0;
        for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
            if (entry.getValue() > highestCount) {
                highestCount = entry.getValue();
                mostCommon = entry.getKey();
            }
        }

        if (mostCommon != null) {
            if (mostCommon == Blocks.DIRT || mostCommon == Blocks.ROOTED_DIRT) {
                return Blocks.GRASS_BLOCK.getDefaultState();
            }

            if (mostCommon == Blocks.COARSE_DIRT) {
                return Blocks.GRASS_BLOCK.getDefaultState();
            }

            for (BlockState candidate : candidates) {
                if (candidate.getBlock() == mostCommon) {
                    return candidate;
                }
            }
            return mostCommon.getDefaultState();
        }

        return fallbackBlock.getDefaultState();
    }

    private void addToCount(Map<Block, Integer> counts, Block block, int weight) {
        counts.merge(block, weight, Integer::sum);
    }

    private boolean isTerrainBlock(BlockState state) {
        if (state.isAir()) return false;
        if (isWaterAt(state)) return false;

        if (state.isIn(BlockTags.DIRT)) return true;
        if (state.isIn(BlockTags.SAND)) return true;
        if (state.isIn(BlockTags.BASE_STONE_OVERWORLD)) return true;
        if (state.isIn(BlockTags.TERRACOTTA)) return true;
        if (state.isIn(BlockTags.SNOW)) return true;

        if (state.isOf(Blocks.GRASS_BLOCK)) return true;
        if (state.isOf(Blocks.DIRT_PATH)) return true;
        if (state.isOf(Blocks.PODZOL)) return true;
        if (state.isOf(Blocks.MYCELIUM)) return true;
        if (state.isOf(Blocks.MUD)) return true;
        if (state.isOf(Blocks.MUDDY_MANGROVE_ROOTS)) return true;
        if (state.isOf(Blocks.GRAVEL)) return true;
        if (state.isOf(Blocks.CLAY)) return true;
        if (state.isOf(Blocks.MOSS_BLOCK)) return true;
        if (state.isOf(Blocks.ROOTED_DIRT)) return true;
        if (state.isOf(Blocks.COARSE_DIRT)) return true;
        if (state.isOf(Blocks.FARMLAND)) return true;

        if (state.isOf(Blocks.COBBLESTONE)) return true;
        if (state.isOf(Blocks.MOSSY_COBBLESTONE)) return true;
        if (state.isOf(Blocks.DEEPSLATE)) return true;
        if (state.isOf(Blocks.CALCITE)) return true;
        if (state.isOf(Blocks.TUFF)) return true;
        if (state.isOf(Blocks.DRIPSTONE_BLOCK)) return true;

        String blockId = Registries.BLOCK.getId(state.getBlock()).toString().toLowerCase();
        if (blockId.contains("dirt") || blockId.contains("soil") || blockId.contains("grass") ||
            blockId.contains("stone") || blockId.contains("sand") || blockId.contains("gravel") ||
            blockId.contains("clay") || blockId.contains("mud") || blockId.contains("podzol") ||
            blockId.contains("mycelium")) {
            return true;
        }

        return false;
    }

    private BlockState getVariation(BlockState original, Random random) {
        Block block = original.getBlock();

        if (block == Blocks.GRASS_BLOCK) {
            float f = random.nextFloat();
            if (f < 0.05) return Blocks.MOSS_BLOCK.getDefaultState();
            if (f < 0.08) return Blocks.PODZOL.getDefaultState();
        }

        if (block == Blocks.DIRT) {
            return Blocks.GRASS_BLOCK.getDefaultState();
        }

        if (block == Blocks.STONE) {
            float f = random.nextFloat();
            if (f < 0.15) return Blocks.COBBLESTONE.getDefaultState();
            if (f < 0.25) return Blocks.MOSSY_COBBLESTONE.getDefaultState();
            if (f < 0.35) return Blocks.ANDESITE.getDefaultState();
        }

        if (block == Blocks.SAND) {
            float f = random.nextFloat();
            if (f < 0.1) return Blocks.SANDSTONE.getDefaultState();
            if (f < 0.2) return Blocks.GRAVEL.getDefaultState();
        }

        if (block == Blocks.SNOW_BLOCK) {
            float f = random.nextFloat();
            if (f < 0.15) return Blocks.POWDER_SNOW.getDefaultState();
            if (f < 0.25) return Blocks.ICE.getDefaultState();
        }

        return original;
    }

    private boolean isWaterAt(BlockState state) {
        if (state.isOf(Blocks.WATER)) return true;
        if (state.getFluidState().isIn(FluidTags.WATER)) return true;
        return false;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.TERRAIN_BLENDING;
    }
}
