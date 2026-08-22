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
import net.spell_engine.api.spell.fx.ParticleGroup.Facing;
import net.spell_engine.api.spell.fx.ParticleGroup.Motion;
import net.spell_engine.api.spell.fx.ParticleGroup.Render;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.SpellEngineParticles.Entry;
import net.spell_engine.fx.SpellEngineParticles.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MoreParticles {

    private static final List<Entry> entries = new ArrayList<>();

    public static List<Entry> entries() {
        return entries;
    }

    private static Entry add(String name, int frames, Consumer<net.spell_engine.api.spell.fx.ParticleGroup.Appearance> defaults) {
        var entry = new Entry(Identifier.of(MRPGCMod.MOD_ID, name),
                new Texture(Identifier.of(MRPGCMod.MOD_ID, name), frames))
                .defaults(defaults);
        entries.add(entry);
        return entry;
    }

    private static Entry add(String name, int frames, int lifetime, Consumer<net.spell_engine.api.spell.fx.ParticleGroup.Appearance> defaults) {
        var entry = add(name, frames, defaults);
        entry.lifetime(lifetime);
        return entry;
    }

    public static final Entry BLOOD_DROP = add("blood_drop", 1, 20, p -> p
            .motion(Motion.DRIFT).glow(false).scale(0.11F, 0.33F)
            .gravity(0.8F).collides(true));
    public static final Entry WATER_DROP = add("water_drop", 1, 20, p -> p
            .motion(Motion.DRIFT).glow(false).scale(0.11F, 0.33F)
            .gravity(0.8F).collides(true));
    public static final Entry DRIPPING_WATER = add("dripping_water", 4, p -> p
            .motion(Motion.DRIFT).glow(false).scale(0.11F, 0.33F)
            .gravity(0.8F).collides(true).playbackSpeed(0.2F));
    public static final Entry BUBBLE = add("bubble", 6, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.3F));
    public static final Entry BUBBLE_POP = add("bubble_pop", 5, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.42F));
    public static final Entry SPLASH = add("splash", 4, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.33F));

    public static final Entry BIG_SPLASH = add("big_splash", 4, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.33F));
    public static final Entry HOT_SPLASH = add("hot_splash", 4, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.33F));
    public static final Entry WATER_MIST = add("water_mist", 10, p -> p
            .glow(false).scale(0.3F, 0.33F).opacity(0.8F).drag(0.8F).collides(true)
            .playbackSpeed(0.5F));
    public static final Entry WAVE = add("wave", 8, p -> p
            .glow(false).scale(0.3F, 0.33F).drag(0.9F).playbackSpeed(0.5F));

    public static final Entry WATER_SPLASH = add("water_splash", 9, p -> p
            .scale(0.8F).drag(0F).playbackSpeed(0.75F));
    public static final Entry WATER_HEAL = add("water_heal", 1, 20, p -> p
            .render(Render.LIT).color(Color.from(0x7affff).toRGBA()).colorVariance(0.65F)
            .scale(0.15F, 0.33F).drag(0.96F).collides(true));

    public static final Entry WATER_WHIP = add("water_whip", 7, p -> p
            .facing(Facing.UPRIGHT).scale(0.7F).playbackSpeed(1.4F));

    public static final Entry WATER_CIRCLE = add("water_circle", 7, p -> p
            .facing(Facing.GROUND).glow(false).drag(0F)
            .scale(0.87F, 0.39F)
            .playbackSpeed(0.46F).lifetimeVariance(0.76F));


    public static final Entry STONE_EXPLOSION = add("stone_explosion", 5, p -> p
            .scale(0.8F).drag(0F).playbackSpeed(0.42F));

    public static final Entry STONE_PARTICLE = add("stone_particle", 3, p -> p
            .render(Render.LIT).color(Color.HOLY.toRGBA()).colorVariance(0.65F)
            .scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.15F));

    public static final Entry STONE_TRAP = add("stone_trap", 4, p -> p
            .scale(0.4F).scaleMultiplier(2.5F).gravity(0.225F).drag(1.0F).collides(true)
            .playbackSpeed(0.067F));
    public static final Entry LEAF = add("leaf", 12, p -> p
            .glow(false).scale(0.15F, 0.33F).drag(0.8F).gravity(-0.5F).collides(true)
            .playbackSpeed(0.6F));

    public static final Entry WIND_VACUUM = add("wind_vacuum", 8, p -> p
            .scale(0.8F).drag(0F).playbackSpeed(0.67F));
    public static final Entry SMALL_GUST = add("small_gust", 7, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.58F));

    public static final Entry GUST = add("gust", 12, p -> p
            .render(Render.LIT).scale(0.4F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.6F));

    public static final Entry SMALL_THUNDER = add("small_thunder", 7, p -> p
            .render(Render.LIT).scale(1.75F).drag(0F).playbackSpeed(0.88F));


    public static final Entry FREEZING_SNOWFLAKE = add("freezing_snowflake", 1, 20, p -> p
            .motion(Motion.DRIFT).scale(0.15F, 0.33F).color(Color.from(0xccffff).toRGBA())
            .opacity(0.75F).collides(true).gravity(0.2475F));
    public static final Entry ICE_TRAP = add("ice_trap", 12, p -> p
            .scale(0.4F).scaleMultiplier(2.5F).gravity(0.225F).drag(1.0F).collides(true)
            .playbackSpeed(0.2F));

    public static final Entry DRAGON_CLAW = add("dragon_claw", 7, p -> p
            .render(Render.LIT).scale(1.3F).drag(0F).playbackSpeed(0.44F));
    public static final Entry SLASH_CLAW = add("slash_claw", 7, p -> p
            .render(Render.LIT).scale(1.3F).drag(0F).playbackSpeed(0.44F));
    public static final Entry MOLTEN_ARMOR = add("molten_armor", 1, 20, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).gravity(0.8F).collides(true));
    public static final Entry FATAL_POISON = add("fatal_poison", 7, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.35F));

    public static final Entry RAGE_PAR = add("rage_particle", 1, 14, p -> p
            .motion(Motion.BURST)
            .render(Render.OPAQUE).glow(false)
            .scale(0.1125F, 0.33F)
            .color(Color.from(0xE6E6E6).toRGBA()).colorVariance(0.33F)
            .lifetimeVariance(0.43F));

    public static final Entry MUSIC_NOTE = add("music_note", 8, p -> {});
    public static final Entry STAR = add("star", 1, 65, p -> {});


    public static final SimpleParticleType RAINBOW_MUSIC_NOTE = FabricParticleTypes.simple();
    public static ParticleType<PopupParticleEffect> POPUP;
    public static ParticleType<PopupParticleEffect> SPELL_STOLEN_POPUP;

    public static void register() {
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
        SPELL_STOLEN_POPUP = Registry.register(
                Registries.PARTICLE_TYPE,
                MRPGCMod.id("spell_stolen_popup"),
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
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of(MRPGCMod.MOD_ID, "rainbow_music_note"), RAINBOW_MUSIC_NOTE);

        for (var entry: entries) {
            Registry.register(Registries.PARTICLE_TYPE, entry.id(), entry.type());
        }
    }
}
