package com.mrpg_lib.forge.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.network.MobBeamPacket;
import net.spell_engine.network.Packets;

/// Forge 47 networking: one `SimpleChannel` carrying this mod's payloads. 1.20.1 has no
/// `CustomPayload` registry, so each message is registered with its own encoder/decoder pair.
/// Sending goes through Spell Engine's transport (see `MRPGCNetworking`); this channel only
/// has to exist so the client can receive.
public class MrpgForgeNetwork {
    public static final Identifier CHANNEL_NAME = new Identifier(MRPGCMod.MOD_ID, "main");
    private static final String PROTOCOL_VERSION = "1";

    /// Lenient version predicates: a peer without the channel (vanilla client, library-less server)
    /// still connects, matching Fabric's behaviour.
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(CHANNEL_NAME)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
            .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
            .simpleChannel();

    private static boolean registered = false;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.messageBuilder(MobBeamPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((packet, buffer) -> packet.write(buffer))
                .decoder(MobBeamPacket::read)
                // The handler body resolves the client class lazily, so a dedicated server never loads it.
                .consumerMainThread((packet, contextSupplier) -> {
                    contextSupplier.get().enqueueWork(() -> MrpgForgeClientNetwork.handleMobBeam(packet));
                    contextSupplier.get().setPacketHandled(true);
                })
                .add();
    }

    /// Sends on THIS mod's channel. Spell Engine's channel would reject the payload: a Forge
    /// `SimpleChannel` dispatches by registered message class, not by channel id.
    public static void sendToPlayer(ServerPlayerEntity player, Packets.Payload payload) {
        if (payload instanceof MobBeamPacket packet) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    private MrpgForgeNetwork() { }

    /// Buffer type alias kept explicit so the encoder above reads clearly.
    interface Encoder {
        void write(PacketByteBuf buffer);
    }
}
