package com.mrpg_lib.util;

import net.minecraft.entity.LivingEntity;

public final class FreezeMethods {
    public static final int POWDER_SNOW_MAX_FROZEN_TICKS = 160;

    private FreezeMethods() {
    }

    public static void addFrozenTicksCapped(LivingEntity entity, int amount) {
        addFrozenTicksCapped(entity, amount, POWDER_SNOW_MAX_FROZEN_TICKS);
    }

    public static void addFrozenTicksCapped(LivingEntity entity, int amount, int cap) {
        entity.setFrozenTicks(Math.min(cap, entity.getFrozenTicks() + amount));
    }
}
