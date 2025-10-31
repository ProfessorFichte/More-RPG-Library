package com.mrpg_lib.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.more_rpg_classes.client.MoreRPGClassesClient;

public final class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MoreRPGClassesClient.init();
        MoreRPGClassesClient.registerParticleAppearances();
    }
}
