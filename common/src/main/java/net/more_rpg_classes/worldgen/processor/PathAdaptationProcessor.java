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
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.more_rpg_classes.worldgen.ModStructureProcessorTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PathAdaptationProcessor extends StructureProcessor {
     //Helper method to detect water at a given position.
     //Checks for water blocks AND waterlogged blocks (blocks containing water fluid).
    private static boolean isWaterAt(BlockState state) {
        if (state.isOf(Blocks.WATER)) return true;
        if (state.getFluidState().isIn(FluidTags.WATER)) return true;
        return false;
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
                    Codec.BOOL.optionalFieldOf("stop_on_water", true).forGetter(p -> p.stopOnWater)
            ).apply(instance, PathAdaptationProcessor::new)
    );

    private final String fillerBlockId;
    private final List<TerrainMapping> terrainMappings;
    private final String waterOutputId;
    private final boolean stopOnWater;

    private final Block fillerBlock;
    private final Block waterOutput;

    public PathAdaptationProcessor(String fillerBlockId, List<TerrainMapping> terrainMappings, String waterOutputId, boolean stopOnWater) {
        this.fillerBlockId = fillerBlockId;
        this.terrainMappings = terrainMappings;
        this.waterOutputId = waterOutputId;
        this.stopOnWater = stopOnWater;

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

        if (state.isOf(fillerBlock)) {
            BlockPos belowPos = currentBlockInfo.pos().down();
            BlockState belowState = world.getBlockState(belowPos);

            if (isWaterAt(belowState)) {
                return new StructureTemplate.StructureBlockInfo(
                        currentBlockInfo.pos(),
                        waterOutput.getDefaultState(),
                        currentBlockInfo.nbt()
                );
            }

            for (TerrainMapping mapping : terrainMappings) {
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

        // If stopOnWater is enabled, cancel jigsaw blocks that would continue the path over water
        // This prevents paths from generating floating bridges over rivers/lakes
        if (stopOnWater && state.getBlock() instanceof JigsawBlock) {
            BlockPos belowPos = currentBlockInfo.pos().down();
            BlockState belowState = world.getBlockState(belowPos);

            if (isWaterAt(belowState)) {
                return new StructureTemplate.StructureBlockInfo(
                        currentBlockInfo.pos(),
                        Blocks.AIR.getDefaultState(),
                        null
                );
            }
        }

        return currentBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.PATH_ADAPTATION;
    }
}
