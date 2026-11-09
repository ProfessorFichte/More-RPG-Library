package com.mrpg_lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrpg_lib.worldgen.modifier.StructureBlockModifiers;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
    @Shadow
    public abstract Structure getStructure();

    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/StructurePiece;generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/util/math/BlockPos;)V"))
    private void mrpg$applyBlockModifiers(StructurePiece piece, StructureWorldAccess world, StructureAccessor accessor, ChunkGenerator generator,
                                          Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot, Operation<Void> original) {
        original.call(piece, world, accessor, generator, random, chunkBox, chunkPos, pivot);
        StructureBlockModifiers.afterPiece(world, getStructure(), piece, chunkBox);
    }
}
