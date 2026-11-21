# Changelog

All notable changes to More RPG Library are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [2.8.3+1.21.1]

### Added

- With Spell Power and Village Taverns installed, the Taverns bartender sells the Earth, Water, Air and Nature potions (level 3, 24 emeralds)
- Brewing recipes for the 4 school potions: Thick Potion + Clay Ball (Earth), Prismarine Shard (Water), Wind Charge (Air) or Moss Block (Nature). A Fermented Spider Eye turns Earth into Air and Water into Nature and back
- Sound events `mrpg_lib:frost_teleport`, `mrpg_lib:frozen_wall`, `mrpg_lib:glacial_spikes`, `mrpg_lib:icicle_hit` and `mrpg_lib:slash_impact`
- Item tag `mrpg_lib:underwater_ranged_weapons`: arrows shot from these bows and crossbows only lose 1% speed per tick underwater, like a trident, and the weapon shows "Shoots normally underwater" in its tooltip

### Fixed

- Sound effects are now mono and 48 kHz, so they come from the right direction and get quieter with distance like vanilla sounds
- Spell casting mobs only start a spell when the target is in range or about to walk into it, and only release it when the target is still in reach. If the target stays out of reach, the spell is cancelled without a cooldown
- Cloud spells without a range, like Frost Spikes, now use the real reach of their clouds instead of 16 blocks

## [2.8.2+1.21.1]

### Added

- MRPG FX (`com.mrpg_lib.fx`): code-defined particle and sound effects sent as one packet per effect and built on the client.
- These defined were mainly added for the Loot & Explore Update. Relic's & Weapons with Abilities will use it.
- 14 shapes, velocity and motion modes, anchors, orientations, timed effects that follow an entity, delayed layers, sounds and child effects
- 13 FX presets that are adjusted with one call: Explosion, Burst, Shockwave, Aura, Swirl, Beam, Trail, Impact, Cast, Pillar, Cloud, Slash and Storm
- Client setting `fx_density` in `config/mrpg_lib/client_fx_v1.json`
- Colored copies of 13 vanilla particles, that use the vanilla textures and take any color and a scale
- `FxLayer.lifetime(ticks)` sets how long the particles of a layer live, `FxParams.startAge(ticks)` starts a timed effect part way through
- `mrpg_lib:floating_bubble` particle: a bubble that floats and lives for a few seconds, also outside of water
- Dynamic spell school `spell_power:dynamic`: always uses the strongest of your melee, ranged and magic schools, including that school's haste, critical chance, critical damage (Critical Strike for melee and ranged) and damage type
- Structure processor `mrpg_lib:random_replace`: turns a random number of blocks of a structure piece into another block, optional with a loot table. The other ones can stay, become a fallback block or copy the blocks around them
- Structure processor `mrpg_lib:item_frame_items`: gives item frames in a structure a random item from a list of item ids and tags, items of missing mods are skipped
- Structure block modifiers (`data/<namespace>/mrpg_lib/structure_block_modifier/*.json`): replace blocks in any structure with a chance, also in structures that are built in code like strongholds, mineshafts or nether fortresses, optional with a loot table. `StructureBlockModifierProvider` creates them with datagen
- `mrpg_lib:random_replace` can replace every target block with a `chance` (optionally capped by `count`), and `keep_properties` keeps the old block's state properties
- Structure processor `mrpg_lib:container_loot`: gives chests, barrels, decorated pots, suspicious blocks and other lootable blocks of a structure a loot table with a chance, and keeps the rest of the block (facing, pot sherds)
- `MobSpellCastGoal.withWeaponSkills(true)` / `MobSpellCastGoal.forWeaponSkills(...)`: mobs use the active skills of the weapon in their main hand (Swift Strikes, Cleave, Impale, ...) and prefer them over their other spells. The weapon is read every time a spell is picked
- Spell casting mobs with an owner (`Ownable`, tamed or controlled) now hit and help the same targets their owner would, so a player's summon doesn't hit its owner or other players with area spells
- Beam spells of mobs don't stick to the target anymore. The beam turns towards the target with a limited speed (1.5 degrees per tick, `withBeamTurnSpeed(degrees)`, 0 = old behaviour) and the mob turns with it, only hits what it really touches and stops at walls, so a fast target can outrun it
- Mobs also use weapons with a spell choice pool: one random spell of the pool is picked when the mob spawns, or a new one after every cast with `withWeaponSpellChoice(WeaponSpellChoice.PER_CAST)`
- `MobSpellCastGoal.withCustomCasting(CustomCasting)`: for mobs with their own cast animation, no Spell Engine player animation, the mob's own cast length and hooks for cast start, release and stop
- Hostile mobs using the Better Combat goal or casting spells can now hit their own current target even if it is another hostile mob (for example a skeleton that shot them by accident). Other hostile mobs nearby are still spared
- Mobs with Spell Engine's evasion chance attribute play the `spell_engine:dodge` animation when they dodge (needs playerAnimator), and spell casting mobs can't dodge while casting, like players. `MobEvasion.setEvasionChance(mob, chance)` sets it from code
- Dev command: `/mrpg_goal <targets> weaponskills [reroll]` and `/mrpg_goal <targets> spellcast <spell> custom <ticks>`
- Loot condition `mrpg_lib:spells_available`: true when Spell Engine has spells in the given pools and tiers, so a spell scroll entry is only rolled when it can get a spell (always false without Spell Engine)

