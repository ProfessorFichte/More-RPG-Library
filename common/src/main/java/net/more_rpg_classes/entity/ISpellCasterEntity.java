package net.more_rpg_classes.entity;

import net.minecraft.entity.mob.MobEntity;

public interface ISpellCasterEntity {

    void startSpellCast(int ticks);

    void stopSpellCast();

    boolean isSpellcasting();

    MobEntity asMobEntity();
}
