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
**🏹Ranged Spell Schools**
- **🔥Fire Ranged**
- **❄️Frost Ranged**
**🗡️Melee Spell Schools**
- **🪓Berserker Melee**

### 3. New Entity Attributes ⭐
- To Do

### 4. New Status Effects 🌟
- To Do

### 5. Custom Spell Impacts 🪄
- To Do

### 6. Conditional Loot Spell Scroll Function 📜
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

---