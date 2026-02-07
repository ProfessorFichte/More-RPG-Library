package net.more_rpg_classes.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JigsawBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import net.more_rpg_classes.worldgen.ModStructureProcessorTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PathAdaptationProcessor extends StructureProcessor {
     //Helper method to detect water at a given position.
    public static boolean isWaterAt(BlockState state) {
        if (state.isOf(Blocks.WATER)) return true;
        if (state.getFluidState().isIn(FluidTags.WATER)) return true;
        return false;
    }

    public static boolean isSolidTerrain(BlockState state) {
        if (state.isAir()) return false;
        if (isWaterAt(state)) return false;
        if (state.isIn(BlockTags.REPLACEABLE)) return false;
        return state.isOpaque() || state.isIn(BlockTags.DIRT) || state.isIn(BlockTags.SAND)
                || state.isIn(BlockTags.BASE_STONE_OVERWORLD);
    }

    public record TerrainMapping(String terrain, String output) {
        public static final Codec<TerrainMapping> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("terrain").forGetter(TerrainMapping::terrain),
                        Codec.STRING.fieldOf("output").forGetter(TerrainMapping::output)
                ).apply(instance, TerrainMapping::new)
        );

        public boolean matches(BlockState state) {
            if (terrain.startsWith("#")) {
                TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, Identifier.of(terrain.substring(1)));
                return state.isIn(tag);
            }
            Block block = Registries.BLOCK.get(Identifier.of(terrain));
            return state.isOf(block);
        }

        public BlockState getOutput() {
            return Registries.BLOCK.get(Identifier.of(output)).getDefaultState();
        }
    }

    public static final MapCodec<PathAdaptationProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("filler_block").forGetter(p -> p.fillerBlockId),
                    TerrainMapping.CODEC.listOf().fieldOf("terrain_mappings").forGetter(p -> p.terrainMappings),
                    Codec.STRING.fieldOf("water_output").forGetter(p -> p.waterOutputId),
                    Codec.BOOL.optionalFieldOf("stop_on_water", true).forGetter(p -> p.stopOnWater),
                    Codec.INT.optionalFieldOf("water_check_radius", 3).forGetter(p -> p.waterCheckRadius),
                    Codec.DOUBLE.optionalFieldOf("water_threshold", 0.25).forGetter(p -> p.waterThreshold),
                    Codec.BOOL.optionalFieldOf("remove_floating_blocks", true).forGetter(p -> p.removeFloatingBlocks)
            ).apply(instance, PathAdaptationProcessor::new)
    );

    private final String fillerBlockId;
    private final List<TerrainMapping> terrainMappings;
    private final String waterOutputId;
    private final boolean stopOnWater;
    private final int waterCheckRadius;
    private final double waterThreshold;
    private final boolean removeFloatingBlocks;

    private final Block fillerBlock;
    private final Block waterOutput;

    public PathAdaptationProcessor(String fillerBlockId, List<TerrainMapping> terrainMappings,
                                   String waterOutputId, boolean stopOnWater, int waterCheckRadius,
                                   double waterThreshold, boolean removeFloatingBlocks) {
        this.fillerBlockId = fillerBlockId;
        this.terrainMappings = terrainMappings;
        this.waterOutputId = waterOutputId;
        this.stopOnWater = stopOnWater;
        this.waterCheckRadius = waterCheckRadius;
        this.waterThreshold = waterThreshold;
        this.removeFloatingBlocks = removeFloatingBlocks;

        this.fillerBlock = Registries.BLOCK.get(Identifier.of(fillerBlockId));
        this.waterOutput = Registries.BLOCK.get(Identifier.of(waterOutputId));
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
        BlockPos currentPos = currentBlockInfo.pos();

        if (state.isOf(fillerBlock)) {
            return processFillerBlock(world, currentPos, currentBlockInfo);
        }

        if (stopOnWater && state.getBlock() instanceof JigsawBlock) {
            if (hasWaterAhead(world, currentPos, state)) {
                return new StructureTemplate.StructureBlockInfo(
                        currentPos,
                        Blocks.AIR.getDefaultState(),
                        null
                );
            }
        }

        return currentBlockInfo;
    }

    private StructureTemplate.StructureBlockInfo processFillerBlock(
            WorldView world, BlockPos currentPos, StructureTemplate.StructureBlockInfo currentBlockInfo) {

        BlockPos belowPos = currentPos.down();
        BlockState belowState = world.getBlockState(belowPos);

        if (isWaterAt(belowState)) {
            return new StructureTemplate.StructureBlockInfo(
                    currentPos,
                    waterOutput.getDefaultState(),
                    currentBlockInfo.nbt()
            );
        }

        if (!isSolidTerrain(belowState)) {
            if (removeFloatingBlocks) {
                if (hasWaterNearby(world, currentPos, 2)) {
                    return null;
                }
                boolean foundGround = false;
                for (int y = 1; y <= 2; y++) {
                    BlockState checkState = world.getBlockState(currentPos.down(y));
                    if (isSolidTerrain(checkState)) {
                        foundGround = true;
                        break;
                    }
                    if (isWaterAt(checkState)) {
                        return new StructureTemplate.StructureBlockInfo(
                                currentPos,
                                waterOutput.getDefaultState(),
                                currentBlockInfo.nbt()
                        );
                    }
                }
                if (!foundGround) {
                    return null;
                }
            }
        }

        for (TerrainMapping mapping : terrainMappings) {
            if (mapping.matches(belowState)) {
                return new StructureTemplate.StructureBlockInfo(
                        currentPos,
                        mapping.getOutput(),
                        currentBlockInfo.nbt()
                );
            }
        }

        return new StructureTemplate.StructureBlockInfo(
                currentPos,
                Blocks.DIRT_PATH.getDefaultState(),
                currentBlockInfo.nbt()
        );
    }

    private boolean hasWaterNearby(WorldView world, BlockPos pos, int radius) {
        for (Direction dir : Direction.Type.HORIZONTAL) {
            for (int dist = 1; dist <= radius; dist++) {
                BlockPos checkPos = pos.offset(dir, dist);
                for (int y = 0; y >= -1; y--) {
                    BlockState checkState = world.getBlockState(checkPos.up(y));
                    if (isWaterAt(checkState)) {
                        return true;
                    }
                }
            }
        }
        BlockState belowState = world.getBlockState(pos.down());
        return isWaterAt(belowState);
    }

    private boolean hasWaterAhead(WorldView world, BlockPos pos, BlockState jigsawState) {
        int waterCount = 0;
        int totalChecks = 0;
        Direction facing = JigsawBlock.getFacing(jigsawState);

        for (int dist = 1; dist <= waterCheckRadius; dist++) {
            BlockPos checkPos = pos.offset(facing, dist);
            for (int yOffset = -1; yOffset <= 0; yOffset++) {
                BlockState checkState = world.getBlockState(checkPos.up(yOffset));
                totalChecks++;
                if (isWaterAt(checkState)) {
                    waterCount++;
                }
            }
        }

        int sideRadius = Math.max(1, waterCheckRadius / 2);
        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (dir == facing || dir == facing.getOpposite()) continue;
            for (int dist = 1; dist <= sideRadius; dist++) {
                BlockPos checkPos = pos.offset(dir, dist).down();
                BlockState checkState = world.getBlockState(checkPos);
                totalChecks++;
                if (isWaterAt(checkState)) {
                    waterCount++;
                }
            }
        }

        BlockState belowState = world.getBlockState(pos.down());
        totalChecks++;
        if (isWaterAt(belowState)) {
            waterCount++;
        }

        double waterRatio = (double) waterCount / totalChecks;
        return waterRatio >= waterThreshold;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.PATH_ADAPTATION;
    }
}
