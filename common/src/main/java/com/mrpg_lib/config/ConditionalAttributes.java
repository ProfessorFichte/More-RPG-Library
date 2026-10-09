package com.mrpg_lib.config;

import java.util.List;

public record ConditionalAttributes(String required_mod, List<AttributeModifier> attributes) {
}
