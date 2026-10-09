package com.mrpg_lib.util.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface SchoolPowerProvider {
    double power(LivingEntity entity, Identifier schoolId);
}
