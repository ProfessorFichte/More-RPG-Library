# Entity Attributes

These work for every living entity, players and mobs alike.
All of them start at 100, which means "off". Everything above 100 is the strength in percent, so 150 = 50% and 200 = 100%.

| Attribute | ID | What it does |
|---|---|---|
| Damage Reflect | `mrpg_lib:damage_reflect_modifier` | Direct hits you take are reflected back at the attacker as thorns damage. 200 = 100% of the damage is reflected. |
| Lifesteal | `mrpg_lib:lifesteal_modifier` | Heals you for part of the health your hits actually removed. 200 = 100%. |
| Spell Vampire | `mrpg_lib:spell_vampire` | Heals you for part of the spell damage you deal. 200 = 100%. |
| Rage | `mrpg_lib:rage_modifier` | Extra damage for non-spell attacks, the lower your health the more. Bonus = attack damage × (Rage − 100)% × missing health %. |
| Fuse | `mrpg_lib:<school>_fuse_modifier` | Your non-spell hits also deal magic damage of that school: spell power × (Fuse − 100)%. At most once per second. Exists for `air`, `arcane`, `earth`, `fire`, `frost`, `healing` and `water`.<br>*Needs Spell Power.* |
| Burning Chance | `mrpg_lib:burning_chance` | Chance to apply [Ignited](Status-Effects) for 2 seconds. |
| Stagger Chance | `mrpg_lib:stagger_chance` | Chance to apply [Stagger](Status-Effects) for 4 seconds. |
| Stun Chance | `mrpg_lib:stun_chance` | Chance to apply Spell Engine's Stun for 2 seconds.<br>*Needs Spell Engine.* |
| Freeze Chance | `mrpg_lib:freeze_chance` | Chance to apply [Frozen Solid](Status-Effects) for 3 seconds. |
| Poison Chance | `mrpg_lib:poison_chance` | Chance to apply vanilla Poison for 6 seconds. |
| Bleeding Chance | `mrpg_lib:bleeding_chance` | Chance to apply Spell Engine's Bleed for 6 seconds.<br>*Needs Spell Engine.* |
| Armor Piercing | `mrpg_lib:armor_piercing` | Your non-spell hits ignore part of the target's armor and armor toughness. 200 = armor is ignored completely. |
| Tenacity | `mrpg_lib:tenacity` | Chance to resist a harmful status effect when it gets applied. 150 = 50% chance, 200 = immune. Bad Omen, Trial Omen and Raid Omen are never resisted. |

About the chance attributes:
- They trigger on melee hits and projectile hits, never on spells.
- The level of Ignited, Stagger, Poison and Bleed scales with the attacker's damage (15% of attack damage, or of ranged damage for projectiles).
- Ignited, Stagger, Stun and Frozen Solid share an 8 second cooldown per attacker. Poison and Bleed share a 4 second cooldown.

Lifesteal and Spell Vampire have a 1 second cooldown each, `lifestealCooldownTicks` and `spellVampireCooldownTicks` in `config/mrpg_lib/tweaks_v2.json`.
