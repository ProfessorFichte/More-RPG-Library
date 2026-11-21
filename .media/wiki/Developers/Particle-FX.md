# Particle FX (MRPG FX)

`com.mrpg_lib.fx` plays particle and sound effects with one packet per effect. The client builds the shapes, so even large effects stay cheap on the network.

**Use a preset.** Presets are ready-made effects you adjust in one call: `EXPLOSION`, `BURST`, `SHOCKWAVE`, `AURA`, `SWIRL`, `BEAM`, `TRAIL`, `IMPACT`, `CAST`, `PILLAR`, `CLOUD`, `SLASH`, `STORM`.
```java
public static final FxEffect DRAGON_BURST = MrpgFx.register(Identifier.of("mymod", "dragon_burst"), FxPresets.EXPLOSION.tweak(t -> t
        .radius(5.0F)
        .color(0xB040F0, 0x300050)
        .speed(0.3F)
        .vanillaParticle(ParticleTypes.DRAGON_BREATH)
        .sound(SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.9F, 1.1F)));
```
Register effects in common code during mod init, so the server and the client both know them.

**Play it** on the server. Players within 64 blocks receive it:
```java
MrpgFx.play(serverWorld, DRAGON_BURST, FxParams.at(center).radius(5.0F));
MrpgFx.play(serverWorld, MY_AURA, FxParams.on(player));          // timed effects can follow an entity
FxHandle handle = MrpgFx.play(serverWorld, MY_CHANNEL, params);  // handle.stop() ends a timed effect early
```
`FxParams` can also carry a `direction`, a `target` (beams), `length`, `scale`, `intensity` (particle count multiplier), a `color` override for colored layers a `duration` override in ticks for timed effects and a `startAge` in ticks that starts a timed effect part way through (for example to replay it in another color without restarting it). A radius passed here replaces the effect's default radius, so an effect always matches the size of your ability.

**Build your own** from layers when no preset fits. A layer is a shape, how its particles move, and which particle it uses:
```java
public static final FxEffect FROST_NOVA = MrpgFx.register(Identifier.of("mymod", "frost_nova"), FxEffect.builder()
        .radius(3.0F)
        .layer(FxLayer.of(FxShape.SPHERE).radiusFromParams(0.4F).count(40)
                .velocity(FxVelocity.OUTWARD).speed(0.1F, 0.25F).speedFromRadius().particle(ParticleTypes.SNOWFLAKE))
        .layer(FxLayer.of(FxShape.RING).radiusFromParams(1.0F).count(32).anchor(FxAnchor.GROUND).dust(0x9FE0FF, 1.2F))
        .sound(SoundEvents.BLOCK_GLASS_BREAK, 0.9F, 0.6F)
        .build());
```
| Option | Values |
|---|---|
| Shapes | `POINT`, `SPHERE`, `SHELL`, `RING`, `DISC`, `ARC`, `CYLINDER`, `COLUMN`, `CONE`, `LINE`, `BEAM`, `HELIX`, `SPIRAL`, `BOX` |
| Velocity | `NONE`, `OUTWARD`, `INWARD`, `UP`, `DOWN`, `ALONG_AXIS`, `TANGENT` (swirl), `RANDOM` |
| Motion | `DEFAULT`, `STATIC`, `FLOAT`, `ASCEND`, `DRIFT`, `BURST` |
| Anchor | `POSITION`, `ENTITY` (with `verticalOrigin`), `EYES`, `GROUND`, `TARGET` |
| Orientation | `WORLD` (turns with the yaw only), `LOOK` (follows the direction), `AXIS` (the shape's up axis points along the direction) |

Effects can be timed (`duration`, `interval`, `follow()`), layers can be delayed, grow over time (`growFrom`) and spin (`spin`), sounds can be delayed or repeated, and effects can start other effects as `child` with a delay.

**Colored vanilla particles.** `ColoredParticles` has colorable copies of vanilla particles that use the vanilla textures: `EXPLOSION`, `EXPLOSION_EMITTER` (large explosion), `POOF`, `SMOKE`, `LARGE_SMOKE`, `CLOUD`, `END_ROD`, `FIREWORK`, `FLASH`, `WITCH`, `CRIT`, `ENCHANTED_HIT` and `SNOWFLAKE`. Use them in a layer with `.colored(ColoredParticles.END_ROD, 0xFF40C0)` or anywhere else as a particle effect with `ColoredParticles.END_ROD.of(0xFF40C0)` (an optional second value scales the particle). When a preset gets a `color`, it uses the colored copies of its own particles, and dust only for particles without a copy. Preset layers stay recolorable, so a `color` in `FxParams` recolors them when the effect is played.

**Particle lifetime.** `FxLayer.lifetime(ticks)` overrides how long the particles of that layer live. A timed effect with `interval(1)` and short lived particles draws a clean shape every tick, for example a ring that shrinks with `growFrom`.

**Floating bubble.** `MoreParticles.FLOATING_BUBBLE` is a bubble that floats up slowly and lives for 1.5 to 2.5 seconds. Unlike the vanilla bubble it does not need water.

**Testing.** In a development environment `/mrpg_fx play <effect> [radius] [scale] [intensity] [color]` plays an effect where you look, and `/mrpg_fx list` lists all effects. Every preset is registered as `mrpg_lib:preset/<name>` and every colored particle as `mrpg_lib:demo/colored_<name>` (white, recolor it with the color argument).

**Players** can lower or raise the amount of particles with `fx_density` (0.1 to 3) in `config/mrpg_lib/client_fx_v1.json`.
