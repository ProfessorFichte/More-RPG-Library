package net.more_rpg_classes.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.more_rpg_classes.network.MobBeamPacket;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side tracker for active mob beam spells.
 */
@Environment(EnvType.CLIENT)
public class MobBeamTracker {

    public record ActiveBeam(int targetId, Spell.Target.Beam beam, float range) {}

    private static final Map<Integer, ActiveBeam> activeBeams = new HashMap<>();

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MobBeamPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.spellId() == null) {
                    activeBeams.remove(payload.casterId());
                    return;
                }
                var world = context.client().world;
                if (world == null) return;
                var entry = SpellRegistry.from(world).getEntry(payload.spellId()).orElse(null);
                if (entry == null) return;
                Spell spell = entry.value();
                if (spell.target == null || spell.target.beam == null) return;
                activeBeams.put(payload.casterId(), new ActiveBeam(payload.targetId(), spell.target.beam, spell.range));
            });
        });
    }

    @Nullable
    public static ActiveBeam get(int entityId) {
        return activeBeams.get(entityId);
    }

    public static void clear() {
        activeBeams.clear();
    }
}
