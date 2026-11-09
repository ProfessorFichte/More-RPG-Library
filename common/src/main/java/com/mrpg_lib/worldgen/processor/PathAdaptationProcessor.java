package com.mrpg_lib.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrpg_lib.worldgen.ModStructureProcessorTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PathAdaptationProcessor extends StructureProcessor {
    private static final String NAME = "mrpg_lib:path_adaptation";

    public static boolean isWaterAt(BlockState state) {
        return TerrainSampler.isWater(state);
    }

    public static boolean isSolidTerrain(BlockState state) {
        if (state.isAir()) return false;
        if (isWaterAt(state)) return false;
        if (state.isIn(BlockTags.REPLACEABLE)) return false;
        return state.isOpaque() || state.isIn(BlockTags.DIRT) || state.isIn(BlockTags.SAND)
                || state.isIn(BlockTags.BASE_STONE_OVERWORLD);
    }

    public static final class TerrainMapping {
        public static final Codec<TerrainMapping> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("terrain").forGetter(TerrainMapping::terrain),
                        Codec.STRING.fieldOf("output").forGetter(TerrainMapping::output)
                ).apply(instance, TerrainMapping::new)
        );

        private final String terrain;
        private final String output;
        private final ProcessorBlocks.Matcher matcher;
        @Nullable
        private final BlockState outputState;

        public TerrainMapping(String terrain, String output) {
            this.terrain = terrain;
            this.output = output;
            this.matcher = ProcessorBlocks.Matcher.parse(terrain, NAME, "terrain_mappings.terrain");
            this.outputState = ProcessorBlocks.resolve(output, NAME, "terrain_mappings.output").map(Block::getDefaultState).orElse(null);
        }

        public String terrain() {
            return terrain;
        }

        public String output() {
            return output;
        }

        public boolean matches(BlockState state) {
            return outputState != null && matcher.matches(state);
        }

        public BlockState getOutput() {
            return outputState;
        }

        @Nullable
        public static BlockState map(List<TerrainMapping> mappings, BlockState terrain) {
            for (TerrainMapping mapping : mappings) {
                if (mapping.matches(terrain)) return mapping.getOutput();
            }
            return null;
        }
    }

    public static final MapCodec<PathAdaptationProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("filler_block").forGetter(p -> p.fillerBlockId),
                    TerrainMapping.CODEC.listOf().fieldOf("terrain_mappings").forGetter(p -> p.terrainMappings),
                    Codec.STRING.fieldOf("water_output").forGetter(p -> p.waterOutputId),
                    Codec.BOOL.optionalFieldOf("stop_on_water", false).forGetter(p -> p.stopOnWater),
                    Codec.INT.optionalFieldOf("water_check_radius", 3).forGetter(p -> p.waterCheckRadius),
                    Codec.DOUBLE.optionalFieldOf("water_threshold", 0.25).forGetter(p -> p.waterThreshold),
                    Codec.BOOL.optionalFieldOf("remove_floating_blocks", true).forGetter(p -> p.removeFloatingBlocks),
                    Codec.STRING.optionalFieldOf("fallback_block", "minecraft:dirt_path").forGetter(p -> p.fallbackBlockId)
            ).apply(instance, PathAdaptationProcessor::new)
    );

    private final String fillerBlockId;
    private final List<TerrainMapping> terrainMappings;
    private final String waterOutputId;
    private final boolean stopOnWater;
    private final int waterCheckRadius;
    private final double waterThreshold;
    private final boolean removeFloatingBlocks;
    private final String fallbackBlockId;

    private final Optional<Block> fillerBlock;
    private final BlockState fallbackState;
    private final BlockState waterOutputState;

    // stop_on_water, water_check_radius and water_threshold are only kept so older processor lists still load.
    // A processor can't stop a jigsaw path, the pieces are already chosen before any block gets placed.
    public PathAdaptationProcessor(String fillerBlockId, List<TerrainMapping> terrainMappings,
                                   String waterOutputId, boolean stopOnWater, int waterCheckRadius,
                                   double waterThreshold, boolean removeFloatingBlocks, String fallbackBlockId) {
        this.fillerBlockId = fillerBlockId;
        this.terrainMappings = terrainMappings;
        this.waterOutputId = waterOutputId;
        this.stopOnWater = stopOnWater;
        this.waterCheckRadius = waterCheckRadius;
        this.waterThreshold = waterThreshold;
        this.removeFloatingBlocks = removeFloatingBlocks;
        this.fallbackBlockId = fallbackBlockId;

        this.fillerBlock = ProcessorBlocks.resolve(fillerBlockId, NAME, "filler_block");
        this.fallbackState = ProcessorBlocks.resolve(fallbackBlockId, NAME, "fallback_block")
                .orElse(Blocks.DIRT_PATH).getDefaultState();
        this.waterOutputState = ProcessorBlocks.resolve(waterOutputId, NAME, "water_output")
                .map(Block::getDefaultState).orElse(fallbackState);
    }

    @Override
    public List<StructureBlockInfo> reprocess(ServerWorldAccess world, BlockPos pos, BlockPos pivot,
                                              List<StructureBlockInfo> originalBlockInfos,
                                              List<StructureBlockInfo> currentBlockInfos,
                                              StructurePlacementData data) {
        if (fillerBlock.isEmpty()) return currentBlockInfos;
        List<StructureBlockInfo> result = new ArrayList<>(currentBlockInfos.size());
        for (StructureBlockInfo info : currentBlockInfos) {
            if (!info.state().isOf(fillerBlock.get()) || !ProcessorBlocks.inChunk(data, info.pos())) {
                result.add(info);
                continue;
            }
            BlockState state = adapt(world, info.pos());
            if (state != null) result.add(new StructureBlockInfo(info.pos(), state, info.nbt()));
        }
        return result;
    }

    @Nullable
    private BlockState adapt(WorldView world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.down());
        if (isWaterAt(below)) return waterOutputState;

        if (!isSolidTerrain(below) && removeFloatingBlocks) {
            if (hasWaterNearby(world, pos)) return null;
            boolean foundGround = false;
            for (int y = 1; y <= 2; y++) {
                BlockState check = world.getBlockState(pos.down(y));
                if (isSolidTerrain(check)) {
                    foundGround = true;
                    break;
                }
                if (isWaterAt(check)) return waterOutputState;
            }
            if (!foundGround) return null;
        }

        BlockState mapped = TerrainMapping.map(terrainMappings, below);
        return mapped != null ? mapped : fallbackState;
    }

    private static boolean hasWaterNearby(WorldView world, BlockPos pos) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            for (int distance = 1; distance <= 2; distance++) {
                BlockPos check = pos.offset(direction, distance);
                if (isWaterAt(world.getBlockState(check)) || isWaterAt(world.getBlockState(check.down()))) return true;
            }
        }
        return isWaterAt(world.getBlockState(pos.down()));
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.PATH_ADAPTATION;
    }
}
