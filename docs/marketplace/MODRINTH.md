![Data Attributes Banner](https://raw.githubusercontent.com/BareMinimumStudios/data-attributes/HEAD/docs/assets/data-attributes-banner.png)

# Data Attributes

**A data-driven entity attribute framework for Minecraft 1.21.1 on Fabric and NeoForge.**

Data Attributes makes Minecraft's attribute system much easier to shape without hard-coding every change into another mod. Server owners and pack makers can change attribute bounds and stacking behavior, derive one attribute from another, and assign base values to individual entity types or broader entity groups.

It does not add a progression system or a collection of new content by itself. Think of it as the attribute layer that datapacks, servers, and other mods can build on.

This project continues the original [Data Attributes](https://modrinth.com/mod/data-attributes) by CleverNucleus and is maintained by Bare Minimum Studios. It is listed as **Data Attributes** on Modrinth and **Data Attributes: DC** on CurseForge.

## Three separate layers

### Attribute Definition Overrides

Definition overrides change the rules of an attribute: its allowed minimum/maximum, stacking formula, diminishing smoothness, and display format.

**They do not set entity values.** If you raise `minecraft:generic.max_health`'s maximum to `2048`, you have raised the ceiling to 2048; you have not given every entity 2048 health.

### Attribute Functions

Functions connect a parent/source attribute to a child/derived attribute. The parent's current value can add to or multiply the child using a configurable coefficient, which makes follow-on stats possible without baking them into every mod.

### Entity Base Attributes

Entity entries are what actually assign raw base values. You can target a concrete entity such as `minecraft:player`, or use Data Attributes' broader entity groups and then layer specific entries on top.

For example, `minecraft:player -> minecraft:generic.max_health = 2` gives players a max-health base of 2.

## Datapacks + server config

Data Attributes resolves its settings in this order:

```text
Vanilla defaults → datapacks → server configuration
```

The server configuration uses **Fzzy Config** and includes a Datapack Baseline Preview, so administrators can see what datapacks are supplying before they override it.

Datapacks use these paths:

```text
data/<namespace>/data_attributes/overrides/*.json
data/<namespace>/data_attributes/functions/*.json
data/<namespace>/data_attributes/entity_types/*.json
```

## Live editing

Changes accepted through the in-game Fzzy Config screen apply live. Existing loaded living entities adopt supplier-owned base changes on their next tick.

Datapack JSON changes use vanilla `/reload`. Manually changing Fzzy's JSON5 file on disk is separate; `/reload` only reloads datapack/resources and does not reread arbitrary Fzzy config files.

## Requirements

**Fabric 1.21.1**

- Fabric API
- Fabric Language Kotlin
- Fzzy Config

**NeoForge 1.21.1**

- Kotlin for Forge (NeoForge)
- Fzzy Config

OWO Lib/OOLib, OWO Sentinel, and Endec are **not** required by the current source.

> **Compatibility note:** Data Attributes is not intended to be used with AttributeFix. Both modify the same attribute bounds/capabilities layer and can conflict.

## Links

- [Source](https://github.com/BareMinimumStudios/data-attributes)
- [Issues](https://github.com/BareMinimumStudios/data-attributes/issues)
- [Configuration reference](https://github.com/BareMinimumStudios/data-attributes/blob/HEAD/CONFIGURATION.md)
- [Documentation](https://bareminimumstudios.github.io/Bare-Minimum-Docs/data-attributes/home/)
- [Modrinth](https://modrinth.com/mod/dataattributes)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dataattributes)
- [Original Data Attributes by CleverNucleus](https://modrinth.com/mod/data-attributes)

## Sponsor

[![Sponsor Banner](https://www.bisecthosting.com/partners/custom-banners/db76a74a-a111-4660-98b7-5a75c15a5951.png)](https://bisecthosting.com/bareminimum)

Use code **`bareminimum`** to get **25% off your first month**!

## License

Current Bare Minimum Studios releases use the **Bare Minimum License (BML) v1.0**. See the [license](https://github.com/BareMinimumStudios/data-attributes/blob/HEAD/LICENSE) for the full terms.
