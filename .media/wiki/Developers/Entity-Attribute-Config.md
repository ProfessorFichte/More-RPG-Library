# Entity Attribute Config

A small config shape (`com.mrpg_lib.entity.config.EntityAttributeConfig`) for giving your registered entities configurable attributes, so players and modpack makers can retune them without touching your code.
`EASY` is the base value you set. `NORMAL` and `HARD` each get their own multiplier on top, `PEACEFUL` uses the base value like `EASY` does.

Build your defaults once, keyed by entity id:
```java
public static final EntityAttributeConfig DEFAULTS = new EntityAttributeConfig();
static {
    DEFAULTS.entries.put("mymod:frost_wraith", new EntityAttributeConfig.Entry()
            .set("minecraft:generic.max_health", 40.0, 1.25, 1.5)
            .set("spell_power:frost", 3.0, 1.25, 1.5));
}
```
Persist it the same way you already persist any other config in your mod, with `ConfigManager<EntityAttributeConfig>` (TinyConfig):
```java
public static final ConfigManager<EntityAttributeConfig> entityAttributeConfig =
        new ConfigManager<>("entity_attributes_v1", DEFAULTS).builder().setDirectory(MOD_ID).sanitize(true).build();
```
Call `entityAttributeConfig.refresh()` once during `init()` so the json file on disk exists and is loaded, then apply it whenever the entity is created or its attributes are set up:
```java
EntityAttributeApplier.apply(this, Identifier.of("mymod", "frost_wraith"),
        entityAttributeConfig.value, DEFAULTS.entries.get("mymod:frost_wraith"));
```
This reads the live persisted entry if the user has one, and only falls back to your hardcoded `Entry` if the config has nothing for that id yet.
`EntityAttributeApplier.apply(entity, entry)` on its own is there too, if you already have the right `Entry` and don't need the config lookup.

If an entity needs an attribute that isn't already on it (a custom one like `spell_power:frost`, or one the entity type doesn't carry by default), add it to the attribute container while you build it: `EntityAttributeApplier.registerCustomAttribute(builder, "spell_power:frost", 0.0)` inside `createAttributes()`.
The value you pass there is just a placeholder, the real value comes from `apply(...)` afterwards.

**Custom values.** Besides attributes, an entry can carry a `custom` map for any other number or string your mob code wants to make tunable: ability cooldowns, projectile or summon counts, ranges, damage multipliers, entity ids. Numbers use the same `base` / `normal_multiplier` / `hard_multiplier` shape as attributes, strings go into `string`, and `normal_string` and `hard_string` can replace it on Normal and Hard difficulty (anything you leave out falls back to `string`). The attribute applier ignores this map; your own code reads it.
```json
"mymod:frost_wraith": {
  "attributes": { "minecraft:generic.max_health": { "base": 40.0 } },
  "custom": {
    "salvo_arrow_count": { "base": 5.0, "hard_multiplier": 1.4 },
    "summon_entity": { "string": "mymod:ice_wisp", "normal_string": "mymod:frost_wisp", "hard_string": "mymod:storm_wisp" }
  }
}
```
Add the defaults to the same `Entry`: `.setCustom("salvo_arrow_count", 5)`, `.setCustom(id, base, normal, hard)` or `.setCustomString("summon_entity", "mymod:ice_wisp")`. The map is left out of the JSON when an entry has none.
Read them where the value is used, not once in `initialize()`, because mobs loaded from a saved world never run `initialize()` again:
```java
CustomValues tuning = EntityAttributeApplier.custom(this, entityAttributeConfig.value, DEFAULTS.entries.get("mymod:frost_wraith"));
int arrows = tuning.getInt("salvo_arrow_count", 5);
String summon = tuning.getString("summon_entity", "mymod:ice_wisp");
```
`getInt` (rounded), `getDouble` and `getBoolean` (0 = false) apply the multiplier for the mob's current world difficulty. Each key is looked up in the config entry first, then in the fallback `Entry`, then the default you pass, so a config being `null` is fine too. `Entry` itself has the same getters with an explicit `Difficulty`.

> **Note:** a config file that already exists does not get new custom keys added automatically: the file is loaded as it is and saved back unchanged. Missing keys silently use the fallback `Entry`, so nothing breaks, but players only see the new keys after deleting the file and letting it regenerate, or by adding them by hand.

**Equipment.** An entry can also carry an `equipment` map that gives the mob items when it spawns. The keys are the slot names `mainhand`, `offhand`, `head`, `chest`, `legs` and `feet`. `item` is an item id, or an item tag written as `#namespace:tag`, in which case a random item from that tag is picked for every spawn. `minecraft:air` empties the slot on purpose.
```json
"mymod:frost_wraith": {
  "equipment": {
    "mainhand": { "item": "minecraft:iron_sword", "drop_chance": 0.0 },
    "head": { "item": "#minecraft:head_armor", "chance": 0.5, "enchant_chance": 0.25, "enchant_levels": 20 },
    "offhand": { "item": "wizards:wand_frost", "required_mods": ["wizards"], "fallback_item": "minecraft:stick" }
  }
}
```
| Field | Default | Meaning |
|---|---|---|
| `item` | required | item id or `#tag` |
| `chance` | `1.0` | chance (0 to 1) that the slot gets equipped at all. If the roll fails the slot is left as it was |
| `enchant_chance` | `0.0` | chance (0 to 1) that the item gets enchanted |
| `enchant_levels` | `20` | enchanting power, like the level cost at an enchanting table (30 is the table's maximum) |
| `enchant_tag` | `#minecraft:in_enchanting_table` | enchantment tag the enchantments are picked from |
| `drop_chance` | vanilla | chance to drop the item on death (`0.0` never, `1.0` always) |
| `required_mods` | none | list of mod ids; the item is only used if at least one of them is loaded |
| `fallback_item` | none | item id or `#tag` used instead when no required mod is loaded or `item` does not exist. It uses the same chance and enchant settings. Without it, the slot is left as it was |

Unknown items, empty tags and unknown slot names are skipped with a single warning in the log. Slots you leave out keep whatever the mob already has, and an entry without an `equipment` map does not touch the mob's equipment at all. Config slots are merged with the fallback `Entry` slot by slot, so a config entry that only lists `head` still gets the fallback's `mainhand`.
Defaults go on the same `Entry`: `.setEquipment(EquipmentSlot.MAINHAND, "minecraft:iron_sword")`, `.setEquipment("head", "#minecraft:head_armor", 0.5, 0.25)` (chance, enchant chance), or a full `EquipmentValue`:
```java
.setEquipment(EquipmentSlot.MAINHAND, EntityAttributeConfig.EquipmentValue.of("wizards:wand_frost")
        .requiresAnyMod("wizards").fallback("minecraft:stick").enchant(0.25, 20).dropChance(0.0))
```
Equipment is applied by `apply(...)` together with the attributes and is saved with the mob, so it only affects freshly spawned mobs, not ones already saved in a world. Only `MobEntity` subclasses can get equipment. Whether an item is visible depends on the mob's renderer: armor needs an armor feature renderer and held items a held item feature renderer, but the item's attribute bonuses apply either way. Existing config files don't gain the `equipment` map on their own (see the note above).
