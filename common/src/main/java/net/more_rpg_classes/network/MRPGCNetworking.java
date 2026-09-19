package net.more_rpg_classes.network;

import net.minecraft.server.network.ServerPlayerEntity;
import net.spell_engine.network.Packets;

/**
 * Payloads are described with Spell Engine's {@link Packets.Payload} contract, but they must ride
 * THIS mod's own transport: Spell Engine's Forge `SimpleChannel` dispatches by message class and
 * answers "Invalid message" for a payload it never registered. Each platform entrypoint installs
 * its sender - Fabric a plain channel send, Forge this mod's `SimpleChannel`.
 */
public class MRPGCNetworking {

    public interface Sender {
        void sendToPlayer(ServerPlayerEntity player, Packets.Payload payload);
    }

    private static Sender sender;

    public static void setSender(Sender sender) {
        MRPGCNetworking.sender = sender;
    }

    public static void sendToPlayer(ServerPlayerEntity player, Packets.Payload payload) {
        if (sender == null) {
            return;
        }
        sender.sendToPlayer(player, payload);
    }
}
