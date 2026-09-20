package com.mrpg_lib.forge.network;

import net.minecraft.client.MinecraftClient;
import net.more_rpg_classes.client.render.MobBeamTracker;
import net.more_rpg_classes.network.MobBeamPacket;

public class MrpgForgeClientNetwork {
    public static void handleMobBeam(MobBeamPacket packet) {
        var client = MinecraftClient.getInstance();
        MobBeamTracker.handle(packet, client.world);
    }
}
