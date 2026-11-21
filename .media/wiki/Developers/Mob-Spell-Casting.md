# Mob Spell Casting

Needs Spell Engine installed.

`com.mrpg_lib.compat.spell_engine.MobSpellCastGoal` lets any mob cast Spell Engine spells.
Delivery type, particles, sounds, channeling and cooldown all come from the spell JSON, so there is nothing to configure by hand.

**Setup**

1. Implement `ISpellCasterEntity`
```java
public class MyWizard extends HostileEntity implements ISpellCasterEntity {

    private boolean isCasting = false;
    private int castingTicks = 0;

    @Override public void startSpellCast(int ticks) { isCasting = true;  castingTicks = ticks; }
    @Override public void stopSpellCast()           { isCasting = false; castingTicks = 0;     }
    @Override public boolean isMobCasting()        { return isCasting; }
    @Override public MobEntity asMobEntity()        { return this; }
}
```
2. Add the goals in `initGoals()`.
The spell argument is a plain spell ID string or a `#`-prefixed tag. Passing the shared `spellGoals` list prevents two goals from firing at the same time.
```java
private final List<MobSpellCastGoal> spellGoals = new ArrayList<>();

@Override
protected void initGoals() {
    MobSpellCastGoal fireball  = new MobSpellCastGoal(this, "mymod:fireball",  spellGoals);
    MobSpellCastGoal frostbolt = new MobSpellCastGoal(this, "mymod:frostbolt", spellGoals);

    this.goalSelector.add(1, fireball);
    this.goalSelector.add(2, frostbolt);

    spellGoals.add(fireball);
    spellGoals.add(frostbolt);
}
```
To pick randomly from a group of spells, pass a tag instead of a single ID:
```java
new MobSpellCastGoal(this, "#mymod:mob/wizard_spells", spellGoals)
```
The mob picks one available (off-cooldown) spell from the tag each cast.
The goal tracks cooldowns against world time itself, you don't need to call anything. `updateCooldown()` still exists but does nothing.

---

**Custom timings (optional)**

By default the goal uses the spell's own cast/channel duration and cooldown. To give a mob its own values, use `withTimings`:

```java
MobSpellCastGoal fireball = new MobSpellCastGoal(this, "mymod:fireball", spellGoals)
        .withTimings(SpellTimings.builder().castSeconds(1.2F).cooldownSeconds(6F).build());

MobSpellCastGoal beam = new MobSpellCastGoal(this, "#mymod:mob/wizard_spells", spellGoals)
        .withTimings(SpellTimings.cooldown(4F))
        .withTimings("mymod:death_ray", SpellTimings.channel(3F));
```

- `withTimings(timings)` applies to every spell of the goal, `withTimings(spellId, timings)` to one spell only. Per-spell wins over goal-wide, which wins over the spell's own value.
- `castSeconds` replaces the cast duration. For channel spells it is the total channel length, and `channelSeconds` (channel spells only) wins if both are set.
- `cooldownSeconds` replaces the cooldown. `0` means no cooldown.
- `channelTicks` (optional) changes how many times a channel spell releases. Default is the spell's own count, so damage per cast stays the same when you only change the duration.
- Anything you leave unset falls back to the spell. Cast time is never below 1 tick.

The overridden cast time is what `startSpellCast(int ticks)` receives.
If you play your own fixed-length animation, it will not stretch to fit by itself, so loop it or scale it from that tick count.
The spell's own animations (`withAnimations(true)`, see [section 10](Mob-Animations)) do follow the overridden cast time.

---

**What works automatically**

