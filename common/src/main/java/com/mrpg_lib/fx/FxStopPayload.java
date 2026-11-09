package com.mrpg_lib.fx;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FxStopPayload(int handleId) implements CustomPayload {
    public static final CustomPayload.Id<FxStopPayload> ID = new CustomPayload.Id<>(Identifier.of("mrpg_lib", "fx_stop"));

    public static final PacketCodec<PacketByteBuf, FxStopPayload> CODEC = PacketCodec.of(FxStopPayload::write, FxStopPayload::read);

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(handleId);
    }

    private static FxStopPayload read(PacketByteBuf buf) {
        return new FxStopPayload(buf.readVarInt());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
