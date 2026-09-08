package net.more_rpg_classes.network;

import net.minecraft.server.network.ServerPlayerEntity;
import net.spell_engine.Platform;
import net.spell_engine.network.Packets;

/**
 * Sending goes through Spell Engine's loader-agnostic transport, so there is no per-platform
 * sender to wire up. Receiving is still loader-specific (see FabricClient / MrpgForgeNetwork).
 */
public class MRPGCNetworking {

    public static void sendToPlayer(ServerPlayerEntity player, Packets.Payload payload) {
        Platform.util().networkS2C_Send(player, payload);
    }
}
