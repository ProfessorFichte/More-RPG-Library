package com.mrpg_lib.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class CustomModelHelper {
    private static final List<Identifier> MODEL_IDS = new ArrayList<>();
    private static Function<Identifier, BakedModel> lookup = id -> null;

    private CustomModelHelper() {
    }

    public static void registerModelIds(List<Identifier> ids) {
        MODEL_IDS.addAll(ids);
    }

    public static List<Identifier> modelIds() {
        return MODEL_IDS;
    }

    public static void setLookup(Function<Identifier, BakedModel> lookup) {
        CustomModelHelper.lookup = lookup;
    }

    public static BakedModel bakedModel(Identifier id) {
        return lookup.apply(id);
    }
}
