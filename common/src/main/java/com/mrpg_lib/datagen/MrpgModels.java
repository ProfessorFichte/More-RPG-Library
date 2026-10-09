package com.mrpg_lib.datagen;

import com.mrpg_lib.compat.armory_rpgs.SmithingIngredients;
import com.mrpg_lib.item.MRPGCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MrpgModels extends FabricModelProvider {
    public MrpgModels(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        Stream.of(MRPGCItems.WOLF_FUR, MRPGCItems.POLAR_BEAR_FUR, MRPGCItems.HARDENED_LEATHER,
                MRPGCItems.AQUA_STONE, MRPGCItems.TERRA_STONE, MRPGCItems.STORM_STONE, MRPGCItems.NATURE_STONE
        ).filter(Objects::nonNull).forEach(item -> itemModelGenerator.register(item, Models.GENERATED));
        SmithingIngredients.ENTRIES.forEach(entry -> itemModelGenerator.register(entry.item().get(), Models.GENERATED));
    }
}
