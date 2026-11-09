package com.mrpg_lib.fx;

import java.util.function.Consumer;
import java.util.function.Function;

public final class FxPreset {
    private final String name;
    private final Consumer<FxTweaks> defaults;
    private final Function<FxTweaks, FxEffect.Builder> recipe;

    public FxPreset(String name, Consumer<FxTweaks> defaults, Function<FxTweaks, FxEffect.Builder> recipe) {
        this.name = name;
        this.defaults = defaults;
        this.recipe = recipe;
    }

    public String name() {
        return name;
    }

    public FxEffect tweak(Consumer<FxTweaks> tweaks) {
        var values = new FxTweaks();
        defaults.accept(values);
        tweaks.accept(values);
        var builder = recipe.apply(values);
        if (!values.silent && values.sound != null) {
            builder.sound(values.sound);
        }
        values.extraSounds.forEach(builder::sound);
        return builder.build();
    }

    public FxEffect create() {
        return tweak(tweaks -> {
        });
    }
}
