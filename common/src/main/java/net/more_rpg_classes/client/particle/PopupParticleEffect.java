package net.more_rpg_classes.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class PopupParticleEffect implements ParticleEffect {
    private final ParticleType<PopupParticleEffect> type;
    public final Identifier iconId;
    public final boolean isSpell;
    public final int entityId;

    public static final ParticleEffect.Factory<PopupParticleEffect> FACTORY = new ParticleEffect.Factory<>() {
        @Override
        public PopupParticleEffect read(ParticleType<PopupParticleEffect> type, StringReader reader) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
            reader.expect(' ');
            Identifier iconId = Identifier.fromCommandInput(reader);
            reader.expect(' ');
            boolean isSpell = reader.readBoolean();
            reader.expect(' ');
            int entityId = reader.readInt();
            return new PopupParticleEffect(type, iconId, isSpell, entityId);
        }

        @Override
        public PopupParticleEffect read(ParticleType<PopupParticleEffect> type, PacketByteBuf buf) {
            return new PopupParticleEffect(type, buf.readIdentifier(), buf.readBoolean(), buf.readVarInt());
        }
    };

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

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(iconId);
        buf.writeBoolean(isSpell);
        buf.writeVarInt(entityId);
    }

    @Override
    public String asString() {
        return Registries.PARTICLE_TYPE.getId(this.getType()) + " " + iconId + " " + isSpell + " " + entityId;
    }

    public static Codec<PopupParticleEffect> createCodec(ParticleType<PopupParticleEffect> type) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("icon").forGetter(e -> e.iconId),
            Codec.BOOL.fieldOf("is_spell").forGetter(e -> e.isSpell),
            Codec.INT.fieldOf("entity").forGetter(e -> e.entityId)
        ).apply(instance, (iconId, isSpell, entityId) -> new PopupParticleEffect(type, iconId, isSpell, entityId)));
    }
}
