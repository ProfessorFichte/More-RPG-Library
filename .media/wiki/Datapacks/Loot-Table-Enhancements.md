# Loot Table Enhancements

The spell scroll and spell pool functions only do something when Spell Engine is installed. The conditional item function and entry work without it.

**Loot tables that should also work without Spell Engine**

Minecraft throws away the whole loot table if one part of it can't be read, for example an item or a function from a mod that isn't installed. A suspicious block or chest with that table then gives nothing at all.
- Without Spell Engine, `mrpg_lib:specific_spell_scroll_pool` and `mrpg_lib:bind_spell_from_pools` still load, but do nothing.
- The scroll item itself (`spell_engine:spell_scroll`) is still missing. So don't add it as a normal `minecraft:item` entry, use the conditional item entry below. Without Spell Engine the entry is left out and the other entries of the pool are rolled instead.
```json
{
  "type": "mrpg_lib:conditional_item",
  "item": "spell_engine:spell_scroll",
  "weight": 2,
  "functions": [
    {
      "function": "mrpg_lib:specific_spell_scroll_pool",
      "spell_pools": ["#wizards:frost"],
      "spell_tier_min": 1,
      "spell_tier_max": 2,
      "count": 1
    }
  ]
}
```
With datagen: `ConditionalItemEntry.builder(Identifier.of("spell_engine", "spell_scroll"), Optional.empty())`. It works like `ItemEntry.builder`, with `.weight()` and `.apply()`.

With Spell Engine but without spells in the chosen pools and tiers (for example no class mods installed), the scroll function finds nothing and the roll gives nothing.
Add the condition `mrpg_lib:spells_available` with the same pools and tiers to the entry, then the entry only takes part when there are matching spells, otherwise one of the other entries is picked. Without Spell Engine the condition is always false.
```json
{
  "type": "mrpg_lib:conditional_item",
  "item": "spell_engine:spell_scroll",
  "weight": 8,
  "conditions": [
    { "condition": "mrpg_lib:spells_available", "spell_tier_min": 3, "spell_tier_max": 4 }
  ],
  "functions": [
    { "function": "mrpg_lib:specific_spell_scroll_pool", "spell_tier_min": 3, "spell_tier_max": 4, "count": 1 }
  ]
}
```
`spell_pools`, `spell_tier_min` and `spell_tier_max` of the condition are optional. With datagen: `SpellsAvailableLootCondition.builder(pools, tierMin, tierMax)` and `SpecificSpellScrollPoolLootFunction.builder(...)` (these two need Spell Engine on the datagen classpath).

**Spell scroll from specific spell pools 📜**

Adds a spell scroll with a spell from the given pools. You can limit the spell tier and blacklist spells.
Without `spell_pools` every spell can be picked. This example uses a normal item entry, so the table only loads with Spell Engine installed. For tables that should also work without it, use the conditional item entry from above.
```json
{
  "type": "minecraft:item",
  "name": "spell_engine:spell_scroll",
  "functions": [
    {
      "function": "mrpg_lib:specific_spell_scroll_pool",
      "spell_pools": ["#wizards:frost", "#wizards:fire"],
      "spell_tier_min": 3,
      "spell_tier_max": 4,
      "count": 1,
      "blacklist_spells": ["wizards:frost_blizzard", "wizards:fire_meteor"]
    }
  ]
}
```

**Bind spells from spell pools on an item**

Binds a random spell from the pools to the item. If the item is not a spell container yet, it becomes one.
```json
{
  "type": "minecraft:item",
  "name": "minecraft:diamond_sword",
  "functions": [
    {
      "function": "mrpg_lib:bind_spell_from_pools",
      "spell_pools": ["#wizards:arcane", "#wizards:frost"],
      "count": 1
    }
  ]
}
```

**Conditional item with fallback**

Drops the conditional item if it is registered, otherwise the entry's own item is used as the fallback.
```json
{
  "type": "minecraft:item",
  "name": "minecraft:diamond",
  "functions": [
    {
      "function": "mrpg_lib:conditional_item",
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

**Conditional item entry**

A loot pool entry without a fallback. If the item is not registered, the entry is left out of the roll, so one of the other entries of the pool is picked instead, and the loot table keeps working.
```json
{
  "type": "mrpg_lib:conditional_item",
  "item": "your_mod:jade_gem",
  "count": {
    "type": "minecraft:uniform",
    "min": 1,
    "max": 3
  }
}
```
