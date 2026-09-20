package net.more_rpg_classes.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.spell_engine.network.Packets;
import org.jetbrains.annotations.Nullable;

/**
 * S2C packet sent when a mob starts or stops casting a BEAM spell.
 */
public record MobBeamPacket(int casterId, int targetId, @Nullable Identifier spellId) implements Packets.Payload {

    public static final Identifier ID = new Identifier("more_rpg_classes", "mob_beam");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(casterId);
        buf.writeInt(targetId);
        buf.writeBoolean(spellId != null);
        if (spellId != null) buf.writeIdentifier(spellId);
    }

    public static MobBeamPacket read(PacketByteBuf buf) {
        int casterId = buf.readInt();
        int targetId = buf.readInt();
        Identifier spellId = buf.readBoolean() ? buf.readIdentifier() : null;
        return new MobBeamPacket(casterId, targetId, spellId);
    }
}