| Feature | Driven by |
|---|---|
| Projectile, Meteor, Cloud, Direct, Area, Beam, Teleport | `spell.json` delivery + target type |
| Custom deliveries (`deliver.type: CUSTOM`) | the handler registered with Spell Engine's `SpellHandlers.registerCustomDelivery`, see **Custom deliveries** below |
| Delivery delay | `spell.deliver.delay` |
| Arrow spells (`SHOOT_ARROW`, `AFFECT_ARROW`) and stash spells (`STASH_EFFECT`) | see **Arrow, stash and summon spells** below |
| Melee weapon skills (`MELEE`) and melee-range spells (`range_mechanic: MELEE`) | see **Melee spells** below |
| Area impacts (`area_impact`) of direct spells | `spell.area_impact` |
| Cast duration, channeling & charging (timed like for players: a spell with a cast time goes off at the end of the cast, a channel spell releases every interval starting half an interval in, and each release deals its share of the spell, not the full amount) | `spell.active.cast.duration` / `type` (`CHANNEL` / `CHARGE`) |
| Cast start sound & particles | `spell.active.cast.start_sound` / `particles` |
| Release particles & sound (for channel spells once at the end, or on every release with `channel.release_fx`) | `spell.release.particles` / `sound` |
| Impact handling | SpellEngine internals |
| Cooldown | `spell.cost.cooldown.duration` (or `withTimings`) |
| `particles_scaled_with_ranged` | scaled by `spell.range` automatically |

---

**Healing spells**

Spells whose **entire** impact list is `HEAL` are cast on the nearest wounded ally instead of the combat target.

Spells that mix `HEAL` with damage or other impacts target the combat target normally. Spell Engine's relation system prevents the heal from applying to enemies.

---

**Charge spells**

For a spell authored with `spell.active.cast.type = CHARGE`, the mob charges like a player holding the key: the charge fills over the cast duration and the mob lets go when it has charged enough.
- If the charge adds range (`charge.bonus.range_add`, e.g. Venom Cask), the mob charges just enough for the spell to reach its target, and starts the cast when the target is within the fully charged range.
- Otherwise it charges fully, unless the target is within 3 blocks, then it releases at `min_release_ratio`.
- It never releases below `min_release_ratio`.

---

**Arrow, stash and summon spells**

- `SHOOT_ARROW` spells (e.g. Barrage) fire real arrows, including the extra arrows of the spell, with the spell's arrow perks and on-hit impacts. The mob needs a combat target and something in its main hand (any item, a bow is the natural choice), otherwise the spell is not picked. Mobs don't need arrows in their inventory.
- `AFFECT_ARROW` spells change the mob's next arrow fired within 5 seconds.
- `STASH_EFFECT` spells (e.g. Power Shot) put the stash effect on the mob just like on a player, and the stashed spell is only used when its trigger fires. Mobs support `ARROW_SHOT` stashes: skeletons, strays, bogged, and any mob shooting through a bow or crossbow (pillagers, piglins, arrow spells) use up the stash and carry its spell on the arrow. A self-targeted stash spell is only cast while the mob has a combat target. Other trigger types (melee hit, damage taken, ...) are still player-only in Spell Engine.
- Summons (e.g. Spirit Wolf) treat the casting mob as their owner: they follow it, defend it, attack what it attacks and are scaled from its attributes. Summons of a mob using `ISpellCasterEntity` don't attack other mobs unless provoked, the same way the caster itself treats them.

---

**Melee spells**

- Spell Engine runs `MELEE` weapon skills (e.g. Swift Strikes, Swipe) on the player's client, so for mobs the goal runs them on the server instead. It uses the same data: every attack in `deliver.melee.attacks` with its delay, extra strikes, hitbox (length, width, height, roll, arc), `forward_momentum`, `movement_slipperiness`, `damage_bonus`, animation, swing and impact sounds. Each hit is a normal mob attack (`tryAttack`, so the mob needs an attack damage attribute), followed by the spell's own impacts.
- Reach is Spell Engine's melee range for non-players (3 blocks plus `spell.range`). The mob walks to its target before swinging, and only swings once the target is in reach (plus the dash distance for skills with `forward_momentum`). If it can't get there within 2 seconds it gives up without going on cooldown.
- Attack length follows the mob's attack speed attribute, or a sword-like 1.6 attacks per second if the mob has none.
- Spells with `range_mechanic: MELEE` (e.g. Ground Slam) are only started when the target is within that reach, and the mob moves towards the target instead of backing away while casting. If the target escapes before the release, an area impact still lands in front of the mob, like a player's missed slam.
- Area impacts of direct spells hit everything around the target that the caster may hurt (for a hostile caster: players, villagers and other non-hostile mobs, never other hostile mobs), with the area's particles and sound.
- Area spells with melee range (e.g. Whirlwind) use the same 3 block reach. While channeling, the mob keeps walking into its target and every release hits everything around it that it may hurt.

