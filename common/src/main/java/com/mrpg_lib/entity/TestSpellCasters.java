package com.mrpg_lib.entity;

import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class TestSpellCasters {
    private static final Set<Entity> MARKED = Collections.newSetFromMap(new WeakHashMap<>());

    private TestSpellCasters() {
    }

    public static void mark(Entity entity) {
        MARKED.add(entity);
    }

    public static void unmark(Entity entity) {
        MARKED.remove(entity);
    }

    public static boolean isMarked(Entity entity) {
        return MARKED.contains(entity);
    }
}
