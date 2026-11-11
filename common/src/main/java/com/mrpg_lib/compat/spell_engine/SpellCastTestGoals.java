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
        return new MobSpellCastGoal(caster(mob), spellId).withAnimations(true);
    }

    public static Goal createCustomCasting(MobEntity mob, String spellId, int castTicks) {
        return new MobSpellCastGoal(caster(mob), spellId).withAnimations(true).withCustomCasting(CustomCasting.ticks(castTicks));
    }

    public static Goal createWeaponSkills(MobEntity mob) {
        return MobSpellCastGoal.forWeaponSkills(caster(mob), null).withAnimations(true);
    }

    private static ISpellCasterEntity caster(MobEntity mob) {
        TestSpellCasters.mark(mob);
        SpellCasterState state = new SpellCasterState(mob);
        return new ISpellCasterEntity() {
            @Override
            public void startSpellCast(int ticks) {
                state.start(ticks);
            }

            @Override
            public void stopSpellCast() {
                state.stop();
            }

            @Override
            public boolean isMobCasting() {
                return state.isCasting();
            }

            @Override
            public MobEntity asMobEntity() {
                return mob;
            }
        };
    }
}
