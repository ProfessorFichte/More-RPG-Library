# Better Combat and Combat Roll for Mobs

Needs [Better Combat](https://modrinth.com/mod/better-combat) and [Combat Roll](https://modrinth.com/mod/combat-roll) respectively, both optional.
The factories below are safe to call from `initGoals()` whether the mod is installed or not, and nothing changes for mobs you don't give these goals to.
Nothing in the library adds these goals to mobs by itself.

**Better Combat melee** (`com.mrpg_lib.compat.better_combat.BetterCombatCompat`):
```java
goalSelector.add(2, BetterCombatCompat.createMeleeGoal(this, 1.0, false));
goalSelector.add(2, BetterCombatCompat.createMeleeGoal(this, 1.0, false,
        BetterCombatCompat.MeleeSettings.DEFAULT.withIntervalScale(1.5F)));
```
With Better Combat installed and a weapon that has Better Combat attributes in the main hand, the mob uses that weapon's combo the way a player would:
- Attacks cycle through the combo, and the combo resets when the mob stops swinging for a while.
- The attack animation and weapon pose play on the mob with the same speed curve Better Combat uses for players, and the hit lands after the weapon's upswing.
- Everything inside the weapon's hitbox is hit (not just the target), and each attack's damage multiplier is applied. Allies are never hit, except the mob's own current target: a hostile mob that was hit by another hostile mob (a skeleton's stray arrow, for example) can fight back. The same goes for spells cast by mob casters.
- The mob starts a swing once its target is within 80% of that attack's range, and moves slower while swinging (Better Combat's `movement_speed_while_attacking` times the attack's movement multiplier).
- It shows the weapon trail particles players get, and fast weapons deal reduced knockback like in Better Combat.
- If the mob's main-hand item changes mid-swing, the swing is cancelled.
- Range and attack speed come from the weapon.
- The weapon's attributes are read from the main-hand item at every swing, never stored, so changing the mob's weapon at any time gives it that weapon's attacks right away (Better Combat spears stab, staffs swing).

Without the mod, or with a weapon that has no Better Combat attributes, it is a normal `MeleeAttackGoal`.

`MeleeSettings.DEFAULT` options:
- `baseRange` 3: reach before the weapon's range bonus, like a player
- `intervalScale` 1: above 1 makes the mob attack slower than a player with the same weapon
- `animations` on
- `resetInvulnerability` on: with Better Combat's `allow_fast_attacks`, a victim's hurt-cooldown is cleared before the hit, but only if this mob was the last one to hurt it, so a pack of mobs can't stack damage
- `weaponTrails` on

**Dual wielding**: with Better Combat weapons in both hands (neither two-handed) the mob alternates hands like a player: main, off, main, off, each hand going through its own combo, so dual-wield-only attacks such as the dagger's double stab show up too.
Off-hand swings use the off-hand weapon's attacks, range, damage, enchantments and trail, play the attack animation mirrored, and the off-hand weapon's pose (if it has one) is shown on the other arm.
Better Combat's `dual_wielding_attack_speed_multiplier` and the main/off-hand damage multipliers apply. While dual wielding, a change to either hand cancels the swing.

**Weapon poses**: if the weapon's Better Combat attributes have a `pose`, the mob holds the weapon in that stance the whole time it has the goal, not only in combat, and the pose changes or goes away as soon as the weapon does.
In Better Combat itself the claymore, hammer, heavy axe, katana, twin blade, scythe, spear, glaive, halberd, soul knife, heavy bow and heavy crossbow presets have one (so e.g. Paladins claymores and great hammers, Archers spears and Rogues glaives). Vanilla items have none.
It follows Better Combat's player rules:
- Two-handed weapons keep the full pose while walking. One-handed ones let the arms swing while the mob walks or sneaks and only keep the weapon itself posed.
- There is no pose while swimming, climbing, gliding, using an item, doing a vanilla arm swing, casting a spell or holding a loaded crossbow. The legs are left alone while riding.
- Left-handed mobs get it mirrored, and when dual wielding the off-hand weapon's pose goes on the other arm.

The weapon is checked whenever the mob's AI looks at the goal, so a mob with `NoAI` never gets a pose. If you remove the goal from a mob yourself, call `BetterCombatCompat.onGoalRemoved(goal)` to drop the pose.

**Attack speed**: the swing length is the same as a player's: `20 / attack speed` ticks, at least Better Combat's `attack_interval_cap`, times `intervalScale`.
If the mob has an attack speed attribute its value is used. Otherwise it is worked out like a player's would be: base 4.0 plus the attack speed modifiers of everything the mob wears and holds and of its status effects, with all modifier operations.
When dual wielding, `dual_wielding_attack_speed_multiplier - 1` (default 1.2, so +20%) is added as an `add_multiplied_base` modifier, which is how Better Combat does it for players. Both hands use this main-hand speed, as for players.

Better Combat's sweeping damage penalty is not applied to mobs.

