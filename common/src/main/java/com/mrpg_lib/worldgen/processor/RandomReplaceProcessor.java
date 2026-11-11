package com.mrpg_lib.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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

    public static final MapCodec<RandomReplaceProcessor> CODEC = RecordCodecBuilder.<RandomReplaceProcessor>mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("target").forGetter(p -> p.target),
                    IntProvider.VALUE_CODEC.optionalFieldOf("count").forGetter(p -> p.count),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance").forGetter(p -> p.chance),
                    BlockState.CODEC.fieldOf("replacement").forGetter(p -> p.replacement),
                    Codec.BOOL.optionalFieldOf("keep_properties", false).forGetter(p -> p.keepProperties),
                    Identifier.CODEC.optionalFieldOf("loot_table").forGetter(p -> p.lootTable),
                    BlockState.CODEC.optionalFieldOf("fallback").forGetter(p -> p.fallback),
                    Codec.BOOL.optionalFieldOf("blend_fallback", false).forGetter(p -> p.blendFallback)
            ).apply(instance, RandomReplaceProcessor::new)
    ).validate(p -> p.count.isEmpty() && p.chance.isEmpty()
            ? DataResult.error(() -> "mrpg_lib:random_replace needs 'count', 'chance' or both")
            : DataResult.success(p));

    private final String target;
    private final Optional<IntProvider> count;
    private final Optional<Float> chance;
    private final BlockState replacement;
    private final boolean keepProperties;
    private final Optional<Identifier> lootTable;
    private final Optional<BlockState> fallback;
    private final boolean blendFallback;
    private final ProcessorBlocks.Matcher matcher;
    private final long salt;

    public RandomReplaceProcessor(String target, Optional<IntProvider> count, Optional<Float> chance, BlockState replacement,
                                  boolean keepProperties, Optional<Identifier> lootTable,
                                  Optional<BlockState> fallback, boolean blendFallback) {
        this.target = target;
        this.count = count;
        this.chance = chance;
        this.replacement = replacement;
        this.keepProperties = keepProperties;
        this.lootTable = lootTable;
        this.fallback = fallback;
        this.blendFallback = blendFallback;
        this.matcher = ProcessorBlocks.Matcher.parse(target, "mrpg_lib:random_replace", "target");
        this.salt = ProcessorBlocks.salt(target, replacement.toString(), lootTable.map(Identifier::toString).orElse(""));
    }

    public RandomReplaceProcessor(String target, IntProvider count, BlockState replacement, Optional<Identifier> lootTable,
                                  Optional<BlockState> fallback, boolean blendFallback) {
        this(target, Optional.of(count), Optional.empty(), replacement, false, lootTable, fallback, blendFallback);
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
        if (chance.isPresent()) {
            long seed = world.toServerWorld().getSeed() ^ salt;
            List<Integer> rolled = new ArrayList<>();
            List<Integer> missed = new ArrayList<>();
            for (int index : candidates) {
                Random blockRandom = ProcessorBlocks.blockRandom(seed, currentBlockInfos.get(index).pos());
                (blockRandom.nextFloat() < chance.get() ? rolled : missed).add(index);
            }
            Util.shuffle(rolled, random);
            candidates = new ArrayList<>(rolled);
            candidates.addAll(missed);
            int max = count.map(provider -> Math.max(0, provider.get(random))).orElse(rolled.size());
            return replace(currentBlockInfos, originalBlockInfos, candidates, Math.min(rolled.size(), max), random);
        }
        int picks = Math.min(candidates.size(), Math.max(0, count.orElseThrow().get(random)));
        Util.shuffle(candidates, random);
        return replace(currentBlockInfos, originalBlockInfos, candidates, picks, random);
    }

    private List<StructureBlockInfo> replace(List<StructureBlockInfo> currentBlockInfos, List<StructureBlockInfo> originalBlockInfos,
                                             List<Integer> candidates, int picks, Random random) {
        List<StructureBlockInfo> result = new ArrayList<>(currentBlockInfos);
        for (int i = 0; i < picks; i++) {
            int index = candidates.get(i);
            StructureBlockInfo info = currentBlockInfos.get(index);
            BlockState state = keepProperties ? ProcessorBlocks.copyProperties(info.state(), replacement) : replacement;
            result.set(index, new StructureBlockInfo(info.pos(), state, lootNbt(random)));
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
