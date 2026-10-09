package com.mrpg_lib.compat.player_animator.network;

import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import com.mrpg_lib.compat.player_animator.api.MobAnimationOptions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record MobAnimationPacket(
        int entityId,
        @Nullable Identifier animationId,
        MobAnimationLayer layer,
        float speed,
        int fadeTicks,
        MobAnimationOptions.Mirror mirror,
        float length,
        float upswingRate,
        float upswingMultiplier,
        MobAnimationOptions.PoseStyle poseStyle) implements CustomPayload {

    public static final CustomPayload.Id<MobAnimationPacket> ID =
            new CustomPayload.Id<>(Identifier.of("mrpg_lib", "mob_animation"));

    public static final PacketCodec<PacketByteBuf, MobAnimationPacket> CODEC = PacketCodec.of(
            MobAnimationPacket::write,
            MobAnimationPacket::read
    );

    public MobAnimationPacket(int entityId, @Nullable Identifier animationId, MobAnimationLayer layer, float speed,
                              int fadeTicks, MobAnimationOptions.Mirror mirror, float length, float upswingRate, float upswingMultiplier) {
        this(entityId, animationId, layer, speed, fadeTicks, mirror, length, upswingRate, upswingMultiplier, MobAnimationOptions.PoseStyle.PLAIN);
    }

    public MobAnimationPacket(int entityId, @Nullable Identifier animationId, MobAnimationLayer layer, float speed,
                              int fadeTicks, MobAnimationOptions.Mirror mirror, int durationTicks) {
        this(entityId, animationId, layer, speed, fadeTicks, mirror, Math.max(0, durationTicks), 0.0F, 0.0F);
    }

    public boolean isStop() {
        return animationId == null;
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(animationId != null);
        if (animationId != null) buf.writeIdentifier(animationId);
        buf.writeEnumConstant(layer);
        buf.writeFloat(speed);
        buf.writeVarInt(fadeTicks + 1);
        buf.writeEnumConstant(mirror);
        buf.writeFloat(length);
        buf.writeFloat(upswingRate);
        buf.writeFloat(upswingMultiplier);
        buf.writeEnumConstant(poseStyle);
    }

    private static MobAnimationPacket read(PacketByteBuf buf) {
        int entityId = buf.readVarInt();
        Identifier animationId = buf.readBoolean() ? buf.readIdentifier() : null;
        MobAnimationLayer layer = buf.readEnumConstant(MobAnimationLayer.class);
        float speed = buf.readFloat();
        int fadeTicks = buf.readVarInt() - 1;
        MobAnimationOptions.Mirror mirror = buf.readEnumConstant(MobAnimationOptions.Mirror.class);
        float length = buf.readFloat();
        float upswingRate = buf.readFloat();
        float upswingMultiplier = buf.readFloat();
        MobAnimationOptions.PoseStyle poseStyle = buf.readEnumConstant(MobAnimationOptions.PoseStyle.class);
        return new MobAnimationPacket(entityId, animationId, layer, speed, fadeTicks, mirror, length, upswingRate, upswingMultiplier, poseStyle);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
