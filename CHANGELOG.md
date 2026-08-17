# 2.6.5 - 1.21.1
- Fix multiple Controlled Enemies fighting each other (same owner)

# 2.6.4 - 1.21.1
- Added some new player animations
- added a ControlEnemyStatusEffect Class. Affected HostileMobs attack other Hostile Mobs.
- Affected Players change their EntityRelations (Hitting Allies with Damaging Spells for example)

# 2.6.3 - 1.21.1
- Add a fix for FriendlyLightning Entities crashing, when they're spawned via commands
- Add two new Item Groups "More Armory" & "More Arsenal"
- All my RPG Series Plus Armory & Arsenal Content will be stored in these Item Groups

# 2.6.2 - 1.21.1
- Add generalized Stealth Status Effect for multiple projects using the Stealth Mechanic
- Burst Crack is now a charged Weapon Skill -> increasing Damage & Range 
- Deprecated a lot of velocity based Custom Spell Impacts

# 2.6.1 - 1.21.1
- Fixed Mobs using the "MobSpellCastGoal", casting CLOUD spells with no set spell range all the time, with no enemies in sight
- PR: Update/Correction es_ar.json #24 - Texaliuz

# 2.6.0 - 1.21.1
- Update for the newest Spell Engine Version
- Deprecate Bleeding Effect from this Mod, because Spell Engine now provides one

# 2.5.30 - 1.21.1
- PR #22 - Fix Armor Piercing Crash - Thx rexlManu
- Changed the Dragon Claw healing spell description from hearts to health to match rpg series descriptions

# 2.5.29 - 1.21.1
- Fix Duelists Focus Renderer on Neoforge

# 2.5.28 - 1.21.1
- Add Visual Renderer for the Duelists Focus Effect
- Add Particle Spawner for Sirens Tear Effect

# 2.5.27 - 1.21.1
- fix music note particles causing lags on neoforge

# 2.5.26 - 1.21.1
- fix Duelists Focus not applying the effect to the caster
- New Weapon Passive: Dragonslayer's Fury
- When applying heals or buffs on an ally under 20% max health, they gain increased crit damage
- SpellBuilderHelper Additions (Mostly RBG Color Codes)

# 2.5.25 - 1.21.1
- Fixed the Range Passives applying cooldown on Range Weapons
- Fixed the Puncture Charge Animation
- The Spellthief Custom Impact can now also perform Beam Spells
- Added a new Status Effect for the Sirens Tear's passive, that regenerates health every second, the heal scales with missing health
- Added a new Melee Weapon Passive -> Duelist's Focus: You mark your actual target, all other targets expect the marked target deal 25% reduced damage to you, and only you deal 25% increased damage to the target

# 2.5.24 - 1.21.1
- Add Puncture Weapon Skills for the Bard Rapier's
- Fixed crash happening after Launch on Neoforge
- Added the Armory Upgrade Gem for the Bard's T5 Armor Set 

# 2.5.23 - 1.21.1
- Add custom heart-renderer #19 - Rulft44
- added a new particle type - star
- added a new PopupParticle, that can render spell icon & mob effect textures
- added BackAway & Flee Goals for Entities (Mainly for LNE-Entities)
- Fixed Bleeding Effect Damage bypassing the Resistance Effect
- Improved Mob Spellcasting with optional Intelligence Settings
- Added a new Lightning Strike Passive Spell for Ranged Weapons
- Added all Weapon Passive Spells from Loot&Explore +Add On's to this project because they might be used by multiple projects
- Weapon Skills from Berserker and Forcemaster got added to this project, so they could be used by other mods as well without having these mods installed
- Added some SpellBuilder Helper methods

# 2.5.22 - 1.21.1
- Fixed a crash due to LivingEntityMixin with the Ender Dragon attacking
- Added Safeguards and Null checks to check if the entity even has the attribute that the custom function could run

