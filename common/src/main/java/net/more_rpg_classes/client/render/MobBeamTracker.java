package net.more_rpg_classes.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.World;
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

    public static void handle(MobBeamPacket payload, @Nullable World world) {
        if (payload.spellId() == null) {
            activeBeams.remove(payload.casterId());
            return;
        }
        if (world == null) return;
        var entry = SpellRegistry.from(world).getEntry(payload.spellId()).orElse(null);
        if (entry == null) return;
        Spell spell = entry.value();
        if (spell.target == null || spell.target.beam == null) return;
        activeBeams.put(payload.casterId(), new ActiveBeam(payload.targetId(), spell.target.beam, spell.range));
    }

    @Nullable
    public static ActiveBeam get(int entityId) {
        return activeBeams.get(entityId);
    }

    public static void clear() {
        activeBeams.clear();
    }
}
