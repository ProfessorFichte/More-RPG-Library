package com.mrpg_lib.entity.config;

import net.minecraft.world.Difficulty;

public final class CustomValues {
    private final EntityAttributeConfig.Entry configured;
    private final EntityAttributeConfig.Entry fallback;
    private final Difficulty difficulty;

    public CustomValues(EntityAttributeConfig.Entry configured, EntityAttributeConfig.Entry fallback, Difficulty difficulty) {
        this.configured = configured;
        this.fallback = fallback;
        this.difficulty = difficulty;
    }

    public double getDouble(String id, double defaultValue) {
        EntityAttributeConfig.Entry source = numberSource(id);
        return source != null ? source.getDouble(id, difficulty, defaultValue) : defaultValue;
    }

    public int getInt(String id, int defaultValue) {
        EntityAttributeConfig.Entry source = numberSource(id);
        return source != null ? source.getInt(id, difficulty, defaultValue) : defaultValue;
    }

    public boolean getBoolean(String id, boolean defaultValue) {
        EntityAttributeConfig.Entry source = numberSource(id);
        return source != null ? source.getBoolean(id, difficulty, defaultValue) : defaultValue;
    }

    public String getString(String id, String defaultValue) {
        if (configured != null && configured.hasString(id)) return configured.getString(id, difficulty, defaultValue);
        if (fallback != null && fallback.hasString(id)) return fallback.getString(id, difficulty, defaultValue);
        return defaultValue;
    }

    private EntityAttributeConfig.Entry numberSource(String id) {
        if (configured != null && configured.hasNumber(id)) return configured;
        if (fallback != null && fallback.hasNumber(id)) return fallback;
        return null;
    }
}
