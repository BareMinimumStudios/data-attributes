# Data Attributes configuration

Data Attributes uses Fzzy Config for its server-owned configuration on both Fabric and NeoForge. The same logical model is used on both loaders, while each loader compiles against its own Fzzy Config artifact.

## Priority model

Configuration is resolved in this order, from lowest to highest priority:

1. Vanilla/native attribute behavior.
2. Loaded Data Attributes datapacks.
3. `serverConfig` from Fzzy Config.

The **Datapack Baseline Preview** is informational. It shows layer 2 before layer 3 is merged over it. It is intentionally transient: preview values are not written to the Fzzy JSON5 file and edits made in a preview popup are discarded.


## Applying changes live

There are two separate reload paths:

- **Server Configuration / Fzzy Config:** an accepted change made through the in-game Fzzy screen calls the server update hook immediately. The effective model is rebuilt, clients are synchronized, and already-loaded living entities rebuild their attribute map at the start of their next tick. No `/reload` or restart is required.
- **Datapack Baseline:** edit JSON under the Data Attributes datapack directories and run vanilla `/reload`. The reload listener reconstructs the datapack layer first, then reapplies Server Configuration over it. Existing loaded entities use the same next-tick rebuild path.

Vanilla `/reload` is for datapacks/resources; it is not treated as a generic disk watcher for the persisted Fzzy JSON5 file. Prefer the in-game Fzzy editor for live changes.

During a live entity rebuild, runtime modifiers are retained. A base value that was still using the previous configured/default supplier value follows the new configured value; a base value independently changed at runtime is preserved. Lowering max health does not heal an entity; vanilla clamps health down only when it is above the new maximum. Raising max health likewise does not automatically heal the entity.

## Identifier fields

Attribute and entity keys provide registry-backed suggestions, but Data Attributes does not require every typed identifier to exist in the current registry at config-edit time. This is deliberate:

- optional-mod IDs can survive when that mod is temporarily absent;
- shared modpack configs do not become impossible to save because one optional registry entry is missing;
- Data Attributes implicit entity groups are pseudo-identifiers rather than vanilla entity registry entries.

Malformed resource locations are still rejected. At runtime, unresolved concrete IDs are skipped safely with diagnostics.

## Server Configuration > Attribute Definition Overrides

The map key is the attribute ID, for example `minecraft:generic.max_health`.

Each value exposes:

- **Enabled** — keeps the entry present while disabling its runtime override.
- **Override Minimum** — when off, inherit the attribute's native minimum.
- **Allowed Minimum** — definition floor used only when Override Minimum is enabled; it is not an entity value.
- **Override Maximum** — when off, inherit the attribute's native maximum.
- **Allowed Maximum** — definition ceiling used only when Override Maximum is enabled; it is not an entity value.
- **Stacking Formula** — `Flat` or `Diminished`.
- **Diminishing Smoothness** — used by `Diminished`; constrained to greater than 0 and at most 1.
- **Display Format** — `Whole` or `Percentage` for Data Attributes' formatted-value API.

If the resolved minimum is greater than the resolved maximum, the runtime skips that override instead of applying an invalid clamp.

For example, `minecraft:generic.max_health` is a vanilla ranged attribute whose normal base/default is 20 and whose vanilla allowed maximum is 1024. Setting an Attribute Definition Override maximum to 1024 does **not** set player health to 1024; it only controls the definition ceiling. Raising that ceiling to 2048 would merely make an effective value above 1024 possible if an entity base, modifier, or function actually produces it.

Existing v2 configs are migrated automatically. Old finite min/max values become enabled bound overrides; old `NaN` values become inherited bounds.

## Server Configuration > Attribute Functions

Functions are intentionally nested so the relationship reads as:

`parent/source attribute -> child/derived attribute -> function`

For example, an outer `minecraft:generic.max_health` key with a nested `minecraft:generic.movement_speed` key means max health is a parent/source for movement speed.

A function exposes:

- **Enabled** — keeps the relationship configured without applying it.
- **Operation: Add** — contributes `parentValue * coefficient` to the child.
- **Operation: Multiply** — multiplies the child's result by `1 + (parentValue * coefficient)`.
- **Coefficient** — the relationship multiplier.

Self-links and non-finite coefficients are ignored safely. Missing source/child attributes are retained in server config but skipped until registered.

## Server Configuration > Entity Base Attributes

The outer map key is either a concrete entity type, such as `minecraft:zombie`, or one of Data Attributes' implicit groups:

- `data_attributes:living_entity`
- `data_attributes:mob_entity`
- `data_attributes:path_aware_entity`
- `data_attributes:hostile_entity`
- `data_attributes:passive_entity`
- `data_attributes:animal_entity`

Inside each entity/group entry, map attribute IDs to their **Base Value**.

Implicit groups apply by entity class hierarchy; an explicit concrete entity entry is applied afterward and therefore can override inherited group values. Non-finite base values and unresolved attributes are ignored safely.


## Datapack Baseline Preview

The preview contains the merged datapack layer for:

- attribute overrides;
- attribute functions;
- entity/group attributes.

If multiple datapacks define the same key, normal pack priority is already resolved before it appears here. The server config then overlays matching keys on top. This makes the preview useful for answering “what am I overriding?” without copying datapack values into permanent config state.

Because the preview is sent from the server with Data Attributes' runtime snapshot, multiplayer clients see the server's actual datapack baseline rather than a guess based on their local files.

## Diagnosing an unexpected base value

Use vanilla `/attribute` to distinguish the raw base from the final calculated value:

```mcfunction
/attribute @s minecraft:generic.max_health base get
/attribute @s minecraft:generic.max_health get
```

If an older Data Attributes build already saved an incorrect player base (for example 1024), changing a definition ceiling cannot safely prove that the saved base was accidental. Reset it once to the intended supplier/default base, for example:

```mcfunction
/attribute @s minecraft:generic.max_health base set 20
```

Or set it to the Entity Base Attribute you intentionally configured. In 3.0.0, setting the live base exactly to the current supplier/config base also reattaches that instance to supplier ownership, so future Fzzy edits and datapack `/reload`s can move it with the entity configuration again.
