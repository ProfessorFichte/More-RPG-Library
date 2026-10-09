package com.mrpg_lib.compat.player_animator.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Environment(EnvType.CLIENT)
public final class MobModelAdapters {
    public record Binding(@Nullable MobModelAdapter adapter, @Nullable MobModelParts parts) {
    }

    private static final Binding NONE = new Binding(null, null);
    private static final List<MobModelAdapter> CUSTOM = new ArrayList<>();
    private static final List<MobModelAdapter> BUILT_IN = List.of(new IllagerModelAdapter(), new VillagerModelAdapter(), new BipedModelAdapter());
    private static final Map<EntityModel<?>, Binding> CACHE = new WeakHashMap<>();

    private MobModelAdapters() {
    }

    public static void register(MobModelAdapter adapter) {
        CUSTOM.add(0, adapter);
        CACHE.clear();
    }

    @Nullable
    public static Binding resolve(EntityModel<?> model) {
        Binding binding = CACHE.get(model);
        if (binding == null) {
            binding = find(model);
            CACHE.put(model, binding);
        }
        return binding == NONE ? null : binding;
    }

    private static Binding find(EntityModel<?> model) {
        for (var adapters : List.of(CUSTOM, BUILT_IN)) {
            for (MobModelAdapter adapter : adapters) {
                MobModelParts parts = adapter.parts(model);
                if (parts != null) return new Binding(adapter, parts);
            }
        }
        return NONE;
    }
}
