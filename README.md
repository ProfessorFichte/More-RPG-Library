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
- To Do

### 4. New Status Effects 🌟
- To Do

### 5. Custom Spell Impacts 🪄
- To Do

### 6. Enhancements for Loot Tables
**Conditional Loot Spell Scroll Function 📜**
With this function you can easily add specific spell scrolls from specific spell pools to your loot tables. You can also blacklist spells and set the minimum and maximum tier of a spell.
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
This function will bind a random spell from a pool to an item, if the item is no spell container, it will also be a spell container now.
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
With this function you can add a conditional item to the loot pool, which only gets looted if its registered, otherwise the fallback item will be used.
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
With this entry type, you can add a conditional item, without a fallback, the loot table is still functional if when the item is not registered.
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