# 2.5.21 - 1.21.1
- improved the isEntityProtectedCheck logic because Spell Impacts caused by spell spawned entities did sometimes not work on players that are on another team if pvp was enabled
- Rage, Spell Power Fuse, Lifesteal and all other attributes that have some technical function. can now all be used by all LivingEntities
- Heavily Improved MobSpellCastGoal, especially channeled spells like fire breath
- If you try to run away from a fire breath casting mob it'll follow you while still casting the spell
- When you come to close while a Mob is casting/channeling a Spell, the entity will walk slowly backwards

# 2.5.20 - 1.21.1
- fixed an MobSpellCaster crash due to wrong class and method naming

# 2.5.19 - 1.21.1
- Fix Death Messages and Fatal Poison - Pull Request #17
- Improved the entityRelationCheck CustomMethod and renamed it to -> isEntityProtectedCheck
- Renamed CastSpellGoal to MobSpellCastGoal
- Improved the Goal and ISpellCasterEntity a lot to automatically recognize what Spell Type was chosen and how it needs to be casted
- A Guide how to add the SpellCastGoal to a Mob has been added in the README
- Bleeding no longer bypasses resistance
- Added SimpleSoundGeneratorV2 for Datagen
- Some Sound Effects got added for future content
- Magic Fuse Attacks now have a 20 tick cooldown to avoid click spamming

# 2.5.18 - 1.21.1
- Update for Spell Engine 1.9 Update
- Delete SpellEngineTiersMixin
- Fix Spell Scroll Loot Functions
- Fix Spell Projectile and Status Effect Renders due to the Spell Engine API Changes

# 2.5.17 - 1.21.1
- Added a new TerrainBlendingProcessor, which makes Path Structures bend smoother and more natural to terrain
- fixed some weird looking stuff for the ConditionalJigsawStructure & PathAdaptionProcessor

