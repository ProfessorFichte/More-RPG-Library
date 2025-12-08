package net.more_rpg_classes.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.particle.*;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.client.effect.*;
import net.more_rpg_classes.client.particle.*;
import net.more_rpg_classes.client.render.FriendlyLightningEntityRenderer;
import net.more_rpg_classes.custom.MrpgLibSpells;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.entity.MRPGCEntities;
import net.spell_engine.api.effect.CustomModelStatusEffect;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.render.CustomModels;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_engine.client.particle.SpellExplosionParticle;
import net.spell_engine.client.particle.SpellFlameParticle;

import java.util.List;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class MoreRPGClassesClient{

    public static void  init(){
        for (var entry: MrpgLibSpells.entries) {
            if (entry.mutator() != null) {
                SpellTooltip.addDescriptionMutator(entry.id(), entry.mutator());
            }
        }
        // Register entity renderers
        EntityRendererRegistry.register(MRPGCEntities.FRIENDLY_LIGHTNING, FriendlyLightningEntityRenderer::new);

        CustomModels.registerModelIds(List.of(
                FrozenSolidRenderer.modelId,
                Identifier.of(MOD_ID, "projectile/falling_icicle")
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
        ParticleFactoryRegistry.getInstance().register(MoreParticles.DRAGON_CLAW, ClawParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.FREEZING_SNOWFLAKE, SnowflakeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.WATER_DROP, RainSplashParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.SLASH_CLAW, ClawParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.ICE_TRAP, IceTrapParticle.IceTrapParticleFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.STONE_TRAP, StoneTrapParticle.StoneTrapParticleFactory::new);
        ParticleFactoryRegistry.getInstance().register(MoreParticles.LEAF, LeafParticle.Factory::new);

        CustomParticleStatusEffect.register(MRPGCEffects.MOLTEN_ARMOR.effect, new MoltenArmorParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.BLEEDING.effect, new BleedingParticles(1));
        CustomParticleStatusEffect.register(MRPGCEffects.FROSTED.effect, new FrostedParticles(10));
        CustomModelStatusEffect.register(MRPGCEffects.FROZEN_SOLID.effect, new FrozenSolidRenderer());
        CustomParticleStatusEffect.register(MRPGCEffects.SOAKED.effect, new SoakedParticles(2));
    }
    public static void registerParticleAppearances() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();

        registry.register(MoreParticles.BLOOD_DROP, RainSplashParticle.Factory::new);
        registry.register(MoreParticles.MOLTEN_ARMOR, RainSplashParticle.Factory::new);
        registry.register(MoreParticles.BUBBLE, FlameParticle.Factory::new);
        registry.register(MoreParticles.BUBBLE_POP, FlameParticle.Factory::new);
        registry.register(MoreParticles.WATER_MIST, ExplosionSmokeParticle.Factory::new);
        registry.register(MoreParticles.SPLASH, FlameParticle.Factory::new);
        registry.register(MoreParticles.BIG_SPLASH, FlameParticle.Factory::new);
        registry.register(MoreParticles.WAVE, FishingParticle.Factory::new);
        registry.register(MoreParticles.DRIPPING_WATER, RainSplashParticle.Factory::new);
        registry.register(MoreParticles.HOT_SPLASH, FlameParticle.Factory::new);
        registry.register(MoreParticles.WATER_WHIP, VerticalSlashParticle.DefaultFactory::new);
        registry.register(MoreParticles.WATER_CIRCLE, CircleGroundParticle.DefaultFactory::new);
        registry.register(MoreParticles.WATER_HEAL, AbstractParticle.WaterHealingFactory::new);
        registry.register(MoreParticles.WATER_SPLASH, SpellExplosionParticle.Factory::new);
        registry.register(MoreParticles.STONE_EXPLOSION, CustomSpellExplosionParticle.Factory::new);
        registry.register(MoreParticles.STONE_PARTICLE, SpellFlameParticle.HolyFactory::new);
        registry.register(MoreParticles.WIND_VACUUM, CustomSpellExplosionParticle.Factory::new);
        registry.register(MoreParticles.SMALL_GUST,  FlameParticle.Factory::new);
        registry.register(MoreParticles.DRAGON_CLAW, ClawParticle.Factory::new);
        registry.register(MoreParticles.FREEZING_SNOWFLAKE, SnowflakeParticle.Factory::new);
        registry.register(MoreParticles.WATER_DROP, RainSplashParticle.Factory::new);
        registry.register(MoreParticles.SLASH_CLAW, ClawParticle.Factory::new);
        registry.register(MoreParticles.ICE_TRAP, IceTrapParticle.IceTrapParticleFactory::new);
        registry.register(MoreParticles.STONE_TRAP, StoneTrapParticle.StoneTrapParticleFactory::new);
        registry.register(MoreParticles.LEAF, LeafParticle.Factory::new);
        registry.register(MoreParticles.RAGE_PAR, DamageParticle.Factory::new);
        registry.register(MoreParticles.SMALL_THUNDER, SmallThunderParticle.Factory::new);
    }
}
