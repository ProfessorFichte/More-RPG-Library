package com.mrpg_lib.compat.spell_engine.client.render;

import com.mrpg_lib.compat.spell_engine.network.MobSpinPacket;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class MobSpinTracker {
    private record Spin(float degreesPerTick, int startAge, int durationTicks) {}

    private static final Map<Integer, Spin> SPINS = new HashMap<>();

    private MobSpinTracker() {
    }

    public static void handle(MobSpinPacket packet, @Nullable World world) {
        if (packet.degreesPerTick() == 0 || world == null) {
            SPINS.remove(packet.casterId());
            return;
        }
        Entity entity = world.getEntityById(packet.casterId());
        if (entity == null) return;
        SPINS.put(packet.casterId(), new Spin(packet.degreesPerTick(), entity.age, packet.durationTicks()));
    }

    public static float degrees(Entity entity, float delta) {
        if (SPINS.isEmpty()) return 0F;
        Spin spin = SPINS.get(entity.getId());
        if (spin == null) return 0F;
        int ticks = entity.age - spin.startAge;
        if (entity.isRemoved() || ticks < 0 || ticks > spin.durationTicks) {
            SPINS.remove(entity.getId());
            return 0F;
        }
        return spin.degreesPerTick * (ticks + delta);
    }

    public static void clear() {
        SPINS.clear();
    }
}
