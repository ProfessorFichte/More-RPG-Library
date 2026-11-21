# Structure Processors

Processors change the blocks of a structure template while it is placed. They work in every structure that places templates, jigsaw structures included.

**How to use them**

Put the processors in a processor list, for example `data/your_mod/worldgen/processor_list/library.json`:
```json
{
  "processors": [
    { "processor_type": "mrpg_lib:random_replace", "target": "minecraft:bookshelf", "count": 2,
      "replacement": { "Name": "loot_n_explore:suspicious_bookshelf" }, "loot_table": "loot_n_explore:archaeology/bookshelf" }
  ]
}
```
Then point the template pool element at it with `"processors": "your_mod:library"`. You can also write the list right into the pool element instead of the id.
Processors run in the order of the list, and each one sees the blocks the processors before it already changed.

A typo or a block from a mod that isn't installed in `target`, `filler_block`, `corner_block` or `blend_block` only shows a warning in the log, and that processor is skipped.
Blocks in `replacement` and `fallback` of random replace are checked when the datapack loads, like in vanilla processors. An unknown block there stops the processor list from loading.
For blocks from optional mods, only load the processor list when that mod is installed (Fabric `fabric:load_conditions`, NeoForge `neoforge:conditions`).
Give the same condition to everything that points at it (template pool, structure, structure set), otherwise those fail to load instead.

**Random replace**

Picks random blocks from one structure piece and replaces them, for example 2 of the 60 bookshelves in a library become suspicious bookshelves with a loot table, or every bookshelf has a 15% chance to become an icy one.

| Field | Required | What it does |
| --- | --- | --- |
| `target` | yes | The block to look for: a block id or a block tag (`#minecraft:logs`) |
| `count` | `count` or `chance` | How many of them get replaced: a number, or an int provider like `{ "type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 3 }`. Together with `chance` it is the most that get replaced |
| `chance` | `count` or `chance` | Chance for every target block to be replaced, from `0.0` to `1.0` |
| `replacement` | yes | The new block as a block state: `{ "Name": "minecraft:chest", "Properties": { "facing": "north" } }` |
| `keep_properties` | no (`false`) | The new block takes over every block state property of the old block that it has too (same name, same value), for example `facing`, `axis`, `waterlogged` or `variant`. These win over `Properties` in `replacement` |
| `loot_table` | no | Loot table for the new block. Works for chests, barrels, shulker boxes, decorated pots and suspicious blocks |
| `fallback` | no | Block state for the blocks that weren't picked. Without it they stay as they are |
| `blend_fallback` | no (`false`) | The blocks that weren't picked copy the most common full block next to them in the template, for example the wall they are in. Blocks with a block entity are never copied. If nothing is found, `fallback` is used |

Library example: 2 bookshelves become suspicious bookshelves, the other bookshelves stay normal.
```json
{
  "processor_type": "mrpg_lib:random_replace",
  "target": "minecraft:bookshelf",
  "count": 2,
  "replacement": { "Name": "loot_n_explore:suspicious_bookshelf" },
  "loot_table": "loot_n_explore:archaeology/bookshelf"
}
```

Marker example: build black stained glass where a chest could be. 2 or 3 of them become loot chests, the rest blend into the wall or become stone bricks.
```json
{
  "processor_type": "mrpg_lib:random_replace",
  "target": "minecraft:black_stained_glass",
  "count": { "type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 3 },
  "replacement": { "Name": "minecraft:chest", "Properties": { "facing": "north" } },
  "loot_table": "minecraft:chests/simple_dungeon",
  "fallback": { "Name": "minecraft:stone_bricks" },
  "blend_fallback": true
}
```
Chance example: every deepslate bookshelf has a 20% chance to become an icy one. Use it in the processor list of every structure that should get it.
```json
{
  "processor_type": "mrpg_lib:random_replace",
  "target": "loot_n_explore:deepslate_bookshelf",
  "chance": 0.2,
  "replacement": { "Name": "loot_n_explore:icy_deepslate_bookshelf" },
  "keep_properties": true
}
```

Good to know:
- The count is per structure piece, not for the whole structure. A jigsaw structure with 4 library pieces gets 2 suspicious bookshelves in each piece.
- With `chance` every block rolls on its own, so a piece can get none or many. Add `count` to cap it.
- To turn some bookshelves into suspicious ones and some of the rest into another look, put the suspicious one first in the list. The second processor then only sees the bookshelves that are left.
- If a piece has fewer target blocks than the count, all of them are replaced.
- The replacement is rotated together with the piece, so `facing` is the direction it has in the template.
- The same structure at the same place always picks the same blocks, so the picks don't change between the chunks a piece is placed in.

**Container loot**

Gives blocks with an inventory a loot table, without changing the block. Everything else stays as it is in the template: the facing, the pattern, the sherds of a decorated pot.
Works for every block that reads a `LootTable` like vanilla: chests, barrels, shulker boxes, hoppers, dispensers, droppers, decorated pots and suspicious blocks.

