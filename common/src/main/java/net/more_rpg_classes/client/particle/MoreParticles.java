package net.more_rpg_classes.client.particle;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;
import net.spell_engine.client.particle.TemplateParticleType;

public class MoreParticles {
    public static final SimpleParticleType BLOOD_DROP = FabricParticleTypes.simple();
    public static final SimpleParticleType MOLTEN_ARMOR= FabricParticleTypes.simple();
    public static final SimpleParticleType BUBBLE = FabricParticleTypes.simple();
    public static final SimpleParticleType BUBBLE_POP = FabricParticleTypes.simple();
    public static final SimpleParticleType WATER_MIST = FabricParticleTypes.simple();
    public static final SimpleParticleType SPLASH = FabricParticleTypes.simple();
    public static final SimpleParticleType BIG_SPLASH = FabricParticleTypes.simple();
    public static final SimpleParticleType WAVE = FabricParticleTypes.simple();
    public static final SimpleParticleType DRIPPING_WATER = FabricParticleTypes.simple();
    public static final SimpleParticleType HOT_SPLASH = FabricParticleTypes.simple();
    public static final SimpleParticleType WATER_WHIP = FabricParticleTypes.simple();
    public static final SimpleParticleType WATER_CIRCLE = FabricParticleTypes.simple();
    public static final SimpleParticleType WATER_HEAL = FabricParticleTypes.simple();
    public static final SimpleParticleType WATER_SPLASH = FabricParticleTypes.simple();
    public static final SimpleParticleType STONE_EXPLOSION = FabricParticleTypes.simple();
    public static final SimpleParticleType STONE_PARTICLE = FabricParticleTypes.simple();
    public static final SimpleParticleType WIND_VACUUM = FabricParticleTypes.simple();
    public static final SimpleParticleType SMALL_GUST = FabricParticleTypes.simple();
    public static final SimpleParticleType DRAGON_CLAW = FabricParticleTypes.simple();
    public static final SimpleParticleType FREEZING_SNOWFLAKE = FabricParticleTypes.simple();
    public static final SimpleParticleType WATER_DROP = FabricParticleTypes.simple();
    public static final SimpleParticleType SLASH_CLAW = FabricParticleTypes.simple();
    public static final SimpleParticleType ICE_TRAP = FabricParticleTypes.simple();
    public static final SimpleParticleType STONE_TRAP = FabricParticleTypes.simple();
    public static final SimpleParticleType LEAF = FabricParticleTypes.simple();
    public static final SimpleParticleType FATAL_POISON = FabricParticleTypes.simple();
    public static final SimpleParticleType RAGE_PAR = FabricParticleTypes.simple();
    public static final SimpleParticleType SMALL_THUNDER = FabricParticleTypes.simple();
    public static final SimpleParticleType RAINBOW_MUSIC_NOTE_0 = FabricParticleTypes.simple();
    public static final SimpleParticleType RAINBOW_MUSIC_NOTE_1 = FabricParticleTypes.simple();
    public static final TemplateParticleType MUSIC_NOTE = new TemplateParticleType();
    public static final TemplateParticleType STAR = new TemplateParticleType();
    public static ParticleType<PopupParticleEffect> POPUP;

    public static void register(){
        POPUP = Registry.register(
            Registries.PARTICLE_TYPE,
            Identifier.of(MRPGCMod.MOD_ID, "popup"),
            new ParticleType<PopupParticleEffect>(false) {
                @Override
                public MapCodec<PopupParticleEffect> getCodec() {
                    return PopupParticleEffect.createCodec(this);
                }
                @Override
                public PacketCodec<? super RegistryByteBuf, PopupParticleEffect> getPacketCodec() {
                    return PopupParticleEffect.createPacketCodec(this);
                }
            }
        );
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "blood_drop"), BLOOD_DROP);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "molten_armor"), MOLTEN_ARMOR);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "bubble"), BUBBLE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "bubble_pop"), BUBBLE_POP);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "water_mist"), WATER_MIST);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "splash"), SPLASH);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "big_splash"), BIG_SPLASH);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "wave"), WAVE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "dripping_water"), DRIPPING_WATER);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "hot_splash"), HOT_SPLASH);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "water_whip"), WATER_WHIP);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "water_circle"), WATER_CIRCLE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "water_heal"), WATER_HEAL);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "water_splash"), WATER_SPLASH);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "stone_explosion"), STONE_EXPLOSION);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "stone_particle"), STONE_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "wind_vacuum"), WIND_VACUUM);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "small_gust"), SMALL_GUST);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "dragon_claw"), DRAGON_CLAW);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "freezing_snowflake"), FREEZING_SNOWFLAKE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "water_drop"), WATER_DROP);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "slash_claw"), SLASH_CLAW);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "ice_trap"), ICE_TRAP);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "stone_trap"), STONE_TRAP);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "leaf"), LEAF);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "fatal_poison"), FATAL_POISON);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "rage_particle"), RAGE_PAR);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "small_thunder"), SMALL_THUNDER);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "rainbow_music_note_0"), RAINBOW_MUSIC_NOTE_0);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "rainbow_music_note_1"), RAINBOW_MUSIC_NOTE_1);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "music_note"), MUSIC_NOTE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MRPGCMod.MOD_ID, "star"), STAR);
    }

}
