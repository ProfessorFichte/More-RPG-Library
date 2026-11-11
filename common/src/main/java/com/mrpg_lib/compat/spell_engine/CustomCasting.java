package com.mrpg_lib.compat.spell_engine;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface CustomCasting {

    int castTicks(MobEntity mob, Identifier spellId, int defaultTicks);

    default void onCastStart(MobEntity mob, Identifier spellId, int castTicks) {
    }

    default void onRelease(MobEntity mob, Identifier spellId) {
    }

    default void onCastStop(MobEntity mob, Identifier spellId) {
    }

    static CustomCasting ticks(int ticks) {
        return (mob, spellId, defaultTicks) -> ticks;
    }

    static CustomCasting spellDuration() {
        return (mob, spellId, defaultTicks) -> defaultTicks;
    }
}
