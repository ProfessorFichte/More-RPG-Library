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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.more_rpg_classes.worldgen.ModStructureProcessorTypes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaterPillarProcessor extends StructureProcessor {

    private static final int BLOCK_UPDATE_FLAGS = Block.NOTIFY_ALL | Block.FORCE_STATE;

    public static final MapCodec<WaterPillarProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("corner_block").forGetter(p -> p.cornerBlockId),
                    Codec.STRING.fieldOf("pillar_block").forGetter(p -> p.pillarBlockId),
                    Codec.STRING.fieldOf("fence_block").forGetter(p -> p.fenceBlockId),
                    Codec.INT.optionalFieldOf("max_pillar_depth", 32).forGetter(p -> p.maxPillarDepth),
                    PathAdaptationProcessor.TerrainMapping.CODEC.listOf()
                            .optionalFieldOf("terrain_mappings", List.of())
                            .forGetter(p -> p.terrainMappings),
                    Codec.STRING.optionalFieldOf("fallback_block", "minecraft:dirt_path").forGetter(p -> p.fallbackBlockId),
                    Codec.INT.optionalFieldOf("sample_radius", 2).forGetter(p -> p.sampleRadius),
                    Codec.BOOL.optionalFieldOf("blend_with_terrain", true).forGetter(p -> p.blendWithTerrain)
            ).apply(instance, WaterPillarProcessor::new)
    );

    private final String cornerBlockId;
    private final String pillarBlockId;
    private final String fenceBlockId;
    private final int maxPillarDepth;
    private final List<PathAdaptationProcessor.TerrainMapping> terrainMappings;
    private final String fallbackBlockId;
    private final int sampleRadius;
    private final boolean blendWithTerrain;

    private final Block cornerBlock;
    private final Block pillarBlock;
    private final Block fenceBlock;
    private final Block fallbackBlock;

    public WaterPillarProcessor(
            String cornerBlockId,
            String pillarBlockId,
            String fenceBlockId,
            int maxPillarDepth,
            List<PathAdaptationProcessor.TerrainMapping> terrainMappings,
            String fallbackBlockId,
            int sampleRadius,
            boolean blendWithTerrain
    ) {
        this.cornerBlockId = cornerBlockId;
        this.pillarBlockId = pillarBlockId;
        this.fenceBlockId = fenceBlockId;
        this.maxPillarDepth = maxPillarDepth;
        this.terrainMappings = terrainMappings;
        this.fallbackBlockId = fallbackBlockId;
        this.sampleRadius = sampleRadius;
        this.blendWithTerrain = blendWithTerrain;

        this.cornerBlock = Registries.BLOCK.get(Identifier.of(cornerBlockId));
        this.pillarBlock = Registries.BLOCK.get(Identifier.of(pillarBlockId));
        this.fenceBlock = Registries.BLOCK.get(Identifier.of(fenceBlockId));
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

        if (!state.isOf(cornerBlock)) {
            return currentBlockInfo;
        }

        BlockPos currentPos = currentBlockInfo.pos();
        BlockPos belowPos = currentPos.down();
        BlockState belowState = world.getBlockState(belowPos);

        boolean hasWaterBelow = isWaterAt(belowState);

        if (!hasWaterBelow && (belowState.isAir() || belowState.isReplaceable())) {
            BlockState twoBelow = world.getBlockState(currentPos.down(2));
            hasWaterBelow = isWaterAt(twoBelow);
        }

        if (hasWaterBelow) {
            WorldAccess worldAccess = getWorldAccess(world);

            if (worldAccess != null) {
                BlockPos abovePos = currentPos.up();
                worldAccess.setBlockState(abovePos, fenceBlock.getDefaultState(), BLOCK_UPDATE_FLAGS);

                generatePillarDownFromCorner(worldAccess, currentPos);
            }

            return new StructureTemplate.StructureBlockInfo(
                    currentPos,
                    pillarBlock.getDefaultState(),
                    null
            );
        }

        for (PathAdaptationProcessor.TerrainMapping mapping : terrainMappings) {
            if (mapping.matches(belowState)) {
                return new StructureTemplate.StructureBlockInfo(
                        currentPos,
                        mapping.getOutput(),
                        currentBlockInfo.nbt()
                );
            }
        }

        if (blendWithTerrain) {
            BlockState blendedState = sampleSurroundingTerrain(world, currentPos);
            if (blendedState != null) {
                return new StructureTemplate.StructureBlockInfo(
                        currentPos,
                        blendedState,
                        currentBlockInfo.nbt()
                );
            }
        }

        return new StructureTemplate.StructureBlockInfo(
                currentPos,
                fallbackBlock.getDefaultState(),
                currentBlockInfo.nbt()
        );
    }

    @Nullable
    private WorldAccess getWorldAccess(WorldView world) {
        if (world instanceof StructureWorldAccess structureWorld) {
            return structureWorld;
        }
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld;
        }
        if (world instanceof WorldAccess worldAccess) {
            return worldAccess;
        }
        return null;
    }

    @Nullable
    private BlockState sampleSurroundingTerrain(WorldView world, BlockPos pos) {
        Map<Block, Integer> blockCounts = new HashMap<>();
        BlockState bestCandidate = null;

        BlockState belowState = world.getBlockState(pos.down());
        if (isTerrainBlock(belowState)) {
            addToCount(blockCounts, belowState.getBlock(), 3);
            bestCandidate = belowState;
        }

        for (Direction dir : Direction.Type.HORIZONTAL) {
            for (int dist = 1; dist <= sampleRadius; dist++) {
                BlockPos checkPos = pos.offset(dir, dist);

                BlockState checkState = world.getBlockState(checkPos);
                if (isTerrainBlock(checkState)) {
                    int weight = sampleRadius - dist + 1;
                    addToCount(blockCounts, checkState.getBlock(), weight);
                    if (bestCandidate == null) bestCandidate = checkState;
                }

                BlockState belowCheckState = world.getBlockState(checkPos.down());
                if (isTerrainBlock(belowCheckState)) {
                    int weight = sampleRadius - dist + 2;
                    addToCount(blockCounts, belowCheckState.getBlock(), weight);
                    if (bestCandidate == null) bestCandidate = belowCheckState;
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
            if (mostCommon == Blocks.GRASS_BLOCK || mostCommon == Blocks.DIRT) {
                return Blocks.DIRT_PATH.getDefaultState();
            }
            if (mostCommon == Blocks.SAND) {
                return Blocks.SANDSTONE.getDefaultState();
            }
            if (mostCommon == Blocks.RED_SAND) {
                return Blocks.RED_SANDSTONE.getDefaultState();
            }
            if (mostCommon == Blocks.STONE || mostCommon == Blocks.GRAVEL) {
                return Blocks.COBBLESTONE.getDefaultState();
            }
            if (mostCommon == Blocks.SNOW_BLOCK || mostCommon == Blocks.POWDER_SNOW) {
                return Blocks.PACKED_ICE.getDefaultState();
            }

            return mostCommon.getDefaultState();
        }

        return bestCandidate;
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
        if (state.isOf(Blocks.GRAVEL)) return true;
        if (state.isOf(Blocks.CLAY)) return true;
        if (state.isOf(Blocks.MOSS_BLOCK)) return true;
        if (state.isOf(Blocks.COARSE_DIRT)) return true;
        if (state.isOf(Blocks.COBBLESTONE)) return true;
        if (state.isOf(Blocks.MOSSY_COBBLESTONE)) return true;

        return false;
    }

    private boolean isWaterAt(BlockState state) {
        if (state.isOf(Blocks.WATER)) return true;
        if (state.getFluidState().isIn(FluidTags.WATER)) return true;
        return false;
    }

    private void generatePillarDownFromCorner(WorldAccess world, BlockPos cornerPos) {
        BlockPos.Mutable mutablePos = cornerPos.down().mutableCopy();

        world.setBlockState(cornerPos, pillarBlock.getDefaultState(), BLOCK_UPDATE_FLAGS);

        for (int i = 0; i < maxPillarDepth; i++) {
            BlockState currentState = world.getBlockState(mutablePos);

            boolean isWater = isWaterAt(currentState);
            boolean isAir = currentState.isAir();
            boolean isReplaceable = currentState.isReplaceable();

            if (isWater || isAir || isReplaceable) {
                world.setBlockState(mutablePos, pillarBlock.getDefaultState(), BLOCK_UPDATE_FLAGS);
                mutablePos.move(Direction.DOWN);
            } else {
                break;
            }
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.WATER_PILLAR;
    }
}
