package com.mrpg_lib.worldgen.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.worldgen.processor.ProcessorBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record StructureBlockModifier(List<String> structures, List<String> pieces, String target, float chance, int maxPerPiece,
                                     BlockState replacement, Optional<Identifier> lootTable, List<String> requiredMods) {

    public static final Codec<StructureBlockModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().fieldOf("structures").forGetter(StructureBlockModifier::structures),
                    Codec.STRING.listOf().optionalFieldOf("pieces", List.of()).forGetter(StructureBlockModifier::pieces),
                    Codec.STRING.fieldOf("target").forGetter(StructureBlockModifier::target),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(StructureBlockModifier::chance),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_per_piece", 0).forGetter(StructureBlockModifier::maxPerPiece),
                    BlockState.CODEC.fieldOf("replacement").forGetter(StructureBlockModifier::replacement),
                    Identifier.CODEC.optionalFieldOf("loot_table").forGetter(StructureBlockModifier::lootTable),
                    Codec.STRING.listOf().optionalFieldOf("required_mods", List.of()).forGetter(StructureBlockModifier::requiredMods)
            ).apply(instance, StructureBlockModifier::new)
    );

    public Compiled compile(Identifier id) {
        String name = "structure block modifier " + id;
        List<Identifier> structureIds = new ArrayList<>();
        List<TagKey<Structure>> structureTags = new ArrayList<>();
        for (String entry : structures) {
            boolean tag = entry.startsWith("#");
            Identifier parsed = Identifier.tryParse(tag ? entry.substring(1) : entry);
            if (parsed == null) {
                MRPGCMod.LOGGER.warn("{}: invalid structure '{}'", name, entry);
            } else if (tag) {
                structureTags.add(TagKey.of(RegistryKeys.STRUCTURE, parsed));
            } else {
                structureIds.add(parsed);
            }
        }
        List<Identifier> pieceIds = new ArrayList<>();
        for (String entry : pieces) {
            Identifier parsed = Identifier.tryParse(entry);
            if (parsed == null || !Registries.STRUCTURE_PIECE.containsId(parsed)) {
                MRPGCMod.LOGGER.warn("{}: unknown structure piece type '{}'", name, entry);
            } else {
                pieceIds.add(parsed);
            }
        }
        RegistryKey<LootTable> loot = lootTable.map(table -> RegistryKey.of(RegistryKeys.LOOT_TABLE, table)).orElse(null);
        return new Compiled(this, structureIds, structureTags, pieceIds, ProcessorBlocks.Matcher.parse(target, name, "target"),
                loot, (long) id.hashCode() * 0x9E3779B97F4A7C15L);
    }

    public record Compiled(StructureBlockModifier modifier, List<Identifier> structureIds, List<TagKey<Structure>> structureTags,
                           List<Identifier> pieceIds, ProcessorBlocks.Matcher target, RegistryKey<LootTable> lootTable, long salt) {

        boolean matches(RegistryEntry<Structure> structure, StructurePiece piece) {
            boolean structureMatches = structureIds.stream().anyMatch(structure::matchesId)
                    || structureTags.stream().anyMatch(structure::isIn);
            if (!structureMatches) return false;
            return pieceIds.isEmpty() || pieceIds.contains(Registries.STRUCTURE_PIECE.getId(piece.getType()));
        }

        void apply(StructureWorldAccess world, BlockBox box) {
            int replaced = 0;
            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (int x = box.getMinX(); x <= box.getMaxX(); x++) {
                for (int z = box.getMinZ(); z <= box.getMaxZ(); z++) {
                    for (int y = box.getMinY(); y <= box.getMaxY(); y++) {
                        pos.set(x, y, z);
                        if (!target.matches(world.getBlockState(pos))) continue;
                        Random random = Random.create(world.getSeed() ^ MathHelper.hashCode(pos) ^ salt);
                        if (random.nextFloat() >= modifier.chance()) continue;
                        world.setBlockState(pos, modifier.replacement(), Block.NOTIFY_LISTENERS);
                        setLootTable(world.getBlockEntity(pos), random.nextLong());
                        replaced++;
                        if (modifier.maxPerPiece() > 0 && replaced >= modifier.maxPerPiece()) return;
                    }
                }
            }
        }

        private void setLootTable(BlockEntity blockEntity, long seed) {
            if (lootTable == null) return;
            if (blockEntity instanceof LootableInventory inventory) {
                inventory.setLootTable(lootTable, seed);
            } else if (blockEntity instanceof BrushableBlockEntity brushable) {
                brushable.setLootTable(lootTable, seed);
            }
        }
    }
}
