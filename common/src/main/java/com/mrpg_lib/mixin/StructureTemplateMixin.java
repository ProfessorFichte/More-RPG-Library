package com.mrpg_lib.mixin;

import com.mrpg_lib.worldgen.processor.EntityNbtProcessor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Inject(method = "place", at = @At("HEAD"))
    private void mrpg$beginPlace(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData data, Random random,
                                 int flags, CallbackInfoReturnable<Boolean> cir) {
        EntityNbtProcessor.PLACING.set(data);
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void mrpg$endPlace(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData data, Random random,
                               int flags, CallbackInfoReturnable<Boolean> cir) {
        EntityNbtProcessor.PLACING.remove();
    }

    @Inject(method = "getEntity", at = @At("HEAD"))
    private static void mrpg$processEntity(ServerWorldAccess world, NbtCompound nbt, CallbackInfoReturnable<Optional<Entity>> cir) {
        EntityNbtProcessor.apply(world, nbt);
    }
}
