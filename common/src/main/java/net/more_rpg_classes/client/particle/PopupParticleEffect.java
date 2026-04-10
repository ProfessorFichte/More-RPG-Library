package net.more_rpg_classes.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;

public class PopupParticleEffect implements ParticleEffect {
    private final ParticleType<PopupParticleEffect> type;
    public final Identifier iconId;
    public final boolean isSpell;
    public final int entityId;

    public PopupParticleEffect(ParticleType<PopupParticleEffect> type, Identifier iconId, boolean isSpell, int entityId) {
        this.type = type;
        this.iconId = iconId;
        this.isSpell = isSpell;
        this.entityId = entityId;
    }

    @Override
    public ParticleType<PopupParticleEffect> getType() {
        return type;
    }

    public static MapCodec<PopupParticleEffect> createCodec(ParticleType<PopupParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("icon").forGetter(e -> e.iconId),
            Codec.BOOL.fieldOf("is_spell").forGetter(e -> e.isSpell),
            Codec.INT.fieldOf("entity").forGetter(e -> e.entityId)
        ).apply(instance, (iconId, isSpell, entityId) -> new PopupParticleEffect(type, iconId, isSpell, entityId)));
    }

    public static PacketCodec<RegistryByteBuf, PopupParticleEffect> createPacketCodec(ParticleType<PopupParticleEffect> type) {
        return PacketCodec.tuple(
            Identifier.PACKET_CODEC, e -> e.iconId,
            PacketCodecs.BOOL, e -> e.isSpell,
            PacketCodecs.VAR_INT, e -> e.entityId,
            (iconId, isSpell, entityId) -> new PopupParticleEffect(type, iconId, isSpell, entityId)
        );
    }
}