### Fixed

- Lifesteal, Spell Vampire, Rage, Fuse and the burning / poison chances could stop working for a long time after switching to another singleplayer world.
  Their cooldowns are now kept per entity, so nothing carries over into another world and old entries are cleaned up
- With Critical Strike installed, mobs couldn't cast spells of the Frost Ranged, Fire Ranged and Rage Melee schools
- Crash on startup without Spell Engine: Tiny Config is now included in the jar. Before it only came with Spell Engine
- Loot tables that use the spell scroll or spell pool function no longer stop working completely without Spell Engine. The functions just do nothing then
- The conditional item entry no longer gives an empty roll when its item is missing, one of the other entries is picked instead.
- The conditional item entry ran its functions twice. A spell scroll got two spells and `enchant_randomly` two enchantments. Now they run once
- Path processors (`path_adaptation`, `water_pillar`, `terrain_blending`) now check the ground where the block really ends up, also in `terrain_matching` pieces. Before they looked at the height the template was saved at, which removed or swapped path blocks
- `water_pillar` builds each pillar once, in the chunk of its corner. Before it was built again for every chunk the path touched, which could leave a path block on top of the pillar, no fence, and "setBlock in a far chunk" errors
- Unknown block ids in structure processors no longer turn into air (which made every air block of the template count as a marker), they are skipped with a warning
- `terrain_blending` only copies full terrain blocks, no more plants, stairs, slabs, bricks or ores, never blocks of the structure itself, and doesn't turn snow into powder snow anymore
- `path_adaptation` got a `fallback_block` option. `stop_on_water` is ignored, it never had an effect
- The conditional jigsaw structure supports `pool_aliases` and checks `max_distance_from_center` like the vanilla jigsaw structure
- NeoForge: the rage attribute and the Duelist's Focus damage changes had no effect, NeoForge ignores the damage value they changed

## [2.8.1+1.21.1]

### Fixed

- Fixed a server crash (StackOverflow) when two players with high Damage Reflect hit each other
- The elemental weaknesses config now actually takes effect. Its default water and earth tag conditions and the water weakness/resistance entries were broken and are fixed. Delete `config/mrpg_lib/elemental_weaknesses.json` once so it regenerates with the new defaults
- Wither Pulse no longer uses a missing sound, which spammed the log with NullPointerExceptions
- Range scaled knockback now uses the real distance and range bonuses

## [2.8.0+1.21.1]

### Added

