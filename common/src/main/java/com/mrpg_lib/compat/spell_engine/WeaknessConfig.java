package com.mrpg_lib.compat.spell_engine;

import net.spell_engine.api.spell.weakness.ScopedWeakness;

import java.util.LinkedHashMap;
import java.util.List;

public class WeaknessConfig {
    public WeaknessConfig() {}
    public LinkedHashMap<String, List<ScopedWeakness>> school_weaknesses = new LinkedHashMap<>();
    public boolean isValid() {
        return school_weaknesses != null;
    }
}
