package net.more_rpg_classes.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.*;
import net.more_rpg_classes.client.effect.BleedingParticles;
import net.more_rpg_classes.client.effect.FrostedParticles;
import net.more_rpg_classes.client.effect.FrozenSolidRenderer;
import net.more_rpg_classes.client.effect.MoltenArmorParticles;
import net.more_rpg_classes.client.particle.*;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.spell_engine.api.effect.CustomModelStatusEffect;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.render.CustomModels;
import net.spell_engine.api.render.StunParticleSpawner;
import net.spell_engine.client.particle.SpellExplosionParticle;
import net.spell_engine.client.particle.SpellFlameParticle;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MoreRPGClassesClient implements ClientModInitializer {

    public void  onInitializeClient(){
        CustomModels.registerModelIds(List.of(
                FrozenSolidRenderer.modelId
        ));


        ParticleFactoryRegistry.getInstance().register(MoreParticles.STUN_PAR, DamageParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.BLOOD_DROP, RainSplashParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MOLTEN_ARMOR, RainSplashParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.BUBBLE, FlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.BUBBLE_POP, FlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WATER_MIST, ExplosionSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.SPLASH, FlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.BIG_SPLASH, FlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WAVE, FishingParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.DRIPPING_WATER, RainSplashParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.HOT_SPLASH, FlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WATER_WHIP, VerticalSlashParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WATER_CIRCLE, CircleGroundParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WATER_HEAL, AbstractParticle.WaterHealingFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WATER_SPLASH, SpellExplosionParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.STONE_EXPLOSION, CustomSpellExplosionParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.STONE_PARTICLE, SpellFlameParticle.HolyFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WIND_VACUUM, CustomSpellExplosionParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.SMALL_GUST,  FlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.GUST, GustParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.GAS_CLOUD, CloudParticle.CloudFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.POISON_SMOKE, ExplosionSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.DRAGON_CLAW, DragonClawParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.FREEZING_SNOWFLAKE, SnowflakeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.RAGE_PAR, RageParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_WHITE, MusicNoteParticle.WhiteFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_RED, MusicNoteParticle.RedFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_PURPLE, MusicNoteParticle.PurpleFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_GREEN, MusicNoteParticle.GreenFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_YELLOW, MusicNoteParticle.YellowFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_BLUE, MusicNoteParticle.BlueFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_CYAN, MusicNoteParticle.CyanFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_GOLD, MusicNoteParticle.GoldFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_BRIGHT_GREEN, MusicNoteParticle.BrightGreenFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_YELLOW_GREEN, MusicNoteParticle.YellowGreenFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_MAGENTA, MusicNoteParticle.MagentaFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_BRIGHT_MAGENTA, MusicNoteParticle.BrightMagentaFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_MID_MAGENTA, MusicNoteParticle.MidMagentaFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_ARCANE, MusicNoteParticle.ArcaneFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.MUSIC_NOTE_RAGE, MusicNoteParticle.RageFactory::new);


        CustomParticleStatusEffect.register(MRPGCEffects.STUNNED, new StunParticleSpawner());
        CustomParticleStatusEffect.register(MRPGCEffects.MOLTEN_ARMOR, new MoltenArmorParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.BLEEDING, new BleedingParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.FROSTED, new FrostedParticles(5));
        CustomModelStatusEffect.register(MRPGCEffects.FROZEN_SOLID, new FrozenSolidRenderer());

    }
}
