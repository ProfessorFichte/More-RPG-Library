package com.mrpg_lib.fabric.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class MRPGCNetworkingImpl {
    public static void sendToPlayer(ServerPlayerEntity player, CustomPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendToTracking(Entity entity, CustomPayload payload) {
        for (ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
            if (ServerPlayNetworking.canSend(player, payload.getId())) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
