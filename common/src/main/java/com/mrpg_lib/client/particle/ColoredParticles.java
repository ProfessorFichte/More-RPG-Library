package com.mrpg_lib.client.particle;

import com.mrpg_lib.MRPGCMod;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ColoredParticles {
    private static final List<ColoredParticleType> ALL = new ArrayList<>();
    private static final Map<SimpleParticleType, ColoredParticleType> BY_VANILLA = new IdentityHashMap<>();

    public static final ColoredParticleType EXPLOSION = add("colored_explosion", ParticleTypes.EXPLOSION);
    public static final ColoredParticleType EXPLOSION_EMITTER = add("colored_explosion_emitter", ParticleTypes.EXPLOSION_EMITTER);
    public static final ColoredParticleType POOF = add("colored_poof", ParticleTypes.POOF);
    public static final ColoredParticleType SMOKE = add("colored_smoke", ParticleTypes.SMOKE);
    public static final ColoredParticleType LARGE_SMOKE = add("colored_large_smoke", ParticleTypes.LARGE_SMOKE);
    public static final ColoredParticleType CLOUD = add("colored_cloud", ParticleTypes.CLOUD);
    public static final ColoredParticleType END_ROD = add("colored_end_rod", ParticleTypes.END_ROD);
    public static final ColoredParticleType FIREWORK = add("colored_firework", ParticleTypes.FIREWORK);
    public static final ColoredParticleType FLASH = add("colored_flash", ParticleTypes.FLASH);
    public static final ColoredParticleType WITCH = add("colored_witch", ParticleTypes.WITCH);
    public static final ColoredParticleType CRIT = add("colored_crit", ParticleTypes.CRIT);
    public static final ColoredParticleType ENCHANTED_HIT = add("colored_enchanted_hit", ParticleTypes.ENCHANTED_HIT);
    public static final ColoredParticleType SNOWFLAKE = add("colored_snowflake", ParticleTypes.SNOWFLAKE);

    private ColoredParticles() {
    }

    private static ColoredParticleType add(String name, SimpleParticleType vanilla) {
        var type = new ColoredParticleType(name, vanilla);
        ALL.add(type);
        BY_VANILLA.put(vanilla, type);
        return type;
    }

    public static List<ColoredParticleType> all() {
        return List.copyOf(ALL);
    }

    @Nullable
    public static ColoredParticleType variantOf(ParticleEffect vanilla) {
        return vanilla instanceof SimpleParticleType simple ? BY_VANILLA.get(simple) : null;
    }

    public static void register() {
        for (var type : ALL) {
            Registry.register(Registries.PARTICLE_TYPE, MRPGCMod.id(type.name()), type);
        }
    }
}
