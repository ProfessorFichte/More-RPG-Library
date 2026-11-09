package com.mrpg_lib.fx;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FxPlayPayload(Identifier effect, FxParams params, int handleId) implements CustomPayload {
    public static final CustomPayload.Id<FxPlayPayload> ID = new CustomPayload.Id<>(Identifier.of("mrpg_lib", "fx_play"));

    public static final PacketCodec<PacketByteBuf, FxPlayPayload> CODEC = PacketCodec.of(FxPlayPayload::write, FxPlayPayload::read);

    private void write(PacketByteBuf buf) {
        buf.writeIdentifier(effect);
        params.write(buf);
        buf.writeVarInt(handleId);
    }

    private static FxPlayPayload read(PacketByteBuf buf) {
        return new FxPlayPayload(buf.readIdentifier(), FxParams.read(buf), buf.readVarInt());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
