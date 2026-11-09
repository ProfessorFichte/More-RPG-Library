package com.mrpg_lib.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;

public final class ColoredParticleType extends ParticleType<ColoredParticleEffect> {
    private final String name;
    private final SimpleParticleType vanilla;
    private final MapCodec<ColoredParticleEffect> codec;
    private final PacketCodec<RegistryByteBuf, ColoredParticleEffect> packetCodec;

    ColoredParticleType(String name, SimpleParticleType vanilla) {
        super(false);
        this.name = name;
        this.vanilla = vanilla;
        this.codec = ColoredParticleEffect.createCodec(this);
        this.packetCodec = ColoredParticleEffect.createPacketCodec(this);
    }

    public String name() {
        return name;
    }

    public SimpleParticleType vanilla() {
        return vanilla;
    }

    public ColoredParticleEffect of(int color) {
        return new ColoredParticleEffect(this, color, 1.0F);
    }

    public ColoredParticleEffect of(int color, float scale) {
        return new ColoredParticleEffect(this, color, scale);
    }

    @Override
    public MapCodec<ColoredParticleEffect> getCodec() {
        return codec;
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, ColoredParticleEffect> getPacketCodec() {
        return packetCodec;
    }
}
