package com.mrpg_lib.compat;

import com.mrpg_lib.platform.MrpgPlatform;

public final class MrpgCompat {
    public static final boolean SPELL_ENGINE = MrpgPlatform.isModLoaded("spell_engine");
    public static final boolean SPELL_POWER = MrpgPlatform.isModLoaded("spell_power");
    public static final boolean RANGED_WEAPON_API = MrpgPlatform.isModLoaded("ranged_weapon_api");
    public static final boolean PLAYER_ANIMATOR = MrpgPlatform.isModLoaded("playeranimator");
    public static final boolean CRITICAL_STRIKE = MrpgPlatform.isModLoaded("critical_strike");
    public static final boolean RUNES = MrpgPlatform.isModLoaded("runes");
    public static final boolean BETTER_COMBAT = MrpgPlatform.isModLoaded("bettercombat");
    public static final boolean COMBAT_ROLL = MrpgPlatform.isModLoaded("combat_roll");

    private MrpgCompat() {
    }
}
