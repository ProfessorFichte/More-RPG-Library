package net.more_rpg_classes.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.*;
import net.more_rpg_classes.client.effect.*;
import net.more_rpg_classes.client.particle.*;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.spell_engine.api.effect.CustomModelStatusEffect;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.render.CustomModels;
import net.spell_engine.client.particle.SpellExplosionParticle;
import net.spell_engine.client.particle.SpellFlameParticle;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MoreRPGClassesClient implements ClientModInitializer {

    public void  onInitializeClient(){
        CustomModels.registerModelIds(List.of(
                FrozenSolidRenderer.modelId
        ));

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
        ParticleFactoryRegistry.getInstance().register(MoreParticles.GAS_CLOUD, CloudParticle.CloudFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.POISON_SMOKE, ExplosionSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.DRAGON_CLAW, ClawParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.FREEZING_SNOWFLAKE, SnowflakeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WATER_DROP, RainSplashParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.SLASH_CLAW, ClawParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.ICE_TRAP, IceTrapParticle.IceTrapParticleFactory::new);

        CustomParticleStatusEffect.register(MRPGCEffects.MOLTEN_ARMOR.effect, new MoltenArmorParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.BLEEDING.effect, new BleedingParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.FROSTED.effect, new FrostedParticles(10));
        CustomModelStatusEffect.register(MRPGCEffects.FROZEN_SOLID.effect, new FrozenSolidRenderer());
        CustomParticleStatusEffect.register(MRPGCEffects.SOAKED.effect, new SoakedParticles(2));

    }
}
