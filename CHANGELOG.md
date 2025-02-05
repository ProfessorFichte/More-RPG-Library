# 2.2.0 - 1.21.1
- Spell Engine 1.5 Prep Update
- fixed CustomMethods according to API Changes
- disabled executeSpellSpellEngine Method, will probably not be needed in the future

# 2.1.1 - 1.21.1
- fix custom attributes not stacking
- clean up PlayerEntityMixin

# 2.1.0 - 1.21.1
- Spell Engine 1.4 Update
- created "vulnerable_to_water_spells" entity type tag, for water spell power spells
- - created "resistant_to_water_spells" entity type tag, for water spell power spells

2.0.15 - 1.21.1
- Fix applyStatusEffect Method

2.0.14 - 1.21.1
- Rage Damage Calculation was changed to % of missing health instead of missing health amount
- This change was done due to rage being too broken in modded environments where health gets increased
- The Rage Attribute still enhances this calculation on increased damage

2.0.13 - 1.21.1
- Stunned Effect Error Hotfix

2.0.12 - 1.21.1
- added stun immunity entity type tag

2.0.11 - 1.21.1
- fixed an issue apply status effect Helper Method
- forgot Runes - Mod dependency causing a crash

2.0.10 - 1.21.1
- Frosted Effect cant be applied if the entity has the Frozen Solid Effect
- changed frozen ticks
- Freeze Immune Entities are not affected by Frosted & Frozen Solid Effect
- Frozen Solid Effect Visual Model now scales with target

2.0.9
- BUGFIX: Fixed the error occurring when leaving the server causing the client crashing the game (something with the registry of my sounds was wrong)
- added a Poison Smoke particle
- fixed a typo in poison smoke particle
- increased Frosted Particle Count

2.0.8
- Changed Frosted & Frozen Solid Effect a tiny bit
- changed the model for the Frozen Solid effect
- added Poison Gas Cloud Particles
- fixed a calculation mistake with the Rage Attribute

2.0.7
- forgot to include a wind particle from the 1.20.1 version

2.0.6
- added the wind charge item to the storm rune recipe material
- added air magic sounds for the Wind Elemental Wizard

2.0.5
- made rage damage calc clearer, configurable & changed it, so rage has more impact the more you have
- made playerentitymixin code more clean for better visibility
- added 15% multiplier for the rage attribute in the simply skills compat datapack

2.0.4
- bleeding effect now gets removed correctly when the target is in undead tag
- fixed some wrong animation names
- fixed an error with enchanting not working
- damage types for air, earth and water spell school

2.0.3
- fixed an error with some status effects
- fixed issues with some Helper Methods and deleted unused ones
- made Elemental Mastery translation more clear
- forgot to change item to id in the advancement section
- fixed a tag issue with enchantments

2.0.2
- translation was not assigned correct to attributes in lang files

2.0.1
- forgot to change the new data folder structures (recipes -> recipe, items -> item, etc.)
- forgot to change to 1.21 recipe json structure

2.0.0
- 1.21.1 Update