- `mrpg_lib:custom_cloud` entity, a generic lingering cloud, added mainly for Loot & Explore
- Mobs can play player animations (needs playerAnimator). This includes villagers, wandering traders, witches and illagers.
- Better Combat (optional): mobs that use the new melee goal attack with their weapon's combos, animations, hitboxes and weapon trails, with the same animation timing, slowdown while swinging and knockback rules as players.
- They can dual wield too: alternating hands, mirrored off-hand swings and Better Combat's dual-wield attack speed and damage multipliers.
- Weapons with a holding pose (like claymores, spears and glaives) are held in that pose whenever the mob carries them, with the same walking and two-handed rules as for players
- Combat Roll (optional): mobs that use the new roll goal can roll away when hurt, roll towards their target and dodge projectiles
- Spell casting mobs can cast arrow spells and stash spells
- Spell casting mobs can also use summon spells correctly now
- Spell casting mobs can now use melee weapon skills like Swift Strikes: they walk up to the target, dash forward when the skill has momentum, and hit everything in the swing's arc
- Spell casting mobs can cast spells with a custom delivery from other mods
- Upgrade crystals (T5 Class Armor Upgrade Crystals) are now affiliated loot: they drop more often for players carrying a spell book of a class they apply to
- Breaking: `ISpellCasterEntity.isCastingSpell()` is now `isMobCasting()`. The old name collides with vanilla's own `isCastingSpell` on spellcasting illagers, which broke the interface on NeoForge dev environments
- `MobSpellCastGoal`: `SpellTimings` with `withTimings(...)` (goal-wide or per spell; cast/channel seconds, cooldown seconds, channel ticks) and `withAnimations(true)` to play the spell's cast and release animations
- `MobSpellCastGoal` casts arrow spells (`SHOOT_ARROW`, `AFFECT_ARROW`) with real arrows, and stash spells (`STASH_EFFECT`, e.g. Power Shot) like on players.
- `MobSpellCastGoal` runs `CUSTOM` deliveries through Spell Engine, so handlers registered with `SpellHandlers.registerCustomDelivery` work for mobs (they get the mob as `caster`).
- `SpellBehaviorRegistry.targetDistance(min, max)`: ready-made condition to only cast a spell when the target is within a distance range
- Spell Engine summons of a mobSpellCaster treat it as their owner, so they follow and defend it and attack its target
- playerAnimator mob bridge (`compat.player_animator.api.MobAnimations`, `MobAnimationOptions`, `MobAnimationLayer` with `POSE`, `OFF_HAND_POSE`, `MISC`, `ATTACK`, `CASTING`, `RELEASE`, `DODGE`, client `MobModelAdapters`, built-in `VillagerModelAdapter`)
- Goals: `LongReachMeleeAttackGoal`, `WindupMeleeAttackGoal`, `PredicateGatedGoal`
- `BetterCombatCompat.createMeleeGoal(...)` (Better Combat combo melee for mobs, a plain `MeleeAttackGoal` without the mod) with `MeleeSettings` (`baseRange`, `intervalScale`, `animations`, `resetInvulnerability`, `weaponTrails`), and `BetterCombatCompat.onGoalRemoved(goal)` to drop the weapon pose of a goal you remove
- `MobAnimations.playWeaponPose(mob, layer, poseId, twoHanded)`: plays the Better Combat weapon pose on `POSE` or `OFF_HAND_POSE`
- `MobGoals` (add goals to mobs you don't own, e.g. villagers) and `SpellCasterState` (cast timer to back an `ISpellCasterEntity` mixin)
- Dev-environment commands `/mrpg_goal <targets> bettercombat|combatroll [dodge]|spellcast <spell>|shoot [count]|clear` to test the custom goals on any mob.
- `AllyHelper` (`canHurt`, `canHelp`, `isAlly`): one ally check for projectiles, clouds, traps and AoE. Resolves owners (projectiles, pets, controlled mobs), checks teams and mob-vs-mob
- Asks Spell Engine's entity relations first when it is installed (`setRelationBridge`)
- `EntityAttributeConfig` / `EntityAttributeApplier`: a reusable, `ConfigManager`-backed way to give a registered entity configurable attributes
- base values are for the easy difficulty and there separate normal/hard difficulty multipliers
- Config entries can also carry a `custom` map of numbers (with the same difficulty multipliers) and string values (with optional `normal_string` and `hard_string` per difficulty), for mob tuning like cooldowns, projectile counts, ranges or summoned entity ids. Set defaults with `Entry.setCustom(...)` / `setCustomString(...)`, read them live with `EntityAttributeApplier.custom(entity, config, fallback)` (`getInt`, `getDouble`, `getBoolean`, `getString`)
- Config entries can also carry an `equipment` map (`mainhand`, `offhand`, `head`, `chest`, `legs`, `feet`) to equip mobs on spawn: an item id or a `#tag` (random item from the tag), with optional spawn `chance`, `enchant_chance` / `enchant_levels` / `enchant_tag`, `drop_chance`, and `required_mods` with a `fallback_item` for items from optional mods. Set defaults with `Entry.setEquipment(...)` and `EquipmentValue`
- `MobSpellCastGoal.withEquipmentSpells(true)` reads the caster's mainhand item for passive and modifier spells
- `withPassiveSpellTag(tag)` grants passive spells for the SpellCasting Entity from a spell tag instead

### Changed

- **Breaking:** Mod id and namespace renamed `more_rpg_classes` -> `mrpg_lib` (registry ids, spells, tags, loot functions and entries such as `mrpg_lib:conditional_item`, custom impact handlers such as `mrpg_lib:knock_up_fixed`)
- **Breaking:** Package renamed `net.more_rpg_classes` -> `com.mrpg_lib`, maven group `com.mrpg_lib`, archive base name `mrpg-lib`
- **Breaking:** Everything that touches Spell Engine, Spell Power, playerAnimator, Ranged Weapon API or Critical Strike moved into `com.mrpg_lib.compat.<name>`
- **Breaking:** `MoreParticles` split: the Spell Engine dependent particles moved to `compat.spell_engine.particle.MoreSpellParticles`; `client.particle.MoreParticles`
- **Breaking:** `CustomMethods` split: `spellSchoolDamageCalculation` and `isEntityProtectedCheck` moved to `SpellEngineMethods`; `isEntityProtectedCheck` is deprecated, use `AllyHelper.canHelp`, `getHighestSpellSchoolPower` to `SpellPowerMethods`
- **Breaking:** `StealthStatusEffect.stealthPopParticles()` is no longer abstract, it returns `Object` (default `null`) so the class no longer references Spell Engine types
- **Breaking:** `MobSpellCastGoal` runs in real time now: it ticks every game tick and tracks cooldowns against world time, so `updateCooldown()` is a no-op and no longer needs to be called
- Spell Engine and Spell Power are no longer required. Both are optional now.
- The mod id is now `mrpg_lib` it was `more_rpg_classes`.
- The config folder moved from `config/more_rpg_classes` to `config/mrpg_lib`.
- Frozen Solid, Soaked and Arcane Precision use standalone versions of the effects when Spell Power is not installed.
- Spells with a cast time now go off at the end of the cast, like for players, so the whole cast animation plays.
- Mob spells now respect the spell's delivery delay.
- Mobs mow charge spells like players. If charging adds range, they charge just enough to reach the target and use the spell from its full charged range.
- Otherwise, they charge fully, or release at the minimum charge when the target is very close.
- Buffed Burstcrack's Damage Power Coefficient

### Fixed

- Soaked now also makes targets vulnerable to lightning spells (with Spell Power), not only frost
- Wither's Curse now only counts harmful status effects on the target
- Mob-cast spells with an area impact now hit everything around the target and show their impact particles and sound.
- Mobs no longer use melee-range spells from far away
- Mob-cast area spells with melee range  now hit everything around the mob.

## [2.7.2+1.21.1]

### Removed

- Drop Forgified Fabric API (FFAPI) as a required dependency

## [2.7.1+1.21.1]

### Fixed

- Fixed an Issue that happened on Neoforge with the "More Armory" Item-Group

## [2.7.0+1.21.1]

### Changed

- Adopt Spell Engine 1.10 - Thanks Daedelus!

## [2.6.5+1.21.1]

### Changed

- Heavily Improve Entity Relations for "Controlled" Entities

### Fixed

- Fix multiple Controlled Enemies fighting each other (same owner)

## [2.6.4+1.21.1]

### Added

- Added some new player animations
- added a ControlEnemyStatusEffect Class. Affected HostileMobs attack other Hostile Mobs.

### Changed

- Affected Players change their EntityRelations (Hitting Allies with Damaging Spells for example)

## [2.6.3+1.21.1]

### Added

- Add a fix for FriendlyLightning Entities crashing, when they're spawned via commands
- Add two new Item Groups "More Armory" & "More Arsenal"

### Changed

- All my RPG Series Plus Armory & Arsenal Content will be stored in these Item Groups

## [2.6.2+1.21.1]

### Added

- Add generalized Stealth Status Effect for multiple projects using the Stealth Mechanic

### Changed

- Burst Crack is now a charged Weapon Skill -> increasing Damage & Range
- Deprecated a lot of velocity based Custom Spell Impacts

## [2.6.1+1.21.1]

### Changed

- PR: Update/Correction es_ar.json #24 - Texaliuz

### Fixed

- Fixed Mobs using the "MobSpellCastGoal", casting CLOUD spells with no set spell range all the time, with no enemies in sight

## [2.6.0+1.21.1]

### Changed

- Update for the newest Spell Engine Version
- Deprecate Bleeding Effect from this Mod, because Spell Engine now provides one

## [2.5.30+1.21.1]

### Changed

- PR #22 - Fix Armor Piercing Crash - Thx rexlManu
- Changed the Dragon Claw healing spell description from hearts to health to match rpg series descriptions

## [2.5.29+1.21.1]

### Fixed

- Fix Duelists Focus Renderer on Neoforge

## [2.5.28+1.21.1]

### Added

- Add Visual Renderer for the Duelists Focus Effect
- Add Particle Spawner for Sirens Tear Effect

## [2.5.27+1.21.1]

### Fixed

- fix music note particles causing lags on neoforge

## [2.5.26+1.21.1]

### Added

- New Weapon Passive: Dragonslayer's Fury

### Changed

- When applying heals or buffs on an ally under 20% max health, they gain increased crit damage
- SpellBuilderHelper Additions (Mostly RBG Color Codes)

### Fixed

- fix Duelists Focus not applying the effect to the caster

## [2.5.25+1.21.1]

### Added

- Added a new Status Effect for the Sirens Tear's passive, that regenerates health every second, the heal scales with missing health
- Added a new Melee Weapon Passive -> Duelist's Focus: You mark your actual target, all other targets expect the marked target deal 25% reduced damage to you, and only you deal 25% increased damage to the target

### Changed

- The Spellthief Custom Impact can now also perform Beam Spells

### Fixed

- Fixed the Range Passives applying cooldown on Range Weapons
- Fixed the Puncture Charge Animation

## [2.5.24+1.21.1]

### Added

- Add Puncture Weapon Skills for the Bard Rapier's
- Added the Armory Upgrade Gem for the Bard's T5 Armor Set

### Fixed

- Fixed crash happening after Launch on Neoforge

## [2.5.23+1.21.1]

### Added

- Add custom heart-renderer #19 - Rulft44
- added a new particle type - star
- added a new PopupParticle, that can render spell icon & mob effect textures
- added BackAway & Flee Goals for Entities (Mainly for LNE-Entities)
- Added a new Lightning Strike Passive Spell for Ranged Weapons
- Added all Weapon Passive Spells from Loot&Explore +Add On's to this project because they might be used by multiple projects
- Added some SpellBuilder Helper methods

### Changed

- Improved Mob Spellcasting with optional Intelligence Settings
- Weapon Skills from Berserker and Forcemaster got added to this project, so they could be used by other mods as well without having these mods installed

### Fixed

- Fixed Bleeding Effect Damage bypassing the Resistance Effect

## [2.5.22+1.21.1]

### Added

- Added Safeguards and Null checks to check if the entity even has the attribute that the custom function could run

### Fixed

- Fixed a crash due to LivingEntityMixin with the Ender Dragon attacking

## [2.5.21+1.21.1]

### Changed

- improved the isEntityProtectedCheck logic because Spell Impacts caused by spell spawned entities did sometimes not work on players that are on another team if pvp was enabled
- Rage, Spell Power Fuse, Lifesteal and all other attributes that have some technical function. can now all be used by all LivingEntities
- Heavily Improved MobSpellCastGoal, especially channeled spells like fire breath
- If you try to run away from a fire breath casting mob it'll follow you while still casting the spell
- When you come to close while a Mob is casting/channeling a Spell, the entity will walk slowly backwards

## [2.5.20+1.21.1]

### Fixed

- fixed an MobSpellCaster crash due to wrong class and method naming

## [2.5.19+1.21.1]

### Added

- Added SimpleSoundGeneratorV2 for Datagen

### Changed

- Improved the entityRelationCheck CustomMethod and renamed it to -> isEntityProtectedCheck
- Renamed CastSpellGoal to MobSpellCastGoal
- Improved the Goal and ISpellCasterEntity a lot to automatically recognize what Spell Type was chosen and how it needs to be casted
- A Guide how to add the SpellCastGoal to a Mob has been added in the README
- Bleeding no longer bypasses resistance
- Some Sound Effects got added for future content
- Magic Fuse Attacks now have a 20 tick cooldown to avoid click spamming

### Fixed

- Fix Death Messages and Fatal Poison - Pull Request #17

## [2.5.18+1.21.1]

### Changed

- Update for Spell Engine 1.9 Update

### Removed

- Delete SpellEngineTiersMixin

### Fixed

- Fix Spell Scroll Loot Functions
- Fix Spell Projectile and Status Effect Renders due to the Spell Engine API Changes

## [2.5.17+1.21.1]

### Added

- Added a new TerrainBlendingProcessor, which makes Path Structures bend smoother and more natural to terrain

### Fixed

- fixed some weird looking stuff for the ConditionalJigsawStructure & PathAdaptionProcessor

## [2.5.16+1.21.1]

### Added

- Added ConditionalJigsawStructure (Specially for LNE-Add-On's)
- Added PathAdaptionProcessor & WaterPillarProcessor for paths that bend to the environment (Specially for LNE-Add-On's)

## [2.5.15+1.21.1]

### Added

- Add new Music Note Particles

### Fixed

- Fix Fatal Poison's wrong Status Effect color
- fix Carve Status Effect reducing incoming damage instead of increasing it

## [2.5.14+1.21.1]

### Fixed

- Fix Datagen Helpers

## [2.5.13+1.21.1]

### Added

- Add Tenacity Attribute -> Everytime a Harmful Status Effect gets applied it will check the Tenacity Attribute
- Add entityRelationCheck & getRangedDamageAttribute Custom Methods
- add 2x new Datagen Helper Methods, for Advancements with Spell Engine Requirements & Generating Better Combat Weapon attribute data files

### Changed

- The Attribute has a 100 Base and 200 Max Value, 200 Means, that the Entity is fully Immune to Harmful Status Effects
- Trial-, Raid- & Bad-Omen Effects are excluded from that
- A value of 150 in Tenacity means, that there is a 50% chance of not applying the Harmful Status Effect

## [2.5.12+1.21.1]

### Added

- Added some particle Visuals to the Attribute Based Trigger Effects

### Changed

- All Attributes, that can inflict status effects, do now also work with ranged weapons
- Fuse Attributes now only deal Damage with Ranged Weapons
- Lifesteal now also works with Projectiles
- Lifesteal only triggers IF the player is actually removing health from the target
- The Frozen Solid, Fear, Stagger and Ignited Effect now shows their own Hud Message instead of "Stunned"

## [2.5.11+1.21.1]

### Added

- burning_chance -> Chance to ignite the target, dealing damage every 10 ticks, reducing the targets healing taken (scaled with attack damage)
- stagger_chance -> Chance to apply the Stagger Effect
- stun_chance -> Chance to apply the Stun Effect
- poison_chance -> Chance to apply the Poison Effect (scaled with attack damage)
- freeze_chance -> Chance to apply the Frozen Solid
- bleeding_chance -> Chance to apply the Bleeding Effect (scaled with attack damage)
- armor_piercing -> Ignores the Armor values of the target in %

### Fixed

- Fixed missing amplifier scaling for the Bleeding Effect
- Fixed Lifesteal healing before armor calculation
- Fix Rage Damage Calculation

## [2.5.10+1.21.1]

### Fixed

- fix wrong spell id for Melee lightning strike passive

## [2.5.9+1.21.1]

### Added

- Adding Particles, Sounds and Passive Spells from the Berserker Mod to this Library
- Add new HelperMethods, to determine the highest Spell power Attribute / Damaging Attribute
- Add FatalPoison Effect (Thanks Rulft44) Pull request #13

### Changed

- The damage dealt by the LightningEntity is now determined by the highest Attribute:
- (Generic Attack Damage, Ranged Weapon Damage & Magic Spell School)
- The Rage Attribute now impacts the Power of the RAGE_MELEE School more

## [2.5.8+1.21.1]

### Added

- Add FriendlyLightningEntity for the custom spell impact LightningStrikeImpact

## [2.5.7+1.21.1]

### Added

- SmithingRecipeGenerator -> with conditional required mod loading checks for fabric and neoforge
- CastSpellGoal & ISpellCasterEntity for Spell Engine Spell Casting Mob Entities

### Changed

- Activate Archers Expansion Armory Upgrade Gem

## [2.5.6+1.21.1]

### Added

- New LOOT_FUNCTION_TYPE -> add conditional Item (Checks if the item is registered, if not uses fallback item)
- New LOOT_FUNCTION_TYPE -> Bind Spell From Pools Function (Binds a random spell from pools to a weapon or jewelry for example)
- New LOOT_POOL_ENTRY_TYPE -> the item only gets added to the loot table, if its registered (without fallback item)

### Changed

- Update es_ar (Thanks Texalius)

## [2.5.5+1.21.1]

### Changed

- Activate Berserker Armory Upgrade Gem

## [2.5.4+1.21.1]

### Changed

- Activate Elemental Wizards Armory Upgrade Gems

### Fixed

- Fix Damage Reflect Mixin, casting Mobs to PlayerEntites

## [2.5.3+1.21.1]

### Fixed

- fix crash in dev environment

## [2.5.2+1.21.1]

### Added

- Added compatibility for Critical Strike mod for Rage Melee, Fire- and FrostRanged SpellSchool
- Add Armory Upgrade Crystal Compat for MRPG Classes

### Changed

- Change Loot Injection

## [2.5.1+1.21.1]

### Added

- add global elemental weaknesses for MoreSpellSchools

### Fixed

- Fix fabric.mod.json and neoforge.mods.roml
- Fix Particles in NeoForge Client

## [2.5.0+1.21.1]

### Changed

- Move to Architectury multiloader workspace
- NeoForge Beta!

## [2.4.3+1.21.1]

### Added

- Add a Cooldown to SpellVampire and Lifesteal Effect (Ticks configurable in the TweaksConfig)

### Changed

- frozen Ticks are now applied more smoothly

### Fixed

- Fix Bleeding Effect tick infinitely

## [2.4.2+1.21.1]

### Fixed

- fix falling icicle model

## [2.4.1+1.21.1]

### Added

- Add Nature Spell School to SpellSchoolMixin registry

### Changed

- Forgot some Damage Type Tags regarding the Nature Spell School

## [2.4.0+1.21.1]

### Added

- Add Nature Spell Power School and Rune & Leaf Particles (Thanks to Rulft44 for providing the artwork!)
- Add Sound Effects for Nature Spells

### Changed

- Update to Spell Engine 1.8 & Spell Power 1.4
- Also, Update Tiny Config & Fabric API Version
- Improve Earth Casting Sound Effect
- 2 new Enchantments got added: Typhoon (Air & Water) & Stonebloom (Earth & Nature)

### Removed

- Delete Elemental Mastery Enchantment (Spell Power just adds 2 spell schools per enchantment, thats why this was changed)

## [2.3.5+1.21.1]

### Added

- add bleeding immune entity tag

### Changed

- rename BERSERKER_MELEE Spell School to RAGE_MELEE

### Removed

- remove bleeding testing logger I forgot

## [2.3.4+1.21.1]

### Added

- added Falling Icicle Projectile
- Added the Frost- & Fire Ranged Spell School from the Archers Expansion Mod in this Lib
- Added the Berserker Melee Spell School from the Berserker Mod in this Lib
- new CustomSpellImpact: damage_according_to_missing_health

### Changed

- Buffed the Bleeding Effect
- it now deals additional damage according to the max health if the target is under a certain hp range
- The less health your target has, the more damage you cause (DamageType is your SpellSchools Damage Type)
- (above 50% -> 0.25 / below 50% -> 1.0 / below 25% -> 1.75 of your SpellSchools Attribute Power)

## [2.3.3+1.21.1]

### Added

- Add Specific Spell Scroll Loot Function (spell pool, min to max spell-tier can be set)

### Changed

- Reduce Damage Taken of Frozen Solid Spell and Add Frost Spell related vulnerability
- nerf Frosted Movement speed slow

### Fixed

- Fix Backward- and Forward dash, that was calculated by spell range

## [2.3.2+1.21.1]

### Added

- Add freezeDamageTicks HelperMethod
- Add Ice Trap & Stone Trap Particle (Credits: ElvGames)
- The Custom Spell Impact Knock Up's value can now be changed in the tweaks config
- Impact that spawns a lightning strike on the target (vanilla lightning entity)
- Pull To Caster Slowly Custom Impact
- Rush Forward Custom Impacts
- Disengage BackwardDash custom impacts
- scaled knockback according to how close the target is to the caster (closer = higher knockback)
- Impact that sets frozen Ticks to a maximum

### Changed

- Improve Frosted Particle Status Effect
- Improve stackFreezeStacks HelperMethod
- Frosted & FrozenSolid Status Effect now also slowly stack freezeTicks like Powder Snow Blocks

### Removed

- Removed Frostiful & EnviromentZ Compat (Extra Datapack will be released)
- Delete Poison Cloud and Gas Cloud particles because they're obsolete now

### Fixed

- fix stop arrow spell impact

## [2.3.1+1.21.1]

### Added

- Add Fixed Amount Knock Up Spell Impact
- Add Pull To Caster Spell Impact

### Changed

- Adjust Custom Knock Up Impact

## [2.3.0+1.21.1]

### Added

- Added "Fuse"-Attributes for all SpellSchools (expect Soul & Lightning), dealing additional magic damage per hit
- Add Soaked Effect from Elemental Wizards
- Add Water Drop Particle
- Add Slash Claw Particle
- Add Custom Spell Impacts (KnockUp & StopArrows)
- Add Custom SpellEntityPredicates (IsOnGround, IsWet, IsInWater)

### Changed

- Update es_ar lang file
- changed ArcaneFuse LifeSteal & Rage Mixin Logic due to incompatibility with Synitra Connector
- Update Mod Icon

### Removed

- Delete Stun Effect & Particles, because Spell Engine 1.7 now has a generic Stun Effect

### Fixed

- Fix: add untamed wilds compat for white hide

## [2.2.8+1.21.1]

### Changed

- Spell Engine 1.7 Update

## [2.2.7+1.21.1]

### Added

- add the stagger status effect
- add untamed wilds compat for white hide

### Changed

- molten armor now reduces the armor by division

## [2.2.6+1.21.1]

### Fixed

- fix clearNegativeEffects Method

## [2.2.5+1.21.1]

### Fixed

- fix grievous wounds effect and change it a bit

## [2.2.4+1.21.1]

### Added

- Add SpellEngineTiersMixin to mixin.json

### Fixed

- fix some animations

## [2.2.3+1.21.1]

### Added

- add fireproof tiers mixin for spell engine weapon registry

### Changed

- improve some animation files

### Fixed

- fix frosted status effect particle error

## [2.2.2+1.21.1]

### Added

- add es_ar translation

### Changed

- the Bleeding Effect can now kill

## [2.2.1+1.21.1]

### Added

- add elemental runes to rune item tag for rune pouches
- add sounds, particles and player animations for Loot & Explore 1.21 Port
- Add Spell Vampire Attribute (LifeSteal for Spell Magic Damage)

### Fixed

- fix DamageReflect Loop with Thorns GitHub Issue #4

## [2.2.0+1.21.1]

### Changed

- Spell Engine 1.5 Prep Update
- disabled executeSpellSpellEngine Method, will probably not be needed in the future
- Update to Fabric Loom 1.9

### Fixed

- fixed CustomMethods according to API Changes

## [2.1.1+1.21.1]

### Changed

- clean up PlayerEntityMixin

### Fixed

- fix custom attributes not stacking

## [2.1.0+1.21.1]

### Added

- created "vulnerable_to_water_spells" entity type tag, for water spell power spells
- created "resistant_to_water_spells" entity type tag, for water spell power spells

### Changed

- Spell Engine 1.4 Update

## [2.0.15+1.21.1]

### Fixed

- Fix applyStatusEffect Method

## [2.0.14+1.21.1]

### Changed

- Rage Damage Calculation was changed to % of missing health instead of missing health amount
- This change was done due to rage being too broken in modded environments where health gets increased
- The Rage Attribute still enhances this calculation on increased damage

## [2.0.13+1.21.1]

### Changed

- Stunned Effect Error Hotfix

## [2.0.12+1.21.1]

### Added

- added stun immunity entity type tag

## [2.0.11+1.21.1]

### Changed

- forgot Runes - Mod dependency causing a crash

### Fixed

- fixed an issue apply status effect Helper Method

## [2.0.10+1.21.1]

### Changed

- Frosted Effect cant be applied if the entity has the Frozen Solid Effect
- changed frozen ticks
- Freeze Immune Entities are not affected by Frosted & Frozen Solid Effect
- Frozen Solid Effect Visual Model now scales with target

## [2.0.9+1.21.1]

### Added

- added a Poison Smoke particle

### Changed

- BUGFIX: Fixed the error occurring when leaving the server causing the client crashing the game (something with the registry of my sounds was wrong)
- increased Frosted Particle Count

### Fixed

- fixed a typo in poison smoke particle

## [2.0.8+1.21.1]

### Added

- added Poison Gas Cloud Particles

### Changed

- Changed Frosted & Frozen Solid Effect a tiny bit
- changed the model for the Frozen Solid effect

### Fixed

- fixed a calculation mistake with the Rage Attribute

## [2.0.7+1.21.1]

### Changed

- forgot to include a wind particle from the 1.20.1 version

## [2.0.6+1.21.1]

### Added

- added the wind charge item to the storm rune recipe material
- added air magic sounds for the Wind Elemental Wizard

## [2.0.5+1.21.1]

### Added

- added 15% multiplier for the rage attribute in the simply skills compat datapack

### Changed

- made rage damage calc clearer, configurable & changed it, so rage has more impact the more you have
- made playerentitymixin code more clean for better visibility

## [2.0.4+1.21.1]

### Changed

- bleeding effect now gets removed correctly when the target is in undead tag
- damage types for air, earth and water spell school

### Fixed

- fixed some wrong animation names
- fixed an error with enchanting not working

## [2.0.3+1.21.1]

### Changed

- made Elemental Mastery translation more clear
- forgot to change item to id in the advancement section

### Fixed

- fixed an error with some status effects
- fixed issues with some Helper Methods and deleted unused ones
- fixed a tag issue with enchantments

## [2.0.2+1.21.1]

### Changed

- translation was not assigned correct to attributes in lang files

## [2.0.1+1.21.1]

### Changed

- forgot to change the new data folder structures (recipes -> recipe, items -> item, etc.)
- forgot to change to 1.21 recipe json structure

## [2.0.0+1.21.1]

### Changed

- 1.21.1 Update