**Combat Roll dodging** (`com.mrpg_lib.compat.combat_roll.CombatRollCompat`):
```java
CombatRollCompat.createRollGoal(this, CombatRollCompat.RollSettings.DEFAULT
        .withDefensiveHealthThreshold(0.4F)
        .withEngage(true, 5.0, 0.15F)
        .withDodgeProjectiles(true))
        .ifPresent(goal -> goalSelector.add(1, goal));
```
Returns an empty `Optional` without Combat Roll, so the mob simply never rolls.
With it, the mob rolls exactly like a player: roll count, recharge and distance come from the `combat_roll:count/recharge/distance` attributes if the mob has them (otherwise their default values), cooldown, roll duration, extra distance and invulnerability frames come from Combat Roll's server config, and it plays the `combat_roll:roll` animation and sound.
- It rolls away from its target when its health drops below the threshold.
- It can roll towards a target that is a few blocks away (`withEngage`).
- It can sidestep projectiles flying at it (`withDodgeProjectiles`).
- It never rolls into lava, fire, cactus, magma, berry bushes, powder snow or deep water, or off a drop of more than 3 blocks. If no direction is safe it doesn't roll.

Give it a better (lower) priority than the mob's melee or movement goals so it can interrupt them.

`RollSettings.DEFAULT`:
- `defensiveHealthThreshold` 0.35: roll away below 35% health
- `engage` off, `engageMinDistance` 5 blocks, `engageChance` 0.1 (`withEngage`)
- `dodgeProjectiles` off
- `cooldownTicks` 40: minimum time between rolls on top of the charges
- `invulnerableTicks` -1: use Combat Roll's i-frames from its config, any other value overrides them for this mob
- `animations` on (`withAnimations`)

**Spell Engine dodging**

Spell Engine's evasion already works for every living entity, not just players: an entity with the `spell_engine:evasion_chance` attribute above its base value has a chance to completely avoid a hit.
Like for players it only works against melee attacks (player and mob), projectiles and generic damage (the `spell_engine:evadable` damage type tag), only when the hit comes from the front (Spell Engine's server config `attribute_evasion_angle`, 120° by default), and never against spells.
The attribute is a percentage on top of its base 100: `120` is 20% dodge chance, `200` is 100%.

With Spell Engine installed, this library adds what is missing for mobs:
- The mob plays Spell Engine's `spell_engine:dodge` animation (needs playerAnimator, on the `DODGE` layer). Spell Engine itself only plays it for players. The dodge sound comes from Spell Engine for everyone.
- A spell casting mob (`ISpellCasterEntity`) can't dodge while it casts, unless Spell Engine's config `attribute_evasion_allowed_while_spell_casting` allows it, same as players.

Giving a mob dodge chance:
- In a summon command or spawner: `attributes:[{id:"spell_engine:evasion_chance",base:130.0d}]` (30%)
- In code: `MobEvasion.setEvasionChance(mob, 0.3)` (`com.mrpg_lib.compat.spell_engine`, only call it with Spell Engine installed). `MobEvasion.getEvasionChance(mob)` reads it back as 0 to 1.
- `MobEvasion.setAnimationFilter(entity -> ...)` decides which mobs get the dodge animation, for mobs that have their own.

Test husk with 100% dodge chance:
```
/summon minecraft:husk ~ ~ ~3 {PersistenceRequired:1b,attributes:[{id:"spell_engine:evasion_chance",base:200.0d}]}
```
Hit it from the front: it takes no damage, plays the dodge sound and animation. Hits from behind still land.

**Trying it in a dev environment**

The `/mrpg_goal` command (op level 2) only exists in a development environment, normal players never get it.
- `/mrpg_goal <targets> bettercombat`, `/mrpg_goal <targets> combatroll` and `/mrpg_goal <targets> spellcast <spell_id>` add the matching goal to any mob you target (for example a zombie). `spellcast` needs Spell Engine installed.
- `/mrpg_goal <targets> combatroll dodge` gives the roll goal with only the projectile dodge turned on (no engage or low-health retreat rolls).
- `/mrpg_goal <targets> shoot [count]` fires that many test arrows at the targets from a ring around them, handy for testing the dodge.
- `/mrpg_goal <targets> spellcast <spell_id> custom <ticks>` uses `withCustomCasting(CustomCasting.ticks(ticks))`: no Spell Engine animation and that cast length.
- `/mrpg_goal <targets> weaponskills` adds a goal that uses the weapon skills of the mob's main-hand weapon (`MobSpellCastGoal.forWeaponSkills`). Add `reroll` at the end to pick a new spell from the weapon's choice pool after every cast.
- `/mrpg_goal <targets> clear` removes the goals again.

Give the mob a Better Combat weapon in its main hand to see the combos, or one in each hand to see dual wielding (for example `HandItems:[{id:"rogues:iron_dagger",count:1},{id:"rogues:iron_dagger",count:1}]`).

Weapon poses (needs playerAnimator):
```
/summon minecraft:husk ~3 ~ ~ {Tags:["goaltest"],PersistenceRequired:1b,HandItems:[{id:"paladins:iron_claymore",count:1},{}]}
/mrpg_goal @e[tag=goaltest] bettercombat
/item replace entity @e[tag=goaltest] weapon.mainhand with archers:iron_spear
/item replace entity @e[tag=goaltest] weapon.mainhand with minecraft:iron_sword
```
The husk should hold the claymore in Better Combat's two-handed sword pose right away, switch to the polearm pose with the spear and drop the pose with the iron sword.
Add `LeftHanded:1b` to see the mirrored pose.
Without Paladins or Archers, a datapack file `data/minecraft/weapon_attributes/iron_axe.json` containing `{"parent": "bettercombat:claymore"}` gives the iron axe the claymore pose.