---

**Custom deliveries**

Spells with `deliver.type: CUSTOM` run the same handler as for players. The content mod registers it once at startup, for example `SpellHandlers.registerCustomDelivery(Identifier.of("mymod", "my_delivery"), (world, spellEntry, caster, targets, context, targetLocation) -> { ... })`, and sets `spell.deliver.custom.handler = "mymod:my_delivery"`. Nothing extra is needed for mobs, but the handler gets the mob as `caster`, so it must not assume a player.
- Targets are found like for players, from where the mob looks (it faces its target before the release). For `AIM` spells the combat target is used if the look ray misses.
- Spells with target type `NONE` are cast too when they use a custom delivery, while the mob has a target in range.
- Spells with target type `FROM_TRIGGER` are never picked, they only run from a trigger.

---

**Intelligent Spellcasting**

Enable situational spell selection per goal by calling `.withIntelligentSpellcasting()`:
```java
MobSpellCastGoal fireball = new MobSpellCastGoal(this, "mymod:fireball", spellGoals)
        .withIntelligentSpellcasting();
```

When enabled, before casting the goal runs these filters in order:
- **Self-heal priority**: if the caster is below 50% HP and has a pure heal spell available, only heal spells are considered.
- **Ally-heal priority**: if an ally is below 60% HP and a heal spell is available, only heal spells are considered.
- **Kill priority**: if the target is below 30% HP, pure heal spells are skipped so the mob focuses on finishing the target.
- **`SpellBehaviorRegistry`**: any custom per-spell condition registered externally (see below).

---

**SpellBehaviorRegistry**

Register custom cast conditions for specific spells. Conditions registered here are checked when intelligent spellcasting is active:
```java
SpellBehaviorRegistry.register(
    Identifier.of("mymod", "updraft"),
    (caster, target, spellId) -> target != null && !target.isOnGround()
);
```

The condition receives the caster, the current target (nullable) and the spell ID. Return `false` to skip this spell this tick, the goal moves on to the next available spell.

For a distance gate there is a ready-made condition. For example, only throw Venom Cask when the target is 6 to 14 blocks away:
```java
SpellBehaviorRegistry.register(
    Identifier.of("archers_expansion", "venom_cask"),
    SpellBehaviorRegistry.targetDistance(6.0, 14.0)
);
```

---

**Equipment and passive-tag spells**

Off by default, so existing mobs keep casting exactly as before. Turn either on per goal:
```java
MobSpellCastGoal fireball = new MobSpellCastGoal(this, "mymod:fireball", spellGoals)
        .withEquipmentSpells(true)
        .withPassiveSpellTag("#mymod:mob/wizard_passives");
```
- `withEquipmentSpells(true)` reads the caster's mainhand item for passive and modifier spells, the same way a player wielding it would benefit from them.
- `withPassiveSpellTag(tag)` grants passive spells straight from a spell tag, independent of any weapon. Same idea as the spell tag you already pass in as the mob's castable spells, just for passives.
- Call `resolvedPassiveSpells(world)` to read everything currently granted from either source.

A modifier spell that matches the spell being cast extends its range (`range_add`).
The rest of what a modifier can do on a player (changing the impact, boosting power, projectile perks) is not applied to mobs yet, because Spell Engine's own modifier resolution only works for `PlayerEntity` casters.
Passive spells are resolved and exposed the same way, but their triggers (on-hit, periodic) are not auto-dispatched for the same reason.

---

**Beam spells**

