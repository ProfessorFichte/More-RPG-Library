# Structure Block Modifiers

Structure processors only work in structures that are built from template files. Many structures aren't: strongholds, mineshafts, nether fortresses, woodland mansions, desert and jungle temples are built in code.
Structure block modifiers work for every structure, vanilla and modded, and you don't have to change any of its files.
They run right after a part of a structure (a room, a corridor, a jigsaw piece) is placed and replace some of its blocks.

Put the files in `data/<namespace>/mrpg_lib/structure_block_modifier/<name>.json`. `/reload` reloads them, but only new chunks use the new values.
This is the one Loot & Explore uses for stronghold libraries:
```json
{
  "structures": ["minecraft:stronghold"],
  "pieces": ["minecraft:shli"],
  "target": "minecraft:bookshelf",
  "chance": 0.03,
  "max_per_piece": 4,
  "replacement": { "Name": "loot_n_explore:suspicious_bookshelf" },
  "loot_table": "loot_n_explore:archaeology/stronghold_library"
}
```

| Field | Required | What it does |
| --- | --- | --- |
| `structures` | yes | Structure ids or structure tags, for example `"minecraft:stronghold"` or `"#minecraft:village"` |
| `pieces` | no (all) | Only these parts of the structure, as structure piece type ids. Stronghold library: `minecraft:shli`. Jigsaw structures use `minecraft:jigsaw` for every piece |
| `target` | yes | The block to replace: a block id or a block tag |
| `chance` | yes | Chance for every target block to be replaced, from `0.0` to `1.0` |
| `max_per_piece` | no (no limit) | At most this many replacements per piece. A piece that crosses chunk borders is handled once per chunk, so it can get this many in each chunk |
| `replacement` | yes | The new block as a block state: `{ "Name": "minecraft:chest", "Properties": { "facing": "north" } }` |
| `loot_table` | no | Loot table for the new block. Works for chests, barrels, shulker boxes and suspicious blocks. Suspicious blocks give only one item, so use a table with one roll (type `minecraft:archaeology`) |
| `required_mods` | no | Mod ids. If one of them isn't installed, the file is skipped without an error. Use it when `replacement` comes from another mod |

Good to know:
- It's a chance per block and not "exactly 2 per room", because code-built rooms are placed one chunk at a time and no step sees the whole room. For a fixed number in template structures, use the random replace processor.
- The same world seed always picks the same blocks.
- A file with an unknown block in `replacement` is skipped and the log says why. The world still loads. Unknown piece types and invalid ids show a warning.
- Common piece type ids: stronghold library `minecraft:shli`, stronghold chest corridor `minecraft:shcc`, mineshaft corridor `minecraft:mscorridor`, nether fortress pieces start with `minecraft:ne` (for example `minecraft:nestart`, `minecraft:nebcr`; `minecraft:nefos` is the nether fossil), desert temple `minecraft:tedp`, jungle temple `minecraft:tejp`, woodland mansion `minecraft:wmp`.

For mod developers: `com.mrpg_lib.datagen.StructureBlockModifierProvider` writes these files with datagen.
```java
public class MyModifierProvider extends StructureBlockModifierProvider {
    public MyModifierProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    protected void generate(BiConsumer<Identifier, StructureBlockModifier> exporter) {
        exporter.accept(Identifier.of("my_mod", "stronghold_bookshelves"), new StructureBlockModifier(
                List.of("minecraft:stronghold"), List.of("minecraft:shli"), "minecraft:bookshelf", 0.03F, 4,
                MyBlocks.SUSPICIOUS_BOOKSHELF.getDefaultState(),
                Optional.of(Identifier.of("my_mod", "archaeology/stronghold_library")), List.of()));
    }
}
```
