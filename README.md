# More RPG Library — Spell Engine Add-On Lib
![Title](mrpg_lib_title.png)
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
- Damage Reflect -> Melee Damage you received will be reflected towards the attacker (200 Damage Reflect = the damage you received will be 100% reflected).
- Lifesteal -> The player heals after dealing damage melee attacks or projectiles (200 Lifesteal = the damage you dealt will be 100% healed).
- Rage -> You'll deal more damage with your melee attacks, the less health you have. Calculation: Base Attack Damage + ( Generic Attack Damage * RageAttribute % * Missing Health % )
- Spell Vampire -> The player heals after dealing damage with spells (200 Spell Vampire = the damage you dealt will be 100% healed).
- "Fuse"-Attributes -> These Attributes exist for all SpellSchools (Arcane, Frost, etc.), you deal Magic Damage with your Melee Attacks & Projectiles (Spell Power * Fuse Attribute).
- Burning Chance -> Chance to apply the Ignited Effect with Melee Attacks or Projectiles (Amplifier Scales with Attack Damage).
- Stagger Chance -> Chance to apply the Stagger Effect with Melee Attacks or Projectiles (Amplifier Scales with Attack Damage).
- Stun Chance -> Chance to apply the Stun Effect with Melee Attacks or Projectiles.
- Poison Chance -> Chance to apply the Poison Effect with Melee Attacks or Projectiles (Amplifier Scales with Attack Damage).
- Freeze Chance -> Chance to apply the Frozen Solid Effect with Melee Attacks or Projectiles.
- Bleeding Chance -> Chance to apply the Bleeding Effect with Melee Attacks or Projectiles (Amplifier Scales with Attack Damage).
- Armor Piercing -> Ignores Armor values for Melee Attacks before the Damage Calculation (100% / 200 Attribute Value ignores Armor completely).
- Tenacity -> Attribute to resist Harmful Status Effects, when they're Applied. At 100 Tenacity = 0% resist, at 200 Tenacity = 100% resist Harmful Status Effects.

### 4. New Status Effects 🌟
**💀Harmful Effects** 
- Molten Armor -> Reduces Armor and Armor Toughness Attribute, only deals damage if the target has Armor Equipped, does not work if the entity is in water.
- Frozen Solid -> Increases Damage Taken, The Entity cant move, attack or jump.
- Grievous Wounds -> Reduces Healing Taken and Increases incoming damage.
- Frosted -> Reduces Movement Speed, adds Freezing Ticks every tick, at Amplifier 5 when this effect is stacked it gets converted to Frozen Solid Effect.
- Bleeding -> Damages the target overtime, damage increases with the amplifier, it also increases depending on how much %health the entity has. If the entity is under 25% Max Health this Effect will deal a lot of damage.
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

---
