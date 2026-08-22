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
import net.spell_engine.fx.SpellEngineParticles;
import net.spell_engine.fx.SpellEngineParticles.Entry;
import net.spell_engine.fx.SpellEngineParticles.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/// Registry of More RPG Classes' own particles.
///
/// Ported from the V1 hand-written particle classes to Spell Engine 1.10's single
/// generic factory: each entry is a texture plus the defaults of the particle drawn
/// from it, and [net.spell_engine.client.particle.SpellParticle] resolves entry
/// defaults + per-spawn `ParticleGroup.Appearance` payload into the final look.
///
/// This is what makes colour / scale / motion set on a `ParticleGroup` actually apply
/// to these ids — a vanilla or hand-written factory ignores the payload entirely.
///
/// Sprites still come from `assets/more_rpg_classes/particles/<name>.json`; the
/// [Texture] frame count here only drives the entry's natural lifetime.
///
/// ### Lifetime
/// An animated entry lives for its frame count, stretched by `playbackSpeed`:
/// `effective maxAge = frames / playback_speed`. Each entry below reproduces the
/// V1 `maxAge` that way — the number in the comment is the V1 value.
///
/// ### Scale
/// V1 mixed two conventions and they do *not* mean the same thing: `this.scale = X`
/// was absolute, while `this.scale(X)` *multiplied* vanilla's random `0.1..0.2` base
/// (mean `0.15`). V2 `scale` is absolute, so the latter ports as `0.15 * X` with the
/// base's own +/-33% as variance. A factory that calls neither leaves the particle at
/// that `0.1..0.2` base, so it ports as `scale(0.15F, 0.33F)` — that is what every
/// `FlameParticle.Factory` entry below carries.
///
/// ### Standing still
/// Several V1 factories here threw their spawn velocity away: `ExplosionLargeParticle`
/// passes `0,0,0` to `super` and its `tick()` never calls `move()`, so a batch's authored
/// speed was dead code and the effect sat on its spawn point for its whole life.
/// `Appearance` has no "ignore spawn velocity" switch, so those entries carry `drag(0F)`:
/// the particle moves for a single tick and then stops dead. The same class is also
/// full-bright (`getBrightness` is a constant), which is `glow`'s default — so those
/// entries must not set `glow(false)`. Mirrors Forcemaster's `Particles`, which ports
/// the identical V1 classes.
public class MoreParticles {

    private static final List<Entry> entries = new ArrayList<>();

    /// Every entry owned by this mod. Registered here (server + client) and bound to
    /// the generic factory per platform — see `MoreRPGClassesClient`.
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

    // MARK: - Liquid

