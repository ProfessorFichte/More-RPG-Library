# Spell Schools

Needs Spell Power installed. Spell schools are Spell Power's own system, this library just adds more of them.

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

**✨Dynamic Spell School** (`spell_power:dynamic`, `DynamicSpellSchool.DYNAMIC`)
- Always uses your strongest damage school: physical melee (attack damage), physical ranged (Ranged Weapon API ranged damage) or any magic school (fire, frost, arcane, soul, lightning, water, air, earth, nature, ...). Healing and generic spell power don't count on their own.
- Power, haste, critical chance and critical damage all come from that school: attack speed and Critical Strike for melee, ranged haste and Critical Strike for ranged, Spell Power haste and critical attributes for magic.
- The damage type follows it too: player attack for melee, arrow for ranged, the magic school's own damage type for magic.
- Meant for spells that should work for every class, like relic and weapon abilities.
