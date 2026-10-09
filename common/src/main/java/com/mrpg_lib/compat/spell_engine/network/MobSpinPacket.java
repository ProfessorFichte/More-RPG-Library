package com.mrpg_lib.compat.spell_engine.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MobSpinPacket(int casterId, float degreesPerTick, int durationTicks) implements CustomPayload {

    public static final CustomPayload.Id<MobSpinPacket> ID =
            new CustomPayload.Id<>(Identifier.of("mrpg_lib", "mob_spin"));

    public static final PacketCodec<PacketByteBuf, MobSpinPacket> CODEC = PacketCodec.of(
            MobSpinPacket::write,
            MobSpinPacket::read
    );

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(casterId);
        buf.writeFloat(degreesPerTick);
        buf.writeVarInt(durationTicks);
    }

    private static MobSpinPacket read(PacketByteBuf buf) {
        return new MobSpinPacket(buf.readVarInt(), buf.readFloat(), buf.readVarInt());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
