package com.mrpg_lib.entity.goal;

import net.minecraft.util.Identifier;

import java.util.List;

public interface IConditionalFleeEntity {
    float getFleeDistance();
    float getLowHealthFleeDistance();
    List<Identifier> getFleeImmuneEffects();
    List<Identifier> getFleeIgnoreIfTargetHasEffects();
    float getFleeIgnoreTargetHpThreshold();
}
