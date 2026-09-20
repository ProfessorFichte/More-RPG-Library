package net.more_rpg_classes.network;

import net.minecraft.server.network.ServerPlayerEntity;
import net.spell_engine.network.Packets;

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
