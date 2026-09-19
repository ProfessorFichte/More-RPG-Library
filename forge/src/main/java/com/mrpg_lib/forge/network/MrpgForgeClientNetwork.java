package com.mrpg_lib.forge.network;

import net.minecraft.client.MinecraftClient;
import net.more_rpg_classes.client.render.MobBeamTracker;
import net.more_rpg_classes.network.MobBeamPacket;

/// Client-side handlers. Only touched from inside a handler body, so a dedicated server never
/// classloads it.
public class MrpgForgeClientNetwork {
    public static void handleMobBeam(MobBeamPacket packet) {
        var client = MinecraftClient.getInstance();
        MobBeamTracker.handle(packet, client.world);
    }
}