# 2.5.16 - 1.21.1
- Added ConditionalJigsawStructure (Specially for LNE-Add-On's)
- Added PathAdaptionProcessor & WaterPillarProcessor for paths that bend to the environment (Specially for LNE-Add-On's)

# 2.5.15 - 1.21.1
- Fix Fatal Poison's wrong Status Effect color
- fix Carve Status Effect reducing incoming damage instead of increasing it
- Add new Music Note Particles

# 2.5.14 - 1.21.1
- Fix Datagen Helpers

# 2.5.13 - 1.21.1
- Add Tenacity Attribute -> Everytime a Harmful Status Effect gets applied it will check the Tenacity Attribute
- The Attribute has a 100 Base and 200 Max Value, 200 Means, that the Entity is fully Immune to Harmful Status Effects
- Trial-, Raid- & Bad-Omen Effects are excluded from that
- A value of 150 in Tenacity means, that there is a 50% chance of not applying the Harmful Status Effect
- Add entityRelationCheck & getRangedDamageAttribute Custom Methods
- add 2x new Datagen Helper Methods, for Advancements with Spell Engine Requirements & Generating Better Combat Weapon attribute data files

# 2.5.12 - 1.21.1
- All Attributes, that can inflict status effects, do now also work with ranged weapons
- Fuse Attributes now only deal Damage with Ranged Weapons
- Lifesteal now also works with Projectiles
- Lifesteal only triggers IF the player is actually removing health from the target
- The Frozen Solid, Fear, Stagger and Ignited Effect now shows their own Hud Message instead of "Stunned"
- Added some particle Visuals to the Attribute Based Trigger Effects

# 2.5.11 - 1.21.1
**Add new Attributes with melee-attack functionality**
- burning_chance -> Chance to ignite the target, dealing damage every 10 ticks, reducing the targets healing taken (scaled with attack damage)
- stagger_chance -> Chance to apply the Stagger Effect
- stun_chance -> Chance to apply the Stun Effect
- poison_chance -> Chance to apply the Poison Effect (scaled with attack damage)
- freeze_chance -> Chance to apply the Frozen Solid
- bleeding_chance -> Chance to apply the Bleeding Effect (scaled with attack damage)
- armor_piercing -> Ignores the Armor values of the target in %
**Internal Changes and Fixes**
- Fixed missing amplifier scaling for the Bleeding Effect
- Fixed Lifesteal healing before armor calculation
- Fix Rage Damage Calculation

# 2.5.10 - 1.21.1
- fix wrong spell id for Melee lightning strike passive

# 2.5.9 - 1.21.1
- The damage dealt by the LightningEntity is now determined by the highest Attribute:
- (Generic Attack Damage, Ranged Weapon Damage & Magic Spell School)
- Adding Particles, Sounds and Passive Spells from the Berserker Mod to this Library
- Add new HelperMethods, to determine the highest Spell power Attribute / Damaging Attribute
- The Rage Attribute now impacts the Power of the RAGE_MELEE School more
- Add FatalPoison Effect (Thanks Rulft44) Pull request #13

# 2.5.8 - 1.21.1
- Add FriendlyLightningEntity for the custom spell impact LightningStrikeImpact

# 2.5.7 - 1.21.1
- Activate Archers Expansion Armory Upgrade Gem
New API Additions:
- SmithingRecipeGenerator -> with conditional required mod loading checks for fabric and neoforge 
- CastSpellGoal & ISpellCasterEntity for Spell Engine Spell Casting Mob Entities

# 2.5.6 - 1.21.1
- New LOOT_FUNCTION_TYPE -> add conditional Item (Checks if the item is registered, if not uses fallback item)
- New LOOT_FUNCTION_TYPE -> Bind Spell From Pools Function (Binds a random spell from pools to a weapon or jewelry for example)
- New LOOT_POOL_ENTRY_TYPE -> the item only gets added to the loot table, if its registered (without fallback item)
- Update es_ar (Thanks Texalius)

# 2.5.5 - 1.21.1
- Activate Berserker Armory Upgrade Gem

# 2.5.4 - 1.21.1
- Activate Elemental Wizards Armory Upgrade Gems
- Fix Damage Reflect Mixin, casting Mobs to PlayerEntites

# 2.5.3 - 1.21.1
- fix crash in dev environment

# 2.5.2 - 1.21.1
- Added compatibility for Critical Strike mod for Rage Melee, Fire- and FrostRanged SpellSchool
- Change Loot Injection
- Add Armory Upgrade Crystal Compat for MRPG Classes


# 2.5.1 - 1.21.1
- Fix fabric.mod.json and neoforge.mods.roml
- Fix Particles in NeoForge Client
- add global elemental weaknesses for MoreSpellSchools

# 2.5.0 - 1.21.1
- Move to Architectury multiloader workspace
- NeoForge Beta!

# 2.4.3 - 1.21.1
- frozen Ticks are now applied more smoothly
- Fix Bleeding Effect tick infinitely
- Add a Cooldown to SpellVampire and Lifesteal Effect (Ticks configurable in the TweaksConfig)

# 2.4.2 - 1.21.1
- fix falling icicle model

# 2.4.1 - 1.21.1
- Add Nature Spell School to SpellSchoolMixin registry
- Forgot some Damage Type Tags regarding the Nature Spell School

# 2.4.0 - 1.21.1
- Update to Spell Engine 1.8 & Spell Power 1.4
- Also, Update Tiny Config & Fabric API Version
- Add Nature Spell Power School and Rune & Leaf Particles (Thanks to Rulft44 for providing the artwork!)
- Add Sound Effects for Nature Spells
- Improve Earth Casting Sound Effect
- Delete Elemental Mastery Enchantment (Spell Power just adds 2 spell schools per enchantment, thats why this was changed)
- 2 new Enchantments got added: Typhoon (Air & Water) & Stonebloom (Earth & Nature)

# 2.3.5 - 1.21.1
- add bleeding immune entity tag
- remove bleeding testing logger I forgot
- rename BERSERKER_MELEE Spell School to RAGE_MELEE

# 2.3.4 - 1.21.1
- Buffed the Bleeding Effect
- it now deals additional damage according to the max health if the target is under a certain hp range
- added Falling Icicle Projectile
- Added the Frost- & Fire Ranged Spell School from the Archers Expansion Mod in this Lib
- Added the Berserker Melee Spell School from the Berserker Mod in this Lib
- new CustomSpellImpact: damage_according_to_missing_health
- The less health your target has, the more damage you cause (DamageType is your SpellSchools Damage Type)
- (above 50% -> 0.25 / below 50% -> 1.0 / below 25% -> 1.75 of your SpellSchools Attribute Power)

# 2.3.3 - 1.21.1
- Reduce Damage Taken of Frozen Solid Spell and Add Frost Spell related vulnerability
- Add Specific Spell Scroll Loot Function (spell pool, min to max spell-tier can be set)
- Fix Backward- and Forward dash, that was calculated by spell range
- nerf Frosted Movement speed slow

# 2.3.2 - 1.21.1
### Internal
- Removed Frostiful & EnviromentZ Compat (Extra Datapack will be released)
- Improve Frosted Particle Status Effect
- Improve stackFreezeStacks HelperMethod
- Add freezeDamageTicks HelperMethod
- Frosted & FrozenSolid Status Effect now also slowly stack freezeTicks like Powder Snow Blocks
- Delete Poison Cloud and Gas Cloud particles because they're obsolete now
### Visual Additions
- Add Ice Trap & Stone Trap Particle (Credits: ElvGames)
### Custom Spell Impacts
- fix stop arrow spell impact
- The Custom Spell Impact Knock Up's value can now be changed in the tweaks config
- Impact that spawns a lightning strike on the target (vanilla lightning entity)
- Pull To Caster Slowly Custom Impact
- Rush Forward Custom Impacts
- Disengage BackwardDash custom impacts
- scaled knockback according to how close the target is to the caster (closer = higher knockback)
- Impact that sets frozen Ticks to a maximum

# 2.3.1 - 1.21.1
- Adjust Custom Knock Up Impact
- Add Fixed Amount Knock Up Spell Impact
- Add Pull To Caster Spell Impact

# 2.3.0 - 1.21.1
### INTERNAL & FUNCTIONAL CHANGES
- Update es_ar lang file
- Fix: add untamed wilds compat for white hide
- changed ArcaneFuse LifeSteal & Rage Mixin Logic due to incompatibility with Synitra Connector
- Delete Stun Effect & Particles, because Spell Engine 1.7 now has a generic Stun Effect
- Update Mod Icon
### NEW CONTENT
- Added "Fuse"-Attributes for all SpellSchools (expect Soul & Lightning), dealing additional magic damage per hit
- Add Soaked Effect from Elemental Wizards
- Add Water Drop Particle
- Add Slash Claw Particle
- Add Custom Spell Impacts (KnockUp & StopArrows)
- Add Custom SpellEntityPredicates (IsOnGround, IsWet, IsInWater)

# 2.2.8 - 1.21.1
- Spell Engine 1.7 Update

# 2.2.7 - 1.21.1
- add the stagger status effect
- molten armor now reduces the armor by division
- add untamed wilds compat for white hide

# 2.2.6 - 1.21.1
- fix clearNegativeEffects Method

# 2.2.5 - 1.21.1
- fix grievous wounds effect and change it a bit

# 2.2.4 - 1.21.1
- Add SpellEngineTiersMixin to mixin.json
- fix some animations

# 2.2.3 - 1.21.1
- fix frosted status effect particle error
- improve some animation files
- add fireproof tiers mixin for spell engine weapon registry

# 2.2.2 - 1.21.1
- add es_ar translation
- the Bleeding Effect can now kill

# 2.2.1 - 1.21.1
- add elemental runes to rune item tag for rune pouches
- add sounds, particles and player animations for Loot & Explore 1.21 Port
- fix DamageReflect Loop with Thorns GitHub Issue #4
- Add Spell Vampire Attribute (LifeSteal for Spell Magic Damage)

# 2.2.0 - 1.21.1
- Spell Engine 1.5 Prep Update
- fixed CustomMethods according to API Changes
- disabled executeSpellSpellEngine Method, will probably not be needed in the future
- Update to Fabric Loom 1.9

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