package com.mrpg_lib.entity;

import net.minecraft.entity.mob.MobEntity;

public interface ISpellCasterEntity {

    void startSpellCast(int ticks);

    void stopSpellCast();

    boolean isMobCasting();

    MobEntity asMobEntity();
}
