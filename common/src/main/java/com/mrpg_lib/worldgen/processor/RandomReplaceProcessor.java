package com.mrpg_lib.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrpg_lib.worldgen.ModStructureProcessorTypes;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.ServerWorldAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RandomReplaceProcessor extends StructureProcessor {

    public static final MapCodec<RandomReplaceProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("target").forGetter(p -> p.target),
                    IntProvider.VALUE_CODEC.fieldOf("count").forGetter(p -> p.count),
                    BlockState.CODEC.fieldOf("replacement").forGetter(p -> p.replacement),
                    Identifier.CODEC.optionalFieldOf("loot_table").forGetter(p -> p.lootTable),
                    BlockState.CODEC.optionalFieldOf("fallback").forGetter(p -> p.fallback),
                    Codec.BOOL.optionalFieldOf("blend_fallback", false).forGetter(p -> p.blendFallback)
            ).apply(instance, RandomReplaceProcessor::new)
    );

    private final String target;
    private final IntProvider count;
    private final BlockState replacement;
    private final Optional<Identifier> lootTable;
    private final Optional<BlockState> fallback;
    private final boolean blendFallback;
    private final ProcessorBlocks.Matcher matcher;

    public RandomReplaceProcessor(String target, IntProvider count, BlockState replacement, Optional<Identifier> lootTable,
                                  Optional<BlockState> fallback, boolean blendFallback) {
        this.target = target;
        this.count = count;
        this.replacement = replacement;
        this.lootTable = lootTable;
        this.fallback = fallback;
        this.blendFallback = blendFallback;
        this.matcher = ProcessorBlocks.Matcher.parse(target, "mrpg_lib:random_replace", "target");
    }

    private boolean matches(BlockState state) {
        return matcher.matches(state);
    }

    @Override
    public List<StructureBlockInfo> reprocess(ServerWorldAccess world, BlockPos pos, BlockPos pivot,
                                              List<StructureBlockInfo> originalBlockInfos,
                                              List<StructureBlockInfo> currentBlockInfos,
                                              StructurePlacementData data) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < currentBlockInfos.size(); i++) {
            if (matches(currentBlockInfos.get(i).state())) candidates.add(i);
        }
        if (candidates.isEmpty()) return currentBlockInfos;

        Random random = data.getRandom(pos);
        int picks = Math.min(candidates.size(), Math.max(0, count.get(random)));
        Util.shuffle(candidates, random);

        List<StructureBlockInfo> result = new ArrayList<>(currentBlockInfos);
        for (int i = 0; i < picks; i++) {
            int index = candidates.get(i);
            StructureBlockInfo info = currentBlockInfos.get(index);
            result.set(index, new StructureBlockInfo(info.pos(), replacement, lootNbt(random)));
        }
        if (fallback.isEmpty() && !blendFallback) return result;

        Map<BlockPos, BlockState> states = blendFallback ? templateStatesByPos(originalBlockInfos, currentBlockInfos) : Map.of();
        for (int i = picks; i < candidates.size(); i++) {
            int index = candidates.get(i);
            StructureBlockInfo info = currentBlockInfos.get(index);
            BlockState state = blendFallback ? blendedState(states, info.pos()) : null;
            if (state == null) state = fallback.orElse(null);
            if (state != null) result.set(index, new StructureBlockInfo(info.pos(), state, null));
        }
        return result;
    }

    private NbtCompound lootNbt(Random random) {
        if (lootTable.isEmpty()) return null;
        NbtCompound nbt = new NbtCompound();
        nbt.putString("LootTable", lootTable.get().toString());
        nbt.putLong("LootTableSeed", random.nextLong());
        return nbt;
    }

    // The original infos use template positions but share their index with the placed infos,
    // so the blend looks at the blocks the builder placed and not at what other processors turned them into.
    private static Map<BlockPos, BlockState> templateStatesByPos(List<StructureBlockInfo> original, List<StructureBlockInfo> current) {
        boolean aligned = original.size() == current.size();
        Map<BlockPos, BlockState> states = new HashMap<>();
        for (int i = 0; i < current.size(); i++) {
            StructureBlockInfo info = aligned ? original.get(i) : current.get(i);
            if (info.nbt() == null && !info.state().hasBlockEntity()) states.put(current.get(i).pos(), info.state());
        }
        return states;
    }

    private BlockState blendedState(Map<BlockPos, BlockState> states, BlockPos pos) {
        Map<BlockState, Integer> counts = new HashMap<>();
        for (Direction direction : Direction.values()) {
            BlockState neighbor = states.get(pos.offset(direction));
            if (neighbor == null || matches(neighbor) || !neighbor.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) continue;
            counts.merge(neighbor, 1, Integer::sum);
        }
        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.RANDOM_REPLACE;
    }
}
