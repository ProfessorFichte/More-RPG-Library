package com.mrpg_lib.compat.spell_engine.network;

import net.minecraft.util.math.Vec3d;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record MobBeamPacket(int casterId, int targetId, @Nullable Identifier spellId, @Nullable Vec3d direction) implements CustomPayload {

    public MobBeamPacket(int casterId, int targetId, @Nullable Identifier spellId) {
        this(casterId, targetId, spellId, null);
    }

    public static final CustomPayload.Id<MobBeamPacket> ID =
            new CustomPayload.Id<>(Identifier.of("mrpg_lib", "mob_beam"));

    public static final PacketCodec<PacketByteBuf, MobBeamPacket> CODEC = PacketCodec.of(
            MobBeamPacket::write,
            MobBeamPacket::read
    );

    private void write(PacketByteBuf buf) {
        buf.writeInt(casterId);
        buf.writeInt(targetId);
        buf.writeBoolean(spellId != null);
        if (spellId != null) buf.writeIdentifier(spellId);
        buf.writeBoolean(direction != null);
        if (direction != null) buf.writeVec3d(direction);
    }

    private static MobBeamPacket read(PacketByteBuf buf) {
        int casterId = buf.readInt();
        int targetId = buf.readInt();
        Identifier spellId = buf.readBoolean() ? buf.readIdentifier() : null;
        Vec3d direction = buf.readBoolean() ? buf.readVec3d() : null;
        return new MobBeamPacket(casterId, targetId, spellId, direction);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