| Field | Required | What it does |
| --- | --- | --- |
| `target` | yes | The block to look for: a block id or a block tag |
| `loot_table` | yes | The loot table |
| `chance` | no (`1.0`) | Chance for every target block to get the loot table, otherwise it stays as it is (for pots: empty) |
| `replace_existing` | no (`false`) | Also replace a loot table that the block already has in the template |

Pot example: every frozen pot has a 25% chance to hold something.
```json
{
  "processor_type": "mrpg_lib:container_loot",
  "target": "loot_n_explore:frozen_decorated_pot",
  "loot_table": "loot_n_explore:pots/frozen_depths",
  "chance": 0.25
}
```
A decorated pot has one slot, so its loot table should give exactly one stack: one pool with one roll. The stack can be 1 item or more, for example with `minecraft:set_count`. The pot rolls it the first time its content is used (the pot breaks, a player puts an item in, a hopper pulls from it), like the pots in trial chambers.
Loot table type `minecraft:chest` works for pots, chests and barrels, suspicious blocks use `minecraft:archaeology`.

**Item frame items**

Gives item frames in a structure a random item from a list. Save the template with entities (structure block option "Include entities"), otherwise there are no frames in it.

| Field | Required | What it does |
| --- | --- | --- |
| `items` | yes | Item ids and item tags (`#minecraft:swords`). Items from mods that aren't installed are skipped, so you can mix items from different mods |
| `entities` | no (`item_frame`, `glow_item_frame`) | Which entities count as frames |
| `only_empty` | no (`true`) | Only frames that are empty in the template get an item. Set it to `false` to also replace items you put in by hand |
| `chance` | no (`1.0`) | Chance per frame to get an item, otherwise the frame stays empty |

```json
{
  "processor_type": "mrpg_lib:item_frame_items",
  "items": ["minecraft:diamond", "#minecraft:swords", "jewelry:ruby"],
  "chance": 0.75
}
```
Every frame rolls on its own, so two frames can get the same item. Every entry in the list has the same chance, and a tag counts as all of its items, so `#minecraft:swords` is picked more often than one single item. To make an item more common, list it more than once.
Invisible frames stay invisible, the rotation of the frame and of the item stays as it was in the template.

**Paths: path adaptation, water pillar and terrain blending**

These three turn marker blocks in a path template into blocks that fit the ground below them. They also work with `"projection": "terrain_matching"`, they look at the ground where the block really ends up.

`mrpg_lib:path_adaptation` turns every `filler_block` into the `output` of the first `terrain_mappings` entry that matches the block below it (block id or `#tag`), otherwise into `fallback_block`. Above water it becomes `water_output`. With `remove_floating_blocks` a filler block with no ground in the 2 blocks below it is left out.
```json
{
  "processor_type": "mrpg_lib:path_adaptation",
  "filler_block": "minecraft:pink_stained_glass",
  "terrain_mappings": [
    { "terrain": "#minecraft:dirt", "output": "minecraft:dirt_path" },
    { "terrain": "minecraft:sand", "output": "minecraft:sandstone" }
  ],
  "water_output": "minecraft:oak_planks",
  "fallback_block": "minecraft:dirt_path",
  "remove_floating_blocks": true
}
```
`stop_on_water`, `water_check_radius` and `water_threshold` are still accepted but do nothing. A processor can't stop a jigsaw path, the pieces are chosen before any block is placed.

`mrpg_lib:water_pillar` is for bridge corners. If there is water below a `corner_block`, it becomes `pillar_block`, a pillar of that block goes down through the water (up to `max_pillar_depth`) and a `fence_block` is put on top. On land the corner works like a path block: `terrain_mappings`, then the surrounding terrain if `blend_with_terrain` is on, then `fallback_block`.
```json
{
  "processor_type": "mrpg_lib:water_pillar",
  "corner_block": "minecraft:yellow_stained_glass",
  "pillar_block": "minecraft:oak_log",
  "fence_block": "minecraft:oak_fence",
  "max_pillar_depth": 16,
  "terrain_mappings": [ { "terrain": "#minecraft:dirt", "output": "minecraft:dirt_path" } ],
  "fallback_block": "minecraft:dirt_path",
  "sample_radius": 2,
  "blend_with_terrain": true
}
```

`mrpg_lib:terrain_blending` turns every `blend_block` into the most common terrain block around it (dirt becomes grass), with a small random `variation_chance` (moss and podzol on grass, cobblestone, mossy cobblestone and andesite on stone, sandstone and gravel on sand, ice on snow). Only full terrain blocks count, never blocks of the structure itself. Over deep water the block is left out. Without any terrain around it becomes `fallback_block`.
```json
{
  "processor_type": "mrpg_lib:terrain_blending",
  "blend_block": "minecraft:lime_stained_glass",
  "sample_radius": 2,
  "fallback_block": "minecraft:grass_block",
  "match_below": true,
  "match_horizontal": true,
  "variation_chance": 0.15
}
```
