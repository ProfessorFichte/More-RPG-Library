package com.mrpg_lib.fabric.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.spell_engine.network.Packets;

public class MRPGCNetworkingImpl {
    /// Fabric API 0.92 sends by channel id, so any payload shape works.
    public static void sendToPlayer(ServerPlayerEntity player, Packets.Payload payload) {
        ServerPlayNetworking.send(player, payload.id(), payload.toBuffer());
    }
}
