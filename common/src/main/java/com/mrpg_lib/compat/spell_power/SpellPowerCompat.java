package com.mrpg_lib.compat.spell_power;

import com.mrpg_lib.compat.CompatHooks;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.critical_strike.CriticalStrikeCompat;

public final class SpellPowerCompat {
    private SpellPowerCompat() {
    }

    public static void init() {
        CompatHooks.setFuse(SpellPowerFuse::apply);
        CompatHooks.setHighestSpellSchoolPower(SpellPowerMethods::getHighestSpellSchoolPower);
        CompatHooks.setSchoolPower(SpellPowerSchoolLookup::getSpellPower);
        MoreSpellSchools.initialize();
        if (MrpgCompat.CRITICAL_STRIKE) {
            CriticalStrikeCompat.init();
        }
    }
}
