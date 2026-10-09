package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.entity.ISpellCasterEntity;
import com.mrpg_lib.entity.SpellCasterState;
import com.mrpg_lib.entity.TestSpellCasters;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public final class SpellCastTestGoals {
    private SpellCastTestGoals() {
    }

    public static Goal create(MobEntity mob, String spellId) {
        TestSpellCasters.mark(mob);
        SpellCasterState state = new SpellCasterState(mob);
        ISpellCasterEntity caster = new ISpellCasterEntity() {
            @Override
            public void startSpellCast(int ticks) {
                state.start(ticks);
            }

            @Override
            public void stopSpellCast() {
                state.stop();
            }

            @Override
            public boolean isCastingSpell() {
                return state.isCasting();
            }

            @Override
            public MobEntity asMobEntity() {
                return mob;
            }
        };
        return new MobSpellCastGoal(caster, spellId).withAnimations(true);
    }
}