    /// V1 vanilla `RainSplashParticle`. Ground-splash behaviour is not reproducible
    /// as a generic billboard — this is a falling droplet with the same gravity.
    public static final Entry BLOOD_DROP = add("blood_drop", 1, 20, p -> p
            .motion(Motion.DRIFT).glow(false).scale(0.11F, 0.33F)
            .gravity(0.8F).collides(true));
    public static final Entry WATER_DROP = add("water_drop", 1, 20, p -> p
            .motion(Motion.DRIFT).glow(false).scale(0.11F, 0.33F)
            .gravity(0.8F).collides(true));
    public static final Entry DRIPPING_WATER = add("dripping_water", 4, p -> p
            .motion(Motion.DRIFT).glow(false).scale(0.11F, 0.33F)
            .gravity(0.8F).collides(true).playbackSpeed(0.2F));      // V1 maxAge 20
    public static final Entry BUBBLE = add("bubble", 6, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.3F));                                    // V1 maxAge 20
    public static final Entry BUBBLE_POP = add("bubble_pop", 5, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.42F));                                   // V1 maxAge 12
    public static final Entry SPLASH = add("splash", 4, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.33F));                                   // V1 maxAge 12
    /// Same vanilla `FlameParticle.Factory` as [#SPLASH] and [#HOT_SPLASH], which never
    /// calls `scale()` — so despite the name it drew at the identical `0.1..0.2` base.
    public static final Entry BIG_SPLASH = add("big_splash", 4, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.33F));                                   // V1 maxAge 12
    public static final Entry HOT_SPLASH = add("hot_splash", 4, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.33F));                                   // V1 maxAge 12
    public static final Entry WATER_MIST = add("water_mist", 10, p -> p
            .glow(false).scale(0.3F, 0.33F).opacity(0.8F).drag(0.8F).collides(true)
            .playbackSpeed(0.5F));                                    // V1 maxAge 20
    /// V1 vanilla `FishingParticle`; its bobber-specific motion has no V2 analogue.
    public static final Entry WAVE = add("wave", 8, p -> p
            .glow(false).scale(0.3F, 0.33F).drag(0.9F).playbackSpeed(0.5F));  // V1 maxAge 16
    /// V1 Spell Engine `SpellExplosionParticle` (`ExplosionLargeParticle`): stood still,
    /// full-bright via its `getBrightness` of `255`. See *Standing still* above.
    public static final Entry WATER_SPLASH = add("water_splash", 9, p -> p
            .scale(0.8F).drag(0F).playbackSpeed(0.75F));              // V1 maxAge 12
    /// V1 `AbstractParticle.WaterHealingFactory`: `SpellFlameParticle` + `0x7affff` + randomDarken.
    public static final Entry WATER_HEAL = add("water_heal", 1, 20, p -> p
            .render(Render.LIT).color(Color.from(0x7affff).toRGBA()).colorVariance(0.65F)
            .scale(0.15F, 0.33F).drag(0.96F).collides(true));
    /// V1 `VerticalSlashParticle` hand-rolled `buildGeometry` for an upright quad —
    /// now `Facing.UPRIGHT`. V1 scale was absolute `0.7`.
    public static final Entry WATER_WHIP = add("water_whip", 7, p -> p
            .facing(Facing.UPRIGHT).scale(0.7F).playbackSpeed(1.4F));  // V1 maxAge 5
    /// V1 `CircleGroundParticle` hand-rolled `buildGeometry` for a ground quad —
    /// now `Facing.GROUND`. V1 did `scale *= 5` on the `0.1..0.2` base.
    public static final Entry WATER_CIRCLE = add("water_circle", 7, p -> p
            .facing(Facing.GROUND).scale(0.75F, 0.33F).playbackSpeed(0.78F));  // V1 maxAge 8..10

    // MARK: - Earth

    /// V1 `CustomSpellExplosionParticle` (vanilla `ExplosionLargeParticle` re-sheeted to
    /// `PARTICLE_SHEET_TRANSLUCENT`, the default here): absolute scale `0.8`, `rgb` forced
    /// to `1` so vanilla's random darkening is gone, and full-bright via the inherited
    /// `getBrightness`. Stood still — see *Standing still* above.
    public static final Entry STONE_EXPLOSION = add("stone_explosion", 5, p -> p
            .scale(0.8F).drag(0F).playbackSpeed(0.42F));               // V1 maxAge 12
    /// V1 `AbstractParticle.HolyFactory`: `SpellFlameParticle` + `Color.HOLY` + randomDarken.
    public static final Entry STONE_PARTICLE = add("stone_particle", 3, p -> p
            .render(Render.LIT).color(Color.HOLY.toRGBA()).colorVariance(0.65F)
            .scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.15F));                                   // V1 maxAge 20
    /// V1 `StoneTrapParticle` (`ShiftedParticle`): grew `+0.01`/tick over 60 ticks,
    /// `0.4 -> 1.0` — expressed as a 2.5x linear `scaleMultiplier`.
    public static final Entry STONE_TRAP = add("stone_trap", 4, p -> p
            .scale(0.4F).scaleMultiplier(2.5F).gravity(0.225F).drag(1.0F).collides(true)
            .playbackSpeed(0.067F));                                  // V1 maxAge 60
    public static final Entry LEAF = add("leaf", 12, p -> p
            .glow(false).scale(0.15F, 0.33F).drag(0.8F).gravity(-0.5F).collides(true)
            .playbackSpeed(0.6F));                                    // V1 maxAge 20

    // MARK: - Air

    /// Same V1 `CustomSpellExplosionParticle` as [#STONE_EXPLOSION] — stood still,
    /// full-bright, absolute scale `0.8`. See *Standing still* above.
    public static final Entry WIND_VACUUM = add("wind_vacuum", 8, p -> p
            .scale(0.8F).drag(0F).playbackSpeed(0.67F));               // V1 maxAge 12
    public static final Entry SMALL_GUST = add("small_gust", 7, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.58F));                                   // V1 maxAge 12
    /// V1 shipped the full 12-frame `gust` sprite sheet and its particle json but never
    /// registered the type, so `more_rpg_classes:gust` spawned nothing. Registered here with
    /// its sibling SMALL_GUST's character at the larger sprite's scale, ~20 tick life.
    public static final Entry GUST = add("gust", 12, p -> p
            .render(Render.LIT).scale(0.4F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.6F));
    /// V1 `SmallThunderParticle` (vanilla `ExplosionLargeParticle`): absolute scale `1.75`,
    /// and its own `getBrightness` of `255` — exactly what `glow`'s default resolves to.
    /// Stood still, hence `drag(0F)` — see *Standing still* above.
    public static final Entry SMALL_THUNDER = add("small_thunder", 7, p -> p
            .render(Render.LIT).scale(1.75F).drag(0F).playbackSpeed(0.88F));  // V1 maxAge 8

    // MARK: - Frost

    public static final Entry FREEZING_SNOWFLAKE = add("freezing_snowflake", 1, 20, p -> p
            .motion(Motion.DRIFT).scale(0.15F, 0.33F).color(Color.from(0xccffff).toRGBA())
            .opacity(0.75F).collides(true).gravity(0.2475F));
    /// V1 `IceTrapParticle` (`ShiftedParticle`): same growth as `STONE_TRAP`.
    public static final Entry ICE_TRAP = add("ice_trap", 12, p -> p
            .scale(0.4F).scaleMultiplier(2.5F).gravity(0.225F).drag(1.0F).collides(true)
            .playbackSpeed(0.2F));                                    // V1 maxAge 60

    // MARK: - Physical

    /// V1 `ClawParticle` (vanilla `ExplosionLargeParticle`): absolute scale `1.3`,
    /// full-bright via the inherited `getBrightness`. Stood still — see *Standing still*.
    public static final Entry DRAGON_CLAW = add("dragon_claw", 7, p -> p
            .render(Render.LIT).scale(1.3F).drag(0F).playbackSpeed(0.44F));   // V1 maxAge 16
    /// Same V1 `ClawParticle.Factory` as [#DRAGON_CLAW].
    public static final Entry SLASH_CLAW = add("slash_claw", 7, p -> p
            .render(Render.LIT).scale(1.3F).drag(0F).playbackSpeed(0.44F));   // V1 maxAge 16
    /// V1 `AbstractParticle.HolyFactory` used `minecraft:lava` sprites.
    public static final Entry MOLTEN_ARMOR = add("molten_armor", 1, 20, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).gravity(0.8F).collides(true));
    /// V1 `AbstractParticle.AnimatedFlameFactory`.
    public static final Entry FATAL_POISON = add("fatal_poison", 7, p -> p
            .render(Render.LIT).scale(0.15F, 0.33F).drag(0.96F).collides(true)
            .playbackSpeed(0.35F));                                   // V1 maxAge 20
    /// V1 `RageParticle`: `scale(1.5F)` multiplied the `0.1..0.2` base -> `~0.225`.
    public static final Entry RAGE_PAR = add("rage_particle", 3, p -> p
            .render(Render.LIT).scale(0.225F, 0.33F).gravity(0.001F)
            .playbackSpeed(0.094F));                                  // V1 maxAge 25..40

    // MARK: - Signs

    /// Stays an [Entry] so `ParticleGroupBuilder.of(MUSIC_NOTE)` and its per-song `.color(...)`
    /// keep working, but it is bound to [MusicNoteParticle.Factory] rather than the generic
    /// one — see *Kept on their own factories* below and `MoreRPGClassesClient`.
    ///
    /// Deliberately carries **no** appearance defaults: the class owns lifetime (40..60),
    /// size (`scale(1.25F)` on vanilla's `0.1..0.2` base), motion and the tail fade. What it
    /// does read from a spawn payload — tint, opacity, a scale multiplier — must stay at its
    /// neutral value here for that read to be an identity, exactly as in V1 where the
    /// `TemplateParticleEffect.Appearance` defaults were an untinted `scale = 1`.
    public static final Entry MUSIC_NOTE = add("music_note", 8, p -> {});
    /// [MusicNoteParticle]'s twin, and bound to [StarParticle.Factory] for the same reason.
    /// Carries no appearance defaults so that the tint / opacity / scale a call site does
    /// author stay identity-composed on top of the class's own values — see [#MUSIC_NOTE].
    public static final Entry STAR = add("star", 1, 65, p -> {});

    // MARK: - Kept on their own factories
    // Behaviour Appearance cannot express: RAINBOW_MUSIC_NOTE cycles hue per tick,
    // MUSIC_NOTE and STAR launch on a fixed rise the batch cannot override (both stay
    // Entries so their colour payload survives - see [#MUSIC_NOTE]), and the popups are a
    // custom ParticleEffect carrying their own payload.

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
