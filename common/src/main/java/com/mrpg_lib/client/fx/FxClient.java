package com.mrpg_lib.client.fx;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.config.FxClientConfig;
import com.mrpg_lib.fx.FxEffect;
import com.mrpg_lib.fx.FxParams;
import com.mrpg_lib.fx.FxPlayPayload;
import com.mrpg_lib.fx.FxStopPayload;
import com.mrpg_lib.fx.MrpgFx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.tiny_config.ConfigManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class FxClient {
    public static final ConfigManager<FxClientConfig> config = new ConfigManager<>("client_fx_v1", new FxClientConfig())
            .builder()
            .setDirectory(MRPGCMod.MOD_ID)
            .sanitize(true)
            .build();

    private static final List<Active> ACTIVE = new ArrayList<>();
    private static final List<Scheduled> SCHEDULED = new ArrayList<>();
    private static final Set<Identifier> UNKNOWN = new HashSet<>();
    private static ClientWorld lastWorld;

    private FxClient() {
    }

    private static final class Active {
        final FxEffect effect;
        final FxParams params;
        final int handleId;
        final Random random;
        final int duration;
        int age = 0;

        Active(FxEffect effect, FxParams params, int handleId) {
            this.effect = effect;
            this.params = params;
            this.handleId = handleId;
            this.random = Random.create(params.seed());
            this.duration = params.duration() > 0 ? params.duration() : effect.durationTicks();
            this.age = Math.min(params.startAge(), Math.max(0, this.duration - 1));
        }
    }

    private record Scheduled(int dueIn, Runnable action) {
        Scheduled next() {
            return new Scheduled(dueIn - 1, action);
        }
    }

    public static void init() {
        config.refresh();
        config.save();
        MrpgFx.setLocalPlayer((effect, params) -> play(effect, params, 0));
    }

    public static void handle(FxPlayPayload payload) {
        var effect = MrpgFx.get(payload.effect());
        if (effect == null) {
            if (UNKNOWN.add(payload.effect())) {
                MRPGCMod.LOGGER.warn("Unknown MRPG FX effect {}", payload.effect());
            }
            return;
        }
        play(effect, payload.params(), payload.handleId());
    }

    public static void handleStop(FxStopPayload payload) {
        ACTIVE.removeIf(active -> active.handleId == payload.handleId());
    }

    public static void play(FxEffect effect, FxParams params, int handleId) {
        var world = MinecraftClient.getInstance().world;
        if (world == null) return;
        var random = Random.create(params.seed());
        var soundPos = FxEmitter.soundPosition(world, params);
        for (var sound : effect.sounds()) {
            if (sound.delayTicks() <= 0) {
                FxEmitter.playSound(world, sound, soundPos, random);
            } else {
                schedule(sound.delayTicks(), () -> withWorld(w -> FxEmitter.playSound(w, sound, FxEmitter.soundPosition(w, params), random)));
            }
        }
        for (var child : effect.children()) {
            schedule(child.delayTicks(), () -> play(child.effect(), params, 0));
        }
        if (effect.timed()) {
            var active = new Active(effect, params, handleId);
            ACTIVE.add(active);
            emitTimed(world, active);
            return;
        }
        int budget = effect.maxParticles();
        for (var layer : effect.layers()) {
            if (layer.delayTicks() <= 0) {
                budget -= FxEmitter.emit(world, effect, layer, params, 0, 1.0F, random, budget, density());
            } else {
                schedule(layer.delayTicks(), () -> withWorld(w ->
                        FxEmitter.emit(w, effect, layer, params, 0, 1.0F, random, effect.maxParticles(), density())));
            }
        }
    }

    public static void tick(MinecraftClient client) {
        var world = client.world;
        if (world != lastWorld) {
            clear();
            lastWorld = world;
        }
        if (world == null || client.isPaused()) return;

        if (!SCHEDULED.isEmpty()) {
            var due = new ArrayList<Runnable>();
            for (int i = 0; i < SCHEDULED.size(); i++) {
                var next = SCHEDULED.get(i).next();
                if (next.dueIn() <= 0) {
                    due.add(next.action());
                    SCHEDULED.remove(i--);
                } else {
                    SCHEDULED.set(i, next);
                }
            }
            due.forEach(Runnable::run);
        }

        Iterator<Active> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            var active = iterator.next();
            active.age++;
            if (active.age >= active.duration
                    || active.effect.follow() && active.params.entityId() >= 0 && world.getEntityById(active.params.entityId()) == null) {
                iterator.remove();
                continue;
            }
            emitTimed(world, active);
        }
    }

    public static void clear() {
        ACTIVE.clear();
        SCHEDULED.clear();
    }

    private static void emitTimed(ClientWorld world, Active active) {
        var effect = active.effect;
        if (active.age % effect.intervalTicks() == 0) {
            float progress = MathHelper.clamp(active.age / (float) Math.max(1, active.duration), 0.0F, 1.0F);
            int budget = effect.maxParticles();
            for (var layer : effect.layers()) {
                if (active.age < layer.delayTicks()) continue;
                budget -= FxEmitter.emit(world, effect, layer, active.params, active.age, progress, active.random, budget, density());
            }
        }
        if (active.age > 0) {
            for (var sound : effect.sounds()) {
                if (sound.repeatTicks() > 0 && active.age % sound.repeatTicks() == 0) {
                    FxEmitter.playSound(world, sound, FxEmitter.soundPosition(world, active.params), active.random);
                }
            }
        }
    }

    private static double density() {
        return MathHelper.clamp(config.value.fx_density, 0.1, 3.0);
    }

    private static void schedule(int ticks, Runnable action) {
        SCHEDULED.add(new Scheduled(ticks, action));
    }

    private static void withWorld(java.util.function.Consumer<ClientWorld> action) {
        var world = MinecraftClient.getInstance().world;
        if (world != null) action.accept(world);
    }
}
