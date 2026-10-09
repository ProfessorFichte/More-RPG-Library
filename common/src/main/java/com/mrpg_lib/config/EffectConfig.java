package com.mrpg_lib.config;

import com.mrpg_lib.platform.MrpgPlatform;

import java.util.List;

public class EffectConfig {
    public static final EffectConfig EMPTY = new EffectConfig(List.of());

    public List<AttributeModifier> attributes = List.of();
    public ConditionalAttributes conditional_attributes;

    public EffectConfig() {
    }

    public EffectConfig(List<AttributeModifier> attributes) {
        this.attributes = attributes;
    }

    public List<AttributeModifier> selectedAttributes() {
        if (conditional_attributes != null
                && conditional_attributes.required_mod() != null
                && MrpgPlatform.isModLoaded(conditional_attributes.required_mod())) {
            return conditional_attributes.attributes();
        }
        return attributes;
    }

    public List<AttributeModifier> attributes() {
        return attributes;
    }

    public AttributeModifier firstModifier() {
        var selected = selectedAttributes();
        return selected.isEmpty() ? AttributeModifier.EMPTY : selected.get(0);
    }

    public EffectConfig conditionalAttributes(String requiredMod, List<AttributeModifier> attributes) {
        this.conditional_attributes = new ConditionalAttributes(requiredMod, attributes);
        return this;
    }
}
