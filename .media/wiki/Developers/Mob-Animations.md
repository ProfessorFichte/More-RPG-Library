# Mob Animations (playerAnimator bridge)

Needs [playerAnimator](https://modrinth.com/mod/playeranimator). Without it, every call below quietly does nothing and none of its classes are loaded.

Lets mobs play playerAnimator animations, the same `player_animations/*.json` files Spell Engine and Better Combat use for players.

**How it works.**
The server sends a small packet (`mrpg_lib:mob_animation`) to the players tracking the mob.
The client keeps a playerAnimator animation stack on the entity and applies it to the mob's model right after vanilla poses it, so armor and held items follow.
Players are never touched, playerAnimator and Spell Engine keep handling those.
Players who start tracking a mob mid-animation get `persistent` animations replayed from the start.

**Server-safe API** (`com.mrpg_lib.compat.player_animator.api`, safe to call from goals even if playerAnimator is missing):
```java
MobAnimations.play(mob, Identifier.of("spell_engine", "one_handed_projectile_charge"));

MobAnimations.play(mob, animationId, MobAnimationOptions.DEFAULT
        .withLayer(MobAnimationLayer.CASTING)
        .withSpeed(1.5F)
        .withFadeTicks(4)
        .withMirror(MobAnimationOptions.Mirror.AUTO)
        .withPersistent(true)
        .withDuration(12));

MobAnimations.stop(mob, MobAnimationLayer.CASTING, 5);
MobAnimations.stopAll(mob, 5);
MobAnimations.playSpellAnimation(mob, MobAnimationLayer.RELEASE, "spell_engine:one_handed_projectile_release", 1F);
```
- `withLayer`: `OFF_HAND_POSE` < `POSE` < `MISC` < `ATTACK` < `CASTING` < `RELEASE` < `DODGE`, higher layers win. Default is `MISC`.
- `withFadeTicks`: `-1` uses the animation's own begin tick.
- `withMirror(AUTO)` mirrors the animation for left-handed mobs.
- `withPersistent(true)` replays the animation for players who start tracking the mob later, until it is stopped.
- `withDuration(ticks)` (optional) stretches the animation to last that long, float lengths work too.
- `withAttackTiming(length, upswingRate, upswingMultiplier)` plays an animation with Better Combat's attack speed curve instead of a constant speed: fast during the upswing, slower afterwards, so the hit frame lines up with the moment the damage lands (`length` in ticks, `upswingRate` and `upswingMultiplier` as Better Combat computes them). The Better Combat melee goal ([section 11](Better-Combat-and-Combat-Roll-for-Mobs)) uses it.

`animationId` is `<namespace>:<"name" field of the json>`, not the file name. Use `MobAnimations.isAvailable()` if you need to know whether playerAnimator is there.

**Spell casting mobs.** `MobSpellCastGoal` can play the spell's own animations: `.withAnimations(true)` plays `spell.active.cast.animation` while casting and `spell.release.animation` on release.
It is off by default so existing mobs look exactly as before. The cast animation speed follows an overridden cast time (`withTimings`).
Spells with `animation_spin` (e.g. Whirlwind) also spin the whole mob while casting, like players (this part works without playerAnimator).

**Custom models.** Models extending `BipedEntityModel` (zombie, skeleton, piglin, player-style models), `IllagerEntityModel` (vindicator, evoker, illusioner) and `VillagerResemblingModel` (villager, wandering trader, witch) work out of the box. For any other model, register an adapter on the client that maps your model parts to the animation bones:
```java
MobModelAdapters.register(new MobModelAdapter() {
    @Override
    public MobModelParts parts(EntityModel<?> model) {
        if (!(model instanceof MyMobModel m)) return null;
        return new MobModelParts(m.head, m.body, m.leftArm, m.rightArm, m.leftLeg, m.rightLeg);
    }
});
```
`prepare(model)` runs after vanilla posing and before the animation (fix visibility flags there), `finish(model)` runs after (copy transforms to overlay parts there). The adapter never sees playerAnimator types, so it keeps working if the bridge is later moved to another animation library.

**Limits.** Illager arms are shown uncrossed while an animation is active. Villagers and witches only have one crossed-arms part, so it follows the right arm of the animation (rotation and offset on top of its normal pose) and the left arm is ignored. The pitch adjustment Spell Engine applies to player casts is not used. Bend (elbow/knee) only works on biped models.

**Test command** (development environment only, op level 2): `/mrpg_anim <targets> <animation id> [speed]` and `/mrpg_anim <targets> stop`, for example `/mrpg_anim @e[type=zombie] spell_engine:one_handed_projectile_charge`.
