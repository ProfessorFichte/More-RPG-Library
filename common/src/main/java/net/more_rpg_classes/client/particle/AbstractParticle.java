package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.spell_engine.client.particle.SpellFlameParticle;
import net.spell_engine.client.util.Color;

public class AbstractParticle extends SpellFlameParticle {
    public AbstractParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
        super(clientWorld, d, e, f, g, h, i);
    }

    @Environment(EnvType.CLIENT)
    public static class WaterHealingFactory extends ConfiguredFactory {
        public WaterHealingFactory(SpriteProvider spriteProvider) {
            super(spriteProvider, config().color(Color.from(0x7affff)).randomDarken());
        }
    }

    @Environment(EnvType.CLIENT)
    public static class HolyFactory extends ConfiguredFactory {
        public HolyFactory(SpriteProvider spriteProvider) {
            super(spriteProvider, config().color(Color.HOLY).randomDarken());
        }
    }

    @Environment(EnvType.CLIENT)
    public static class AnimatedFlameFactory extends ConfiguredFactory {
        public AnimatedFlameFactory(SpriteProvider spriteProvider) {
            super(spriteProvider, config().animated());
        }
    }
}
