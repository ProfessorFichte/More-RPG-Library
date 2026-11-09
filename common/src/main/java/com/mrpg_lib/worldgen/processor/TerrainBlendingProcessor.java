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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TerrainBlendingProcessor extends StructureProcessor {
    private static final String NAME = "mrpg_lib:terrain_blending";

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

    private final Optional<Block> blendBlock;
    private final BlockState fallbackState;

    public TerrainBlendingProcessor(String blendBlockId, int sampleRadius, String fallbackBlockId,
                                    boolean matchBelow, boolean matchHorizontal, double variationChance) {
        this.blendBlockId = blendBlockId;
        this.sampleRadius = sampleRadius;
        this.fallbackBlockId = fallbackBlockId;
        this.matchBelow = matchBelow;
        this.matchHorizontal = matchHorizontal;
        this.variationChance = variationChance;

        this.blendBlock = ProcessorBlocks.resolve(blendBlockId, NAME, "blend_block");
        this.fallbackState = ProcessorBlocks.resolve(fallbackBlockId, NAME, "fallback_block")
                .orElse(Blocks.GRASS_BLOCK).getDefaultState();
    }

    @Override
    public List<StructureBlockInfo> reprocess(ServerWorldAccess world, BlockPos pos, BlockPos pivot,
                                              List<StructureBlockInfo> originalBlockInfos,
                                              List<StructureBlockInfo> currentBlockInfos,
                                              StructurePlacementData data) {
        if (blendBlock.isEmpty()) return currentBlockInfos;
        Set<BlockPos> structureBlocks = new HashSet<>();
        for (StructureBlockInfo info : currentBlockInfos) structureBlocks.add(info.pos());

        List<StructureBlockInfo> result = new ArrayList<>(currentBlockInfos.size());
        for (StructureBlockInfo info : currentBlockInfos) {
            if (!info.state().isOf(blendBlock.get()) || !ProcessorBlocks.inChunk(data, info.pos())) {
                result.add(info);
                continue;
            }
            BlockState state = blend(world, info.pos(), structureBlocks);
            if (state != null) result.add(new StructureBlockInfo(info.pos(), state, null));
        }
        return result;
    }

    @Nullable
    private BlockState blend(ServerWorldAccess world, BlockPos pos, Set<BlockPos> structureBlocks) {
        if (TerrainSampler.isWater(world.getBlockState(pos.down())) && !hasGroundUnderWater(world, pos)) return null;

        BlockState sampled = TerrainSampler.mostCommon(world, pos, sampleRadius, matchBelow, matchHorizontal, structureBlocks);
        BlockState state = sampled == null ? fallbackState : grassOnTop(sampled);

        Random random = Random.create(pos.asLong());
        return random.nextDouble() < variationChance ? variation(state, random) : state;
    }

    private static boolean hasGroundUnderWater(ServerWorldAccess world, BlockPos pos) {
        for (int y = 2; y <= 3; y++) {
            BlockState state = world.getBlockState(pos.down(y));
            if (!state.isAir() && !TerrainSampler.isWater(state) && state.isOpaque()) return true;
        }
        return false;
    }

    private static BlockState grassOnTop(BlockState state) {
        if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.ROOTED_DIRT) || state.isOf(Blocks.COARSE_DIRT)) {
            return Blocks.GRASS_BLOCK.getDefaultState();
        }
        return state;
    }

    private static BlockState variation(BlockState original, Random random) {
        float roll = random.nextFloat();
        if (original.isOf(Blocks.GRASS_BLOCK)) {
            if (roll < 0.05F) return Blocks.MOSS_BLOCK.getDefaultState();
            if (roll < 0.08F) return Blocks.PODZOL.getDefaultState();
        } else if (original.isOf(Blocks.STONE)) {
            if (roll < 0.15F) return Blocks.COBBLESTONE.getDefaultState();
            if (roll < 0.25F) return Blocks.MOSSY_COBBLESTONE.getDefaultState();
            if (roll < 0.35F) return Blocks.ANDESITE.getDefaultState();
        } else if (original.isOf(Blocks.SAND)) {
            if (roll < 0.1F) return Blocks.SANDSTONE.getDefaultState();
            if (roll < 0.2F) return Blocks.GRAVEL.getDefaultState();
        } else if (original.isOf(Blocks.SNOW_BLOCK)) {
            if (roll < 0.1F) return Blocks.ICE.getDefaultState();
        }
        return original;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.TERRAIN_BLENDING;
    }
}
