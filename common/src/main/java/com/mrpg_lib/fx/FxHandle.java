package com.mrpg_lib.fx;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public final class FxHandle {
    public static final FxHandle NONE = new FxHandle(0, List.of());

    private final int id;
    private final List<ServerPlayerEntity> recipients;

    FxHandle(int id, List<ServerPlayerEntity> recipients) {
        this.id = id;
        this.recipients = recipients;
    }

    public int id() {
        return id;
    }

    public void stop() {
        if (id == 0) return;
        var payload = new FxStopPayload(id);
        for (var player : recipients) {
            if (!player.isDisconnected()) {
                MrpgFx.send(player, payload);
            }
        }
    }
}
