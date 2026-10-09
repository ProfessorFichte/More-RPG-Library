package com.mrpg_lib.neoforge.network;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class MRPGCNetworkingImpl {
    public static void sendToPlayer(ServerPlayerEntity player, CustomPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToTracking(Entity entity, CustomPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }
}
