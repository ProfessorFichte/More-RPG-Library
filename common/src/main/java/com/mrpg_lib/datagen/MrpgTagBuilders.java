package com.mrpg_lib.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.Registry;

public final class MrpgTagBuilders {
    private MrpgTagBuilders() {
    }

    @SafeVarargs
    public static <T> FabricTagProvider<T>.FabricTagBuilder addOptional(
            FabricTagProvider<T>.FabricTagBuilder builder, Registry<T> registry, T... entries) {
        for (T entry : entries) {
            builder.addOptional(registry.getId(entry));
        }
        return builder;
    }
}
