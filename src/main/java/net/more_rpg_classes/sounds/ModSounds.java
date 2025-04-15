package net.more_rpg_classes.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class ModSounds {
    public static final Identifier ICICLE_CRASH_ID = Identifier.of(MOD_ID, "icicle_crash");
    public static SoundEvent ICICLE_CRASH_EVENT = SoundEvent.of(ICICLE_CRASH_ID);
    public static final Identifier STRONG_ATTACK_ID = Identifier.of(MOD_ID, "strong_attack");
    public static SoundEvent STRONG_ATTACK_EVENT = SoundEvent.of(STRONG_ATTACK_ID);
    public static final Identifier CRIPPLING_STRIKE_ID = Identifier.of(MOD_ID, "crippling_strike");
    public static SoundEvent CRIPPLING_STRIKE_EVENT = SoundEvent.of(CRIPPLING_STRIKE_ID);
    public static final Identifier WATER_MAGIC_IMPACT_1_ID = Identifier.of(MOD_ID, "water_magic_impact1");
    public static SoundEvent WATER_MAGIC_IMPACT_1_EVENT = SoundEvent.of(WATER_MAGIC_IMPACT_1_ID);
    public static final Identifier EARTH_MAGIC_IMPACT_1_ID = Identifier.of(MOD_ID, "earth_magic_impact1");
    public static SoundEvent EARTH_MAGIC_IMPACT_1_EVENT = SoundEvent.of(EARTH_MAGIC_IMPACT_1_ID);
    public static final Identifier EARTH_MAGIC_IMPACT_2_ID = Identifier.of(MOD_ID, "earth_magic_impact2");
    public static SoundEvent EARTH_MAGIC_IMPACT_2_EVENT = SoundEvent.of(EARTH_MAGIC_IMPACT_2_ID);
    public static final Identifier AIR_MAGIC_IMPACT_1_ID = Identifier.of(MOD_ID, "air_magic_impact1");
    public static SoundEvent AIR_MAGIC_IMPACT_1_EVENT = SoundEvent.of(AIR_MAGIC_IMPACT_1_ID);
    public static final Identifier AIR_MAGIC_IMPACT_2_ID = Identifier.of(MOD_ID, "air_magic_impact2");
    public static SoundEvent AIR_MAGIC_IMPACT_2_EVENT = SoundEvent.of(AIR_MAGIC_IMPACT_2_ID);
    public static final Identifier AIR_MAGIC_IMPACT_3_ID = Identifier.of(MOD_ID, "air_magic_impact3");
    public static SoundEvent AIR_MAGIC_IMPACT_3_EVENT = SoundEvent.of(AIR_MAGIC_IMPACT_3_ID);
    public static final Identifier AIR_MAGIC_CAST_1_ID = Identifier.of(MOD_ID, "air_magic_cast1");
    public static SoundEvent AIR_MAGIC_CAST_1_EVENT = SoundEvent.of(AIR_MAGIC_CAST_1_ID);
    public static final Identifier EARTH_MAGIC_CAST_1_ID = Identifier.of(MOD_ID, "earth_magic_cast1");
    public static SoundEvent EARTH_MAGIC_CAST_1_EVENT = SoundEvent.of(EARTH_MAGIC_CAST_1_ID);
    public static final Identifier AIR_EXPLOSION_ID = Identifier.of(MOD_ID, "air_explosion");
    public static SoundEvent AIR_EXPLOSION = SoundEvent.of(AIR_EXPLOSION_ID);
    public static final Identifier FROST_CRACKLE_ID = Identifier.of(MOD_ID, "frost_crackle");
    public static SoundEvent FROST_CRACKLE = SoundEvent.of(FROST_CRACKLE_ID);
    public static final Identifier ARCANE_STRONG_IMPACT_ID = Identifier.of(MOD_ID, "arcane_strong_impact");
    public static SoundEvent ARCANE_STRONG_IMPACT = SoundEvent.of(ARCANE_STRONG_IMPACT_ID);
    public static final Identifier HOLY_RELEASE_ID = Identifier.of(MOD_ID, "holy_release");
    public static SoundEvent HOLY_RELEASE = SoundEvent.of(HOLY_RELEASE_ID);
    public static final Identifier FROST_CRACKLE_LONG_ID = Identifier.of(MOD_ID, "frost_crackle_long");
    public static SoundEvent FROST_CRACKLE_LONG = SoundEvent.of(FROST_CRACKLE_LONG_ID);

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, ICICLE_CRASH_ID, ICICLE_CRASH_EVENT);
        Registry.register(Registries.SOUND_EVENT, STRONG_ATTACK_ID, STRONG_ATTACK_EVENT);
        Registry.register(Registries.SOUND_EVENT, CRIPPLING_STRIKE_ID, CRIPPLING_STRIKE_EVENT);
        Registry.register(Registries.SOUND_EVENT, WATER_MAGIC_IMPACT_1_ID, WATER_MAGIC_IMPACT_1_EVENT);
        Registry.register(Registries.SOUND_EVENT, EARTH_MAGIC_IMPACT_1_ID, EARTH_MAGIC_IMPACT_1_EVENT);
        Registry.register(Registries.SOUND_EVENT, EARTH_MAGIC_IMPACT_2_ID, EARTH_MAGIC_IMPACT_2_EVENT);
        Registry.register(Registries.SOUND_EVENT, AIR_MAGIC_IMPACT_1_ID, AIR_MAGIC_IMPACT_1_EVENT);
        Registry.register(Registries.SOUND_EVENT, AIR_MAGIC_IMPACT_2_ID, AIR_MAGIC_IMPACT_2_EVENT);
        Registry.register(Registries.SOUND_EVENT, AIR_MAGIC_IMPACT_3_ID, AIR_MAGIC_IMPACT_3_EVENT);
        Registry.register(Registries.SOUND_EVENT, AIR_MAGIC_CAST_1_ID, AIR_MAGIC_CAST_1_EVENT);
        Registry.register(Registries.SOUND_EVENT, EARTH_MAGIC_CAST_1_ID, EARTH_MAGIC_CAST_1_EVENT);
        Registry.register(Registries.SOUND_EVENT, AIR_EXPLOSION_ID, AIR_EXPLOSION);
        Registry.register(Registries.SOUND_EVENT, FROST_CRACKLE_ID, FROST_CRACKLE);
        Registry.register(Registries.SOUND_EVENT, ARCANE_STRONG_IMPACT_ID, ARCANE_STRONG_IMPACT);
        Registry.register(Registries.SOUND_EVENT, HOLY_RELEASE_ID, HOLY_RELEASE);
        Registry.register(Registries.SOUND_EVENT, FROST_CRACKLE_LONG_ID, FROST_CRACKLE_LONG);
    }


}