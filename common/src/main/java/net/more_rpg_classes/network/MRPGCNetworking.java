package net.more_rpg_classes.network;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class MRPGCNetworking {

    public interface Sender {
        void sendToPlayer(ServerPlayerEntity player, CustomPayload payload);
    }

    private static Sender sender;

    public static void setSender(Sender sender) {
        MRPGCNetworking.sender = sender;
    }

    public static void sendToPlayer(ServerPlayerEntity player, CustomPayload payload) {
        sender.sendToPlayer(player, payload);
    }
}
