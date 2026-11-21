# Custom Spell Impacts

Needs Spell Engine installed. These handlers are referenced from a spell's `impacts` list in its spell JSON:
```json
  "impacts": [
    {
      "action": {
        "type": "CUSTOM",
        "custom": {
          "intent": "HARMFUL",
          "handler": "mrpg_lib:knock_up_fixed"
        }
      }
    }
  ]
```

| Handler | What it does |
|---|---|
| `mrpg_lib:knock_up` | Knocks the target up (scalable). |
| `mrpg_lib:knock_up_fixed` | Knocks the target up by a fixed amount (0.75 Y velocity by default). |
| `mrpg_lib:stop_arrows` | Stops arrows, their velocity is set to 0. |
| `mrpg_lib:lightning` | Summons a lightning strike on the target. |
| `mrpg_lib:trembling` | Throws the target around in random directions. |
| `mrpg_lib:range_scaled_knockback` | Knockback that gets stronger the closer the target is to the caster. |
| `mrpg_lib:frozen_ticks` | Adds freezing ticks to the target. |
| `mrpg_lib:damage_according_to_missing_health` | Damage based on the target's missing health, scaled with the caster's spell school power: 0.25× above 50% health, 1.0× below 50%, 1.75× below 25%. |
| `mrpg_lib:spellthief_impact` | Steals all beneficial status effects of the target, then casts one of the target's spells: a random active spell of a player, or a random spell from a spell-casting mob's `<namespace>:mob/<entity>/...` spell tags. |
| `mrpg_lib:pull_to_caster_direct` | **Deprecated.** Pulls the target directly in front of the caster. |
| `mrpg_lib:pull_to_caster_slow` | **Deprecated.** Pulls the target slowly to the caster (for channeled spells). |
| `mrpg_lib:rush_forward_to_target` | **Deprecated.** The caster rushes forward to the target. |
| `mrpg_lib:backward_dash_fixed` | **Deprecated.** The caster dashes back a fixed distance. |
| `mrpg_lib:backward_dash_range` | **Deprecated.** The caster dashes back, scaled with the spell's `range`. |
| `mrpg_lib:forward_dash_range` | **Deprecated.** The caster dashes forward, scaled with the spell's `range`. |

The deprecated handlers still work, but for new spells Spell Engine's own `VELOCITY` impact is the better choice.
Strengths like the knock up height, pull speed, dash range and the missing health multipliers can be tuned in `config/mrpg_lib/tweaks_v2.json`.
