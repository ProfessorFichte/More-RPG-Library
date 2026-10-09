package com.mrpg_lib.network;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class MRPGCNetworking {

    public interface Sender {
        void sendToPlayer(ServerPlayerEntity player, CustomPayload payload);
    }

    public interface TrackingSender {
        void sendToTracking(Entity entity, CustomPayload payload);
    }

    private static Sender sender;
    private static TrackingSender trackingSender;

    public static void setSender(Sender sender) {
        MRPGCNetworking.sender = sender;
    }

    public static void setTrackingSender(TrackingSender trackingSender) {
        MRPGCNetworking.trackingSender = trackingSender;
    }

    public static void sendToPlayer(ServerPlayerEntity player, CustomPayload payload) {
        sender.sendToPlayer(player, payload);
    }

    public static void sendToTracking(Entity entity, CustomPayload payload) {
        trackingSender.sendToTracking(entity, payload);
    }
}
