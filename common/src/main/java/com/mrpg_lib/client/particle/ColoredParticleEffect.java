package com.mrpg_lib.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record ColoredParticleEffect(ColoredParticleType type, int color, float scale) implements ParticleEffect {
    public ColoredParticleEffect(ColoredParticleType type, int color) {
        this(type, color, 1.0F);
    }

    @Override
    public ParticleType<ColoredParticleEffect> getType() {
        return type;
    }

    static MapCodec<ColoredParticleEffect> createCodec(ColoredParticleType type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("color").forGetter(ColoredParticleEffect::color),
                Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(ColoredParticleEffect::scale)
        ).apply(instance, (color, scale) -> new ColoredParticleEffect(type, color, scale)));
    }

    static PacketCodec<RegistryByteBuf, ColoredParticleEffect> createPacketCodec(ColoredParticleType type) {
        return PacketCodec.tuple(
                PacketCodecs.INTEGER, ColoredParticleEffect::color,
                PacketCodecs.FLOAT, ColoredParticleEffect::scale,
                (color, scale) -> new ColoredParticleEffect(type, color, scale));
    }
}
