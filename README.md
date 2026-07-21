# More RPG Library — Spell Engine Add-On Lib
![Title](.media/mrpg_lib_title.png)
A library add-on for the [Spell Engine Mod](https://github.com/ZsoltMolnarrr/SpellEngine) that expands its functionality with new content and Features. Feel free to use this library as a dependency for your mod!

[![Fabric](https://img.shields.io/badge/loader-fabric-black?style=for-the-badge&labelColor=black&color=grey)](https://fabricmc.net/)
[![Curseforge](https://img.shields.io/badge/curseforge-black?style=for-the-badge&logo=curseforge&labelColor=black&color=grey)](https://www.curseforge.com/minecraft/mc-mods/more-rpg-library)
[![Modrinth](https://img.shields.io/badge/modrinth-black?style=for-the-badge&logo=modrinth&labelColor=black&color=grey)](https://modrinth.com/mod/more-rpg-library)
[![Discord](https://img.shields.io/discord/1209063880790376468?style=for-the-badge&logo=discord&label=discord&labelColor=black&color=grey)](https://discord.com/invite/AShKd5XrTu)
[![KoFi](https://img.shields.io/badge/donations-black?style=for-the-badge&logo=kofi&labelColor=black&color=grey)](https://ko-fi.com/fichteee)
---

## Features

### 1. Assets 🖌️
- New **Animations**
- New **Sound Effects**
- New **Particle Effects**

### 2. New Spell Schools 🪄
**🧙‍♂️Magic Spell Schools**
- **🌪️Air Magic**
- **🪨Earth Magic**
- **🌊Water Magic**
- **🍃Nature Magic**

**🏹Ranged Spell Schools**
- **🔥Fire Ranged**
- **❄️Frost Ranged**

**🗡️Melee Spell Schools**
- **🪓Rage Melee**

### 3. New Entity Attributes ⭐
- Damage Reflect -> Melee Damage received will be reflected towards the attacker (200 Damage Reflect = the damage received will be 100% reflected). Works for all Living Entities.
- Lifesteal -> The entity heals after dealing damage with melee attacks or projectiles (200 Lifesteal = the damage dealt will be 100% healed). Works for all Living Entities.
- Rage -> Deals more damage with melee attacks, the less health the attacker has. Calculation: Base Attack Damage + ( Generic Attack Damage * RageAttribute % * Missing Health % ). Works for all Living Entities.
- Spell Vampire -> The entity heals after dealing damage with spells (200 Spell Vampire = the damage dealt will be 100% healed). Works for all Living Entities.
- "Fuse"-Attributes -> These Attributes exist for all SpellSchools (Arcane, Frost, etc.), deals Magic Damage with Melee Attacks & Projectiles (Spell Power * Fuse Attribute). Works for all Living Entities.
- Burning Chance -> Chance to apply the Ignited Effect with Melee Attacks or Projectiles (Amplifier Scales with Attack Damage). Works for all Living Entities.
- Stagger Chance -> Chance to apply the Stagger Effect with Melee Attacks or Projectiles (Amplifier Scales with Attack Damage). Works for all Living Entities.
- Stun Chance -> Chance to apply the Stun Effect with Melee Attacks or Projectiles. Works for all Living Entities.
- Poison Chance -> Chance to apply the Poison Effect with Melee Attacks or Projectiles (Amplifier Scales with Attack Damage). Works for all Living Entities.
- Freeze Chance -> Chance to apply the Frozen Solid Effect with Melee Attacks or Projectiles. Works for all Living Entities.
- Bleeding Chance -> Chance to apply the Bleeding Effect with Melee Attacks or Projectiles (Amplifier Scales with Attack Damage). Works for all Living Entities.
- Armor Piercing -> Ignores Armor values for Melee Attacks before the Damage Calculation (100% / 200 Attribute Value ignores Armor completely). Works for all Living Entities.
- Tenacity -> Attribute to resist Harmful Status Effects when they're applied. At 100 Tenacity = 0% resist, at 200 Tenacity = 100% resist Harmful Status Effects. Works for all Living Entities.

### 4. New Status Effects 🌟
**💀Harmful Effects** 
- Molten Armor -> Reduces Armor and Armor Toughness Attribute, only deals damage if the target has Armor Equipped, does not work if the entity is in water.
- Frozen Solid -> Increases Damage Taken, The Entity cant move, attack or jump.
- Grievous Wounds -> Reduces Healing Taken and Increases incoming damage.
- Frosted -> Reduces Movement Speed, adds Freezing Ticks every tick, at Amplifier 5 when this effect is stacked it gets converted to Frozen Solid Effect.
- Stagger -> Reduces Armor, Attack Damage & Movement Speed and incapacitates the target.
- Soaked -> Soaking the target with water extinguishing fire, more vulnerable to frost, lightning and water spells.
- Carve -> Reduces armor and increases damage taken.
- Fatal Poison -> Inflicts damage over time, and can kill both undead and non-undead mobs.
- Ignited -> Burns the target, dealing damage over time and reduces healing. The target cannot move or attack.
- 
**☘️Beneficial Effects**
- Collected Soul -> Increases Soul Power

### 5. Custom Spell Impacts 🪄
Custom Spells Impacts are added in this format, in your spell.json file:
```json
  "impacts": [
    {
      "action": {
        "type": "CUSTOM",
        "custom": {
          "intent": "HARMFUL",
          "handler": "more_rpg_classes:knock_up_fixed"
        }
      }
    }
  ]
```
List of Custom Impacts added by this Library:
- "knock_up" -> Knocks the target up (scalable).
- "knock_up_fixed" -> Knocks the target up with a fixed height amount (0.75 Y-Velocity).
- "stop_arrows" -> Checks for Arrows, their Velocity gets set to 0.
- "pull_to_caster_direct" -> Pulls the Target directly in front of the Caster.
- "pull_to_caster_slow" -> Pulls the Target Slowly to the Caster (For Channeled Spells).
- "lightning" -> Summons a Lightning Entity to the target.
- "trembling" -> Throws the target around in random directions.
- "rush_forward_to_target" -> The caster travels fast-forward to the target.
- "backward_dash_fixed" -> The Caster Dashes back for a fixed range.
- "backward_dash_range" -> The Caster Dashes back, range is scaled with the "range" value of your spell.json file.
- "range_scaled_knockback" -> Ranged Scaled Knockback, the Closer the Target is to the Caster, the higher the Knockback Value is.
- "frozen_ticks" -> Adds Frozen Entity Ticks on the Target.
- "forward_dash_range" -> The Caster Dashes forward, range is scaled with the "range" value of your spell.json file.
- "damage_according_to_missing_health" -> The target is damaged according to it's missing health percentage (Above 50% = 0.25 Under 50% = 1.0 and Under 25% = 1.75), the damage is calculated with the casters spell school attribute value (Spell School Attribute * Missing Health Damage Multiplier)


### 6. Enhancements for Loot Tables
**Conditional Loot Spell Scroll Function 📜**

- With this function you can easily add specific spell scrolls from specific spell pools to your loot tables. You can also blacklist spells and set the minimum and maximum tier of a spell.
```json
{
  "type": "minecraft:item",
  "name": "spell_engine:scroll",
  "functions": [
    {
      "function": "more_rpg_classes:specific_spell_scroll_pool",
      "spell_pools": ["#wizards:frost", "#wizards:fire"],
      "spell_tier_min": 3,
      "spell_tier_max": 4,
      "count": 1,
      "blacklist_spells": ["wizards:frost_blizzard", "wizards:fire_meteor"]
    }
  ]
}
```

**Bind specific spells from spell pools on an item**

- This function will bind a random spell from a pool to an item, if the item is no spell container, it will also be a spell container now.
```json
        {
          "type": "minecraft:item",
          "name": "minecraft:diamond_sword",
          "functions": [
            {
              "function": "more_rpg_classes:bind_spell_from_pools",
              "spell_pools": ["#wizards:arcane","#wizards:frost"],
              "count": 1
            }
          ]
        }
```

**Conditional Item with Fallback**

- With this function you can add a conditional item to the loot pool, which only gets looted if its registered, otherwise the fallback item will be used.
```json
        {
          "type": "minecraft:item",
          "name": "minecraft:diamond",
          "functions": [
            {
              "function": "more_rpg_classes:conditional_item",
              "conditional_item": "your_mod:jade_gem"
            },
            {
              "function": "minecraft:set_count",
              "count": {
                "min": 1,
                "max": 4
              }
            }
          ]

        }
```

**Conditional Item - Loot Pool Entry**

- With this entry type, you can add a conditional item, without a fallback, the loot table is still functional if when the item is not registered.
```json
        {
          "type": "more_rpg_classes:conditional_item",
          "item": "your_mod:jade_gem",
          "count": {
            "type": "minecraft:uniform",
            "min": 1,
            "max": 3
          }
        }
```


### 7. Spellcaster Flee & Positioning Goals

Three ready-made AI goals for spell-casting mobs that need to keep their distance and flee when low on health.

---

**`IConditionalFleeEntity`**

Implement this interface on your entity to unlock the two goals below. Your entity must extend `PathAwareEntity` (or any subclass) to use `LowHealthFleeGoal`. All methods have sensible defaults to override only what you need:

```java
public class MyWizard extends PathAwareEntity implements ISpellCasterEntity, IConditionalFleeEntity {

    @Override public float getFleeDistance()         { return 4.0F; }
    @Override public float getLowHealthFleeDistance() { return 6.0F; }

    // Effect IDs on the caster that suppress fleeing (e.g. a damage-immunity shield)
    @Override public List<Identifier> getFleeImmuneEffects() {
        return List.of(Identifier.of("mymod", "arcane_shield"));
    }

    // Effect IDs on the target that suppress fleeing (e.g. a snare/root)
    @Override public List<Identifier> getFleeIgnoreIfTargetHasEffects() {
        return List.of(Identifier.of("mymod", "frost_snare"));
    }

    // If the target's HP fraction is below this value, stop fleeing and keep attacking. 0 = disabled.
    @Override public float getFleeIgnoreTargetHpThreshold() { return 0.3f; }
}
```

---

**`LowHealthFleeGoal<T extends PathAwareEntity & IConditionalFleeEntity>`**

Flees from players when the mob drops below **35% HP**. Flee is suppressed when any of the three conditions from `IConditionalFleeEntity` are met (caster is immune, target is snared, or target is nearly dead).

```java
this.goalSelector.add(1, new LowHealthFleeGoal<>(this));
```

---

**`BackAwayGoal<T extends MobEntity & ISpellCasterEntity>`**

Backs away from the combat target whenever it closes within `minDistance` blocks. Stops immediately once safe so spells can fire right away. Does nothing while the mob is already casting.

```java
// (mob, minDistance, speed)
this.goalSelector.add(2, new BackAwayGoal<>(this, 4.0F, 1.2));
```

---

**Full example**

```java
@Override
protected void initGoals() {
    this.goalSelector.add(0, new SwimGoal(this));
    this.goalSelector.add(1, new LowHealthFleeGoal<>(this));          // flee at low HP
    this.goalSelector.add(2, new BackAwayGoal<>(this, 4.0F, 1.2));   // keep distance while casting

    MobSpellCastGoal fireball  = new MobSpellCastGoal(this, "mymod:fireball",  spellGoals);
    MobSpellCastGoal frostbolt = new MobSpellCastGoal(this, "mymod:frostbolt", spellGoals);
    this.goalSelector.add(3, fireball);
    this.goalSelector.add(4, frostbolt);
    spellGoals.add(fireball);
    spellGoals.add(frostbolt);
}
```

---

### 8. Mob Spell Casting

`MobSpellCastGoal` lets any mob cast SpellEngine spells. Delivery type, particles, sounds, channeling, and cooldown are all driven by the spell JSON — nothing to configure manually.
**Setup:**
1. Implement `ISpellCasterEntity`
```java
public class MyWizard extends HostileEntity implements ISpellCasterEntity {

    private boolean isCasting = false;
    private int castingTicks = 0;

    @Override public void startSpellCast(int ticks) { isCasting = true;  castingTicks = ticks; }
    @Override public void stopSpellCast()           { isCasting = false; castingTicks = 0;     }
    @Override public boolean isSpellcasting()       { return isCasting; }
    @Override public MobEntity asMobEntity()        { return this; }
}
```
2. Add goals in `initGoals()`
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
3. Tick the cooldowns

```java
@Override
public void tick() {
    super.tick();
    for (MobSpellCastGoal goal : spellGoals) {
        goal.updateCooldown();
    }
}
```

---

**What works automatically**

| Feature | Driven by |
|---|---|
| Projectile, Meteor, Cloud, Direct, Area, Beam, Teleport | `spell.json` delivery + target type |
| Cast duration, channeling & charging | `spell.active.cast.duration` / `type` (`CHANNEL` / `CHARGE`) |
| Cast start sound & particles | `spell.active.cast.start_sound` / `particles` |
| Release particles & sound | `spell.release.particles` / `sound` |
| Impact handling | SpellEngine internals |
| Cooldown | `spell.cost.cooldown.duration` |
| `particles_scaled_with_ranged` | scaled by `spell.range` automatically |

---

**Healing spells**

Spells whose **entire** impact list is `HEAL` are cast on the nearest wounded ally instead of the combat target.

Spells that mix `HEAL` with damage or other impacts target the combat target normally — SpellEngine's relation system prevents the heal from applying to enemies.

---

**Charge spells**

For a spell authored with `spell.active.cast.type = CHARGE`, the mob tries to hold for the full duration (full power), but releases early — at reduced, curve-scaled power — the moment a target closes within melee range, so the mob doesn't just eat a hit while charging. If forced to release before the spell's `min_release_ratio`, the cast fizzles instead of firing.

---

**Intelligent Spellcasting**

Enable situational spell selection per goal by calling `.withIntelligentSpellcasting()`:
```java
MobSpellCastGoal fireball = new MobSpellCastGoal(this, "mymod:fireball", spellGoals)
        .withIntelligentSpellcasting();
```

When enabled, before casting the goal runs these filters in order:
- **Self-heal priority** — if the caster is below 50% HP and has a pure heal spell available, only heal spells are considered.
- **Ally-heal priority** — if an ally is below 60% HP and a heal spell is available, only heal spells are considered.
- **Kill priority** — if the target is below 30% HP, pure heal spells are skipped so the mob focuses on finishing the target.
- **`SpellBehaviorRegistry`** — any custom per-spell condition registered externally (see below).

---

**SpellBehaviorRegistry**

Register custom cast conditions for specific spells. Conditions registered here are checked when `intelligentSpellcasting` is active:
```java
SpellBehaviorRegistry.register(
    Identifier.of("mymod", "updraft"),
    (caster, target, spellId) -> target != null && !target.isOnGround()
);
```

The condition receives the caster, the current target (nullable), and the spell ID. Return `false` to skip this spell this tick — the goal moves on to the next available spell.

---