A mob doesn't hit you with a beam just because it targets you. The beam follows the target with a limited turn speed, 1.5 degrees per tick by default.
The mob turns with its beam, so you can outrun both by moving sideways, especially up close. It only hits entities it really touches and it stops at blocks.
```java
new MobSpellCastGoal(this, "wizards:arcane_beam", spellGoals).withBeamTurnSpeed(5.0F);
```
- A higher value makes the beam harder to dodge, `0` snaps it onto the target every tick like before.

---

**Weapon skills**

`withWeaponSkills(true)` lets the mob use the active spells of the weapon in its main hand, the same skills a player gets from it: Swift Strikes on swords, Cleave on axes, Impale on spears, and so on.
```java
MobSpellCastGoal skills = MobSpellCastGoal.forWeaponSkills(this, spellGoals);
this.goalSelector.add(1, skills);
spellGoals.add(skills);

MobSpellCastGoal mixed = new MobSpellCastGoal(this, "#mymod:mob/knight_spells", spellGoals)
        .withWeaponSkills(true);
```
- The weapon is read every time the goal picks a spell, so a mob that gets another weapon (by a modpack maker, a command or your code) uses that weapon's skills from then on. Without a weapon, or with one that has no skills, the goal just uses its own spells.
- Weapon skills come first: when one of them can be used, the goal picks it over its other spells. `withWeaponSkills(true, false)` puts them in the same random pick as the rest.
- Only active spells count. Passive and modifier spells of the weapon stay with `withEquipmentSpells(true)`.
- Melee skills (`MELEE` delivery) work like described under **Melee spells**: the mob walks up, swings and hits everything in the arc. The mob needs an attack damage attribute.
- If the weapon leaves the mob's hand while a skill is being used, the skill stops.
- Weapons with a spell choice pool (the ones where a player picks one skill out of a list, like the Wizards staffs or the Bard lutes) work too. The mob gets one random active spell from that pool. By default it's picked once and the mob keeps it, also after a world reload. With `.withWeaponSpellChoice(MobSpellCastGoal.WeaponSpellChoice.PER_CAST)` a new one is picked after every cast of it.
- Cooldowns are per skill and per goal. Turn this on for one goal of a mob only, otherwise each goal uses the skill on its own cooldown.
- `forWeaponSkills(caster, otherGoals)` is a goal that only uses weapon skills. `resolvedWeaponSkills(world)` lists the skills of the current weapon.

Give the goal a better priority than the mob's normal melee goal (for example the Better Combat goal from [section 13](Better-Combat-and-Combat-Roll-for-Mobs)), then the mob uses a skill whenever it is ready and attacks normally in between.

---

**Mobs with their own cast animation**

For mobs that don't hold a real weapon or staff (for example a mage with a magic orb) and have their own cast animation, use `withCustomCasting`:
```java
MobSpellCastGoal spells = new MobSpellCastGoal(this, "#mymod:mob/orb_mage_spells", spellGoals)
        .withCustomCasting(new CustomCasting() {
            @Override
            public int castTicks(MobEntity mob, Identifier spellId, int defaultTicks) {
                return 30;
            }

            @Override
            public void onCastStart(MobEntity mob, Identifier spellId, int castTicks) {
                startMyCastAnimation(spellId);
            }

            @Override
            public void onCastStop(MobEntity mob, Identifier spellId) {
                stopMyCastAnimation();
            }
        });
```
- No Spell Engine player animation is played (cast, release and melee skill animations), even with `withAnimations(true)`. Spinning spells still spin the mob if `withAnimations(true)` is set.
- `castTicks` decides how long every cast takes. `defaultTicks` is what the goal would use without it (the spell's own duration or your `withTimings`), so you can also scale it. `CustomCasting.ticks(30)` is a fixed length for every spell.
- Every spell goes off at the end of that time. Channel spells release evenly over it and charge spells charge over it.
- `onCastStart` runs right after `startSpellCast`, `onRelease` every time the spell goes off (on every release of a channel spell), `onCastStop` when the goal stops. They run on the server, so send the animation to the clients yourself (tracked data, a packet, or your animation library's server trigger).
- Spell particles and sounds still play.
