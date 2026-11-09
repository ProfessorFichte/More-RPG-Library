package com.mrpg_lib.fx;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.client.particle.ColoredParticles;
import com.mrpg_lib.network.MRPGCNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public final class MrpgFx {
    public static final double VIEW_DISTANCE = 64.0;

    private static final Map<Identifier, FxEffect> REGISTRY = new LinkedHashMap<>();
    private static final AtomicInteger NEXT_HANDLE = new AtomicInteger(1);
    private static BiConsumer<FxEffect, FxParams> localPlayer = (effect, params) -> {
    };

    private MrpgFx() {
    }

    public static FxEffect register(Identifier id, FxEffect effect) {
        if (REGISTRY.putIfAbsent(id, effect) != null) {
            MRPGCMod.LOGGER.warn("Duplicate MRPG FX effect id {}, keeping the first one", id);
            return REGISTRY.get(id);
        }
        effect.assignId(id);
        return effect;
    }

    public static void registerBuiltins() {
        for (var preset : FxPresets.all()) {
            register(Identifier.of(MRPGCMod.MOD_ID, "preset/" + preset.name()), preset.create());
        }
        for (var type : ColoredParticles.all()) {
            int count = type == ColoredParticles.EXPLOSION_EMITTER ? 1 : type == ColoredParticles.FLASH ? 2 : type == ColoredParticles.EXPLOSION ? 6 : 24;
            register(Identifier.of(MRPGCMod.MOD_ID, "demo/" + type.name()), FxEffect.builder()
                    .layer(FxLayer.of(FxShape.SPHERE).radius(0.6F).count(count).velocity(FxVelocity.OUTWARD).speed(0.04F, 0.12F)
                            .colored(type, 0xFFFFFF))
                    .build());
        }
    }

    @Nullable
    public static FxEffect get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static Collection<Identifier> ids() {
        return Collections.unmodifiableCollection(REGISTRY.keySet());
    }

    public static FxHandle play(ServerWorld world, FxEffect effect, FxParams params) {
        var id = effect.id();
        if (id == null) {
            MRPGCMod.LOGGER.warn("MRPG FX effect played without being registered");
            return FxHandle.NONE;
        }
        var origin = params.origin();
        double maxDistance = VIEW_DISTANCE * VIEW_DISTANCE;
        List<ServerPlayerEntity> recipients = world.getPlayers(player -> player.squaredDistanceTo(origin) <= maxDistance);
        if (recipients.isEmpty()) return FxHandle.NONE;
        int handleId = effect.timed() ? NEXT_HANDLE.getAndIncrement() : 0;
        var payload = new FxPlayPayload(id, params, handleId);
        for (var player : recipients) {
            send(player, payload);
        }
        return handleId == 0 ? FxHandle.NONE : new FxHandle(handleId, List.copyOf(recipients));
    }

    public static void playLocal(FxEffect effect, FxParams params) {
        localPlayer.accept(effect, params);
    }

    public static void setLocalPlayer(BiConsumer<FxEffect, FxParams> player) {
        localPlayer = player;
    }

    static void send(ServerPlayerEntity player, CustomPayload payload) {
        MRPGCNetworking.sendToPlayer(player, payload);
    }
}
