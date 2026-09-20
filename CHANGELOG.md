# 2.7.2+1.20.1

> ### ⚠️ Read this before updating
>
> This release is a **major technical overhaul and is not backwards compatible.**
>
> - **Requires the matching Spell Engine release.** This version will not run on Spell Engine
>   **0.9.x**, and mods built against 0.9.x will not work alongside it.
> - **Update the whole set together.** Spell Engine and every RPG Series mod must be on
>   matching versions. Mixing in an older add-on will break at startup or misbehave in play.
>
> **Back up your world before updating.**

- Thanks to Daedelus for the PR!
- Ported to Minecraft 1.20.1 (Fabric + Forge 47). NeoForge is replaced by Forge on this line; the same
  Forge jar also loads on NeoForge 1.20.1.
- Requires the matching 1.20.1 releases of Spell Engine (1.10.5) and Spell Power (1.6.0).
- Loot-table modification and the mob-beam packet were rebuilt on Spell Engine's platform seams, so both
  loaders share one implementation.
- Every registry write goes through Forge's `RegisterEvent` window, so the mod also boots on Forge 47.0-47.3
  and on NeoForge 1.20.1, which never unlock the vanilla registries.

### Accepted 1.20.1 limitations

- The `typhoon` and `stonebloom` enchantments are registered from Java instead of shipping as data-driven
  enchantments (1.20.1 has none). Weights, max level, costs, slots and the multi-school exclusivity are
  unchanged, but they can no longer be overridden or disabled from a datapack.
- 1.20.1 has no entity-scale attribute, so everything that read a living entity's scale now uses 1.0.
- Trial Omen and Raid Omen do not exist on 1.20.1: `clearNegativeEffects` no longer clears Trial Omen, and
  the Tenacity resist check covers Bad Omen only.
- Tag entries naming 1.21-only content (`bogged`, `breeze`, `wind_charge`) are declared optional and are
  simply absent.
