package net.more_rpg_classes.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.more_rpg_classes.worldgen.ModStructureProcessorTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaterPillarProcessor extends StructureProcessor {
    // Use minimal block update flags to avoid interfering with world generation
    private static final int BLOCK_UPDATE_FLAGS = Block.NOTIFY_LISTENERS | Block.FORCE_STATE;

    public static final MapCodec<WaterPillarProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("corner_block").forGetter(p -> p.cornerBlockId),
                    Codec.STRING.fieldOf("pillar_block").forGetter(p -> p.pillarBlockId),
                    Codec.STRING.fieldOf("fence_block").forGetter(p -> p.fenceBlockId),
                    Codec.INT.optionalFieldOf("max_pillar_depth", 32).forGetter(p -> p.maxPillarDepth),
                    PathAdaptationProcessor.TerrainMapping.CODEC.listOf()
                            .optionalFieldOf("terrain_mappings", List.of())
                            .forGetter(p -> p.terrainMappings)
            ).apply(instance, WaterPillarProcessor::new)
    );

    private final String cornerBlockId;
    private final String pillarBlockId;
    private final String fenceBlockId;
    private final int maxPillarDepth;
    private final List<PathAdaptationProcessor.TerrainMapping> terrainMappings;

    private final Block cornerBlock;
    private final Block pillarBlock;
    private final Block fenceBlock;

    public WaterPillarProcessor(
            String cornerBlockId,
            String pillarBlockId,
            String fenceBlockId,
            int maxPillarDepth,
            List<PathAdaptationProcessor.TerrainMapping> terrainMappings
    ) {
        this.cornerBlockId = cornerBlockId;
        this.pillarBlockId = pillarBlockId;
        this.fenceBlockId = fenceBlockId;
        this.maxPillarDepth = maxPillarDepth;
        this.terrainMappings = terrainMappings;

        this.cornerBlock = Registries.BLOCK.get(Identifier.of(cornerBlockId));
        this.pillarBlock = Registries.BLOCK.get(Identifier.of(pillarBlockId));
        this.fenceBlock = Registries.BLOCK.get(Identifier.of(fenceBlockId));
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

        BlockPos checkPos = currentBlockInfo.pos().down();
        BlockState belowState = world.getBlockState(checkPos);

        boolean hasWaterBelow = belowState.isOf(Blocks.WATER) || belowState.getFluidState().isIn(FluidTags.WATER);

        if (hasWaterBelow) {
            if (world instanceof WorldAccess worldAccess) {
                generatePillarDown(worldAccess, checkPos);
            }
            return new StructureTemplate.StructureBlockInfo(
                    currentBlockInfo.pos(),
                    fenceBlock.getDefaultState(),
                    null
            );
        }

        for (PathAdaptationProcessor.TerrainMapping mapping : terrainMappings) {
            if (mapping.matches(belowState)) {
                return new StructureTemplate.StructureBlockInfo(
                        currentBlockInfo.pos(),
                        mapping.getOutput(),
                        currentBlockInfo.nbt()
                );
            }
        }

        return new StructureTemplate.StructureBlockInfo(
                currentBlockInfo.pos(),
                Blocks.DIRT_PATH.getDefaultState(),
                currentBlockInfo.nbt()
        );
    }

     //Generates a pillar downward through water/air until it hits solid ground.
    private void generatePillarDown(WorldAccess world, BlockPos startPos) {
        BlockPos.Mutable mutablePos = startPos.mutableCopy();

        for (int i = 0; i < maxPillarDepth; i++) {
            BlockState currentState = world.getBlockState(mutablePos);
            boolean isWater = currentState.isOf(Blocks.WATER) || currentState.getFluidState().isIn(FluidTags.WATER);
            if (isWater || currentState.isOf(Blocks.AIR) || currentState.isReplaceable()) {
                world.setBlockState(mutablePos, pillarBlock.getDefaultState(), BLOCK_UPDATE_FLAGS);
                mutablePos.move(0, -1, 0);
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
