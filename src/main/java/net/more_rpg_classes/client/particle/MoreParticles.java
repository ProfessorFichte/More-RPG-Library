package net.more_rpg_classes.client.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class MoreParticles {
    public static final DefaultParticleType STUN_PAR = FabricParticleTypes.simple();
    public static final DefaultParticleType BLOOD_DROP = FabricParticleTypes.simple();
    public static final DefaultParticleType MOLTEN_ARMOR= FabricParticleTypes.simple();
    public static final DefaultParticleType BUBBLE = FabricParticleTypes.simple();
    public static final DefaultParticleType BUBBLE_POP = FabricParticleTypes.simple();
    public static final DefaultParticleType WATER_MIST = FabricParticleTypes.simple();
    public static final DefaultParticleType SPLASH = FabricParticleTypes.simple();
    public static final DefaultParticleType BIG_SPLASH = FabricParticleTypes.simple();
    public static final DefaultParticleType WAVE = FabricParticleTypes.simple();
    public static final DefaultParticleType DRIPPING_WATER = FabricParticleTypes.simple();
    public static final DefaultParticleType HOT_SPLASH = FabricParticleTypes.simple();
    public static final DefaultParticleType WATER_WHIP = FabricParticleTypes.simple();
    public static final DefaultParticleType WATER_CIRCLE = FabricParticleTypes.simple();
    public static final DefaultParticleType WATER_HEAL = FabricParticleTypes.simple();
    public static final DefaultParticleType WATER_SPLASH = FabricParticleTypes.simple();
    public static final DefaultParticleType STONE_EXPLOSION = FabricParticleTypes.simple();
    public static final DefaultParticleType STONE_PARTICLE = FabricParticleTypes.simple();
    public static final DefaultParticleType WIND_VACUUM = FabricParticleTypes.simple();
    public static final DefaultParticleType SMALL_GUST = FabricParticleTypes.simple();
    public static final DefaultParticleType GUST = FabricParticleTypes.simple();
    public static final DefaultParticleType GAS_CLOUD = FabricParticleTypes.simple();
    public static final DefaultParticleType POISON_SMOKE = FabricParticleTypes.simple();
    public static final DefaultParticleType DRAGON_CLAW = FabricParticleTypes.simple();
    public static final DefaultParticleType FREEZING_SNOWFLAKE = FabricParticleTypes.simple();


    public static void register(){
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "stun_particle"), STUN_PAR);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "blood_drop"), BLOOD_DROP);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "molten_armor"), MOLTEN_ARMOR);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "bubble"), BUBBLE);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "bubble_pop"), BUBBLE_POP);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "water_mist"), WATER_MIST);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "splash"), SPLASH);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "big_splash"), BIG_SPLASH);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "wave"), WAVE);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "dripping_water"), DRIPPING_WATER);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "hot_splash"), HOT_SPLASH);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "water_whip"), WATER_WHIP);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "water_circle"), WATER_CIRCLE);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "water_heal"), WATER_HEAL);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "water_splash"), WATER_SPLASH);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "stone_explosion"), STONE_EXPLOSION);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "stone_particle"), STONE_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "wind_vacuum"), WIND_VACUUM);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "small_gust"), SMALL_GUST);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "gust"), GUST);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "gas_cloud"), GAS_CLOUD);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "poison_smoke"), POISON_SMOKE);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "dragon_claw"), DRAGON_CLAW);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "freezing_snowflake"), FREEZING_SNOWFLAKE);
    }

}
