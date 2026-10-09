package com.mrpg_lib.compat.spell_engine.particle;

import net.minecraft.particle.ParticleEffect;

public final class SoakedMist {
    private SoakedMist() {
    }

    public static ParticleEffect type() {
        return MoreSpellParticles.WATER_MIST.type();
    }
}
