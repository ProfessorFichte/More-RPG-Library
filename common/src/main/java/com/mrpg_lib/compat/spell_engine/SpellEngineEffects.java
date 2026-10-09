package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.effect.EffectEntry;
import com.mrpg_lib.effect.MRPGCEffects;
import net.spell_engine.api.effect.ActionImpairing;
import net.spell_engine.api.effect.EntityActionsAllowed;
import net.spell_engine.api.effect.RemoveOnHit;
import net.spell_engine.api.effect.Synchronized;

import java.util.List;

public final class SpellEngineEffects {
    private SpellEngineEffects() {
    }

    public static void configure(List<EffectEntry> entries) {
        for (var entry : entries) {
            Synchronized.configure(entry.effect, true);
        }

        RemoveOnHit.configure(MRPGCEffects.FROZEN_SOLID.effect, RemoveOnHit.Trigger.DIRECT_HIT, 1, 1);

        ActionImpairing.configure(MRPGCEffects.FROZEN_SOLID.effect, MRPGCActionImpairing.FROZEN);
        ActionImpairing.configure(MRPGCEffects.IGNITED.effect, MRPGCActionImpairing.IGNITED);
        ActionImpairing.configure(MRPGCEffects.FEAR.effect, EntityActionsAllowed.INCAPACITATE);
        ActionImpairing.configure(MRPGCEffects.STAGGER.effect, EntityActionsAllowed.INCAPACITATE);
    }
}
