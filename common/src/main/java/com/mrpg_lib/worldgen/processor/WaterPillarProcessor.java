package com.mrpg_lib.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrpg_lib.worldgen.ModStructureProcessorTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WaterPillarProcessor extends StructureProcessor {
    private static final String NAME = "mrpg_lib:water_pillar";

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

    private final Optional<Block> cornerBlock;
    private final Optional<Block> pillarBlock;
    private final Optional<Block> fenceBlock;
    private final BlockState fallbackState;

    public WaterPillarProcessor(String cornerBlockId, String pillarBlockId, String fenceBlockId, int maxPillarDepth,
                                List<PathAdaptationProcessor.TerrainMapping> terrainMappings, String fallbackBlockId,
                                int sampleRadius, boolean blendWithTerrain) {
        this.cornerBlockId = cornerBlockId;
        this.pillarBlockId = pillarBlockId;
        this.fenceBlockId = fenceBlockId;
        this.maxPillarDepth = maxPillarDepth;
        this.terrainMappings = terrainMappings;
        this.fallbackBlockId = fallbackBlockId;
        this.sampleRadius = sampleRadius;
        this.blendWithTerrain = blendWithTerrain;

        this.cornerBlock = ProcessorBlocks.resolve(cornerBlockId, NAME, "corner_block");
        this.pillarBlock = ProcessorBlocks.resolve(pillarBlockId, NAME, "pillar_block");
        this.fenceBlock = ProcessorBlocks.resolve(fenceBlockId, NAME, "fence_block");
        this.fallbackState = ProcessorBlocks.resolve(fallbackBlockId, NAME, "fallback_block")
                .orElse(Blocks.DIRT_PATH).getDefaultState();
    }

    @Override
    public List<StructureBlockInfo> reprocess(ServerWorldAccess world, BlockPos pos, BlockPos pivot,
                                              List<StructureBlockInfo> originalBlockInfos,
                                              List<StructureBlockInfo> currentBlockInfos,
                                              StructurePlacementData data) {
        if (cornerBlock.isEmpty()) return currentBlockInfos;
        List<StructureBlockInfo> result = new ArrayList<>(currentBlockInfos);
        Map<BlockPos, Integer> indexByPos = new HashMap<>();
        for (int i = 0; i < result.size(); i++) indexByPos.put(result.get(i).pos(), i);

        for (int i = 0; i < result.size(); i++) {
            StructureBlockInfo info = result.get(i);
            if (!info.state().isOf(cornerBlock.get()) || !ProcessorBlocks.inChunk(data, info.pos())) continue;
            BlockPos corner = info.pos();
            if (pillarBlock.isPresent() && hasWaterBelow(world, corner)) {
                result.set(i, new StructureBlockInfo(corner, pillarBlock.get().getDefaultState(), null));
                placeFence(world, corner.up(), result, indexByPos);
                buildPillar(world, corner.down(), indexByPos);
            } else {
                result.set(i, new StructureBlockInfo(corner, groundState(world, corner, indexByPos), info.nbt()));
            }
        }
        return result;
    }

    private static boolean hasWaterBelow(ServerWorldAccess world, BlockPos corner) {
        BlockState below = world.getBlockState(corner.down());
        if (TerrainSampler.isWater(below)) return true;
        return (below.isAir() || below.isReplaceable()) && TerrainSampler.isWater(world.getBlockState(corner.down(2)));
    }

    private void placeFence(ServerWorldAccess world, BlockPos above, List<StructureBlockInfo> result, Map<BlockPos, Integer> indexByPos) {
        if (fenceBlock.isEmpty()) return;
        BlockState fence = fenceBlock.get().getDefaultState();
        Integer index = indexByPos.get(above);
        if (index == null) {
            world.setBlockState(above, fence, Block.NOTIFY_LISTENERS);
        } else if (result.get(index).state().isAir()) {
            result.set(index, new StructureBlockInfo(above, fence, null));
        }
    }

    private void buildPillar(ServerWorldAccess world, BlockPos start, Map<BlockPos, Integer> indexByPos) {
        BlockState pillar = pillarBlock.orElseThrow().getDefaultState();
        BlockPos.Mutable current = start.mutableCopy();
        for (int i = 0; i < maxPillarDepth; i++) {
            if (indexByPos.containsKey(current)) break;
            BlockState state = world.getBlockState(current);
            if (!TerrainSampler.isWater(state) && !state.isAir() && !state.isReplaceable()) break;
            world.setBlockState(current, pillar, Block.NOTIFY_LISTENERS);
            current.move(0, -1, 0);
        }
    }

    private BlockState groundState(ServerWorldAccess world, BlockPos corner, Map<BlockPos, Integer> indexByPos) {
        BlockState mapped = PathAdaptationProcessor.TerrainMapping.map(terrainMappings, world.getBlockState(corner.down()));
        if (mapped != null) return mapped;
        if (blendWithTerrain) {
            BlockState blended = pathLike(TerrainSampler.mostCommon(world, corner, sampleRadius, true, true, indexByPos.keySet()));
            if (blended != null) return blended;
        }
        return fallbackState;
    }

    @Nullable
    private static BlockState pathLike(@Nullable BlockState terrain) {
        if (terrain == null) return null;
        Block block = terrain.getBlock();
        if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT) return Blocks.DIRT_PATH.getDefaultState();
        if (block == Blocks.SAND) return Blocks.SANDSTONE.getDefaultState();
        if (block == Blocks.RED_SAND) return Blocks.RED_SANDSTONE.getDefaultState();
        if (block == Blocks.STONE || block == Blocks.GRAVEL) return Blocks.COBBLESTONE.getDefaultState();
        if (block == Blocks.SNOW_BLOCK || block == Blocks.POWDER_SNOW) return Blocks.PACKED_ICE.getDefaultState();
        return block.getDefaultState();
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.WATER_PILLAR;
    }
}
