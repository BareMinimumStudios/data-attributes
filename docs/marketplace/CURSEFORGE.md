![Data Attributes Banner](https://cdn.modrinth.com/data/cached_images/464354cc9d34d3778ad4a9db3816adc86c0f6b84.png)

# Data Attributes

**A data-driven entity attribute framework for Minecraft 1.21.1 on Fabric and NeoForge.**

Data Attributes gives servers, modpacks, and other mods a cleaner way to work with Minecraft's entity attributes. You can expand or restrict an attribute's legal range, build follow-on attributes from other stats, and give selected entity types their own base values through datapacks or a server-authoritative Fzzy Config setup.

It does not add a progression system or a pile of content on its own. It is infrastructure: a flexible attribute layer that other systems can build on.

This is a continuation of the original Data Attributes by CleverNucleus and was previously distributed as **Data Attributes: Directors Cut**.

## What can it change?

### Attribute Definition Overrides

These change an attribute's rules: allowed minimum/maximum, stacking formula, diminishing smoothness, and display formatting.

An override is **not** an entity value. Setting `minecraft:generic.max_health`'s allowed maximum to `2048` only raises the possible ceiling; it does not set every entity to 2048 health.

### Attribute Functions

A parent/source attribute can feed a child/derived attribute with additive or multiplicative behavior. This is useful for RPG-style follow-on stats without forcing every consuming mod to implement the relationship itself.

### Entity Base Attributes

This layer actually assigns raw base values to entities or Data Attributes entity groups. For example, setting `minecraft:player -> minecraft:generic.max_health = 2` gives players a max-health base of 2.

## Configuration priority

```text
Vanilla defaults → datapacks → server configuration
```

The Fzzy Config screen also exposes a Datapack Baseline Preview so server administrators can see the lower-priority datapack values before overriding them.

Datapacks are loaded from:

```text
data/<namespace>/data_attributes/overrides/*.json
data/<namespace>/data_attributes/functions/*.json
data/<namespace>/data_attributes/entity_types/*.json
```

## Hot reload behavior

Saving an accepted change through the Fzzy Config GUI applies it live. Existing loaded living entities adopt supplier-owned base changes on their next tick.

Changes to datapack JSON use vanilla `/reload`. Directly editing Fzzy's JSON5 file on disk is different: `/reload` reloads datapack/resources, not arbitrary Fzzy config files.

## Required mods

**Fabric 1.21.1**
- Fabric API
- Fabric Language Kotlin
- Fzzy Config

**NeoForge 1.21.1**
- Kotlin for Forge (NeoForge)
- Fzzy Config

The current version does **not** require OWO Lib/OOLib, OWO Sentinel, or Endec.

**AttributeFix is not recommended alongside Data Attributes.** Both change the same attribute bounds/capabilities layer and can conflict.

## Links

- Source: https://github.com/BareMinimumStudios/data-attributes
- Issues: https://github.com/BareMinimumStudios/data-attributes/issues
- Configuration reference: https://github.com/BareMinimumStudios/data-attributes/blob/HEAD/CONFIGURATION.md
- Documentation: https://bareminimumstudios.github.io/Bare-Minimum-Docs/data-attributes/home/
- Modrinth: https://modrinth.com/mod/data-attributes-directors-cut
- Original Data Attributes: https://modrinth.com/mod/data-attributes

## License

Current Bare Minimum Studios releases use the **Bare Minimum License (BML) v1.0**:
https://github.com/BareMinimumStudios/data-attributes/blob/HEAD/LICENSE
