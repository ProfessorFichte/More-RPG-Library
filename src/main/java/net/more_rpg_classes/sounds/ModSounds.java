package net.more_rpg_classes.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;

public class ModSounds {
    public static final Identifier ICICLE_SPELL_ID = new Identifier(MRPGCMod.MOD_ID, "icicle_crash");
    public static SoundEvent ICICLE_SPELL_EVENT= SoundEvent.of(ICICLE_SPELL_ID);
    public static final Identifier STRONG_ATTACK_ID = new Identifier(MRPGCMod.MOD_ID, "strong_attack");
    public static SoundEvent STRONG_ATTACK_EVENT= SoundEvent.of(STRONG_ATTACK_ID);
    public static final Identifier CRIPPLING_STRIKE_ID = new Identifier(MRPGCMod.MOD_ID, "crippling_strike");
    public static SoundEvent CRIPPLING_STRIKE_EVENT= SoundEvent.of(CRIPPLING_STRIKE_ID);
    public static final Identifier SLAHS_IMPACT_ID = new Identifier(MRPGCMod.MOD_ID, "slash_impact");
    public static SoundEvent SLAHS_IMPACT_EVENT= SoundEvent.of(SLAHS_IMPACT_ID);
    public static final Identifier WATER_IMPACT1_ID = new Identifier(MRPGCMod.MOD_ID, "water_magic_impact1");
    public static SoundEvent WATER_IMPACT1= SoundEvent.of(WATER_IMPACT1_ID);
    public static final Identifier EARTH_IMPACT1_ID = new Identifier(MRPGCMod.MOD_ID, "earth_magic_impact1");
    public static SoundEvent EARTH_IMPACT1= SoundEvent.of(EARTH_IMPACT1_ID);
    public static final Identifier EARTH_IMPACT2_ID = new Identifier(MRPGCMod.MOD_ID, "earth_magic_impact2");
    public static SoundEvent EARTH_IMPACT2= SoundEvent.of(EARTH_IMPACT2_ID);
    public static final Identifier EARTH_CAST1_ID = new Identifier(MRPGCMod.MOD_ID, "earth_magic_cast1");
    public static SoundEvent EARTH_CAST1= SoundEvent.of(EARTH_CAST1_ID);
    public static final Identifier AIR_CAST1_ID = new Identifier(MRPGCMod.MOD_ID, "air_magic_cast1");
    public static SoundEvent AIR_CAST1= SoundEvent.of(AIR_CAST1_ID);
    public static final Identifier AIR_IMPACT1_ID = new Identifier(MRPGCMod.MOD_ID, "air_magic_impact1");
    public static SoundEvent AIR_IMPACT1= SoundEvent.of(AIR_IMPACT1_ID);
    public static final Identifier AIR_IMPACT2_ID = new Identifier(MRPGCMod.MOD_ID, "air_magic_impact2");
    public static SoundEvent AIR_IMPACT2= SoundEvent.of(AIR_IMPACT2_ID);
    public static final Identifier AIR_IMPACT3_ID = new Identifier(MRPGCMod.MOD_ID, "air_magic_impact3");
    public static SoundEvent AIR_IMPACT3= SoundEvent.of(AIR_IMPACT3_ID);
    public static final Identifier AIR_EXPLOSION_ID = new Identifier(MRPGCMod.MOD_ID, "air_explosion");
    public static SoundEvent AIR_EXPLOSION = SoundEvent.of(AIR_EXPLOSION_ID);
    public static final Identifier FROST_CRACKLE_ID = new Identifier(MRPGCMod.MOD_ID, "frost_crackle");
    public static SoundEvent FROST_CRACKLE = SoundEvent.of(FROST_CRACKLE_ID);
    public static final Identifier ARCANE_STRONG_IMPACT_ID = new Identifier(MRPGCMod.MOD_ID, "arcane_strong_impact");
    public static SoundEvent ARCANE_STRONG_IMPACT = SoundEvent.of(ARCANE_STRONG_IMPACT_ID);
    public static final Identifier HOLY_RELEASE_ID = new Identifier(MRPGCMod.MOD_ID, "holy_release");
    public static SoundEvent HOLY_RELEASE = SoundEvent.of(HOLY_RELEASE_ID);
    public static final Identifier FROST_CRACKLE_LONG_ID = new Identifier(MRPGCMod.MOD_ID, "frost_crackle_long");
    public static SoundEvent FROST_CRACKLE_LONG = SoundEvent.of(FROST_CRACKLE_LONG_ID);

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, ICICLE_SPELL_ID, ICICLE_SPELL_EVENT);
        Registry.register(Registries.SOUND_EVENT, STRONG_ATTACK_ID, STRONG_ATTACK_EVENT);
        Registry.register(Registries.SOUND_EVENT, CRIPPLING_STRIKE_ID, CRIPPLING_STRIKE_EVENT);
        Registry.register(Registries.SOUND_EVENT, SLAHS_IMPACT_ID, SLAHS_IMPACT_EVENT);
        Registry.register(Registries.SOUND_EVENT, WATER_IMPACT1_ID, WATER_IMPACT1);
        Registry.register(Registries.SOUND_EVENT, EARTH_IMPACT1_ID, EARTH_IMPACT1);
        Registry.register(Registries.SOUND_EVENT, EARTH_IMPACT2_ID, EARTH_IMPACT2);
        Registry.register(Registries.SOUND_EVENT, EARTH_CAST1_ID, EARTH_CAST1);
        Registry.register(Registries.SOUND_EVENT, AIR_CAST1_ID, AIR_CAST1);
        Registry.register(Registries.SOUND_EVENT, AIR_IMPACT1_ID, AIR_IMPACT1);
        Registry.register(Registries.SOUND_EVENT, AIR_IMPACT2_ID, AIR_IMPACT2);
        Registry.register(Registries.SOUND_EVENT, AIR_IMPACT3_ID, AIR_IMPACT3);
        Registry.register(Registries.SOUND_EVENT, AIR_EXPLOSION_ID, AIR_EXPLOSION);
        Registry.register(Registries.SOUND_EVENT, FROST_CRACKLE_ID, FROST_CRACKLE);
        Registry.register(Registries.SOUND_EVENT, ARCANE_STRONG_IMPACT_ID, ARCANE_STRONG_IMPACT);
        Registry.register(Registries.SOUND_EVENT, HOLY_RELEASE_ID, HOLY_RELEASE);
        Registry.register(Registries.SOUND_EVENT, FROST_CRACKLE_LONG_ID, FROST_CRACKLE_LONG);
    }
}