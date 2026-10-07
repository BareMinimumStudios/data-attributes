<p align="center">
  <img src="docs/assets/data-attributes-banner.png" alt="Data Attributes banner" />
</p>

<p align="center">
  <a href="https://github.com/BareMinimumStudios/data-attributes/blob/HEAD/LICENSE"><img alt="BML 1.0 License" src="https://img.shields.io/badge/LICENSE-BML%201.0-FFFFFF?style=for-the-badge&labelColor=1A1A1A" /></a>
  <a href="https://github.com/BareMinimumStudios/data-attributes/stargazers"><img alt="GitHub stars" src="https://img.shields.io/github/stars/BareMinimumStudios/data-attributes?style=for-the-badge&logo=github&labelColor=1A1A1A&color=FFFFFF" /></a>
  <a href="https://github.com/BareMinimumStudios/data-attributes/forks"><img alt="GitHub forks" src="https://img.shields.io/github/forks/BareMinimumStudios/data-attributes?style=for-the-badge&logo=github&labelColor=1A1A1A&color=FFFFFF" /></a>
  <a href="https://github.com/BareMinimumStudios/data-attributes/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/BareMinimumStudios/data-attributes?style=for-the-badge&logo=github&label=ISSUES&labelColor=1A1A1A&color=FFFFFF" /></a>
</p>

<p align="center">
  <a href="https://bareminimumstudios.github.io/Bare-Minimum-Docs/data-attributes/home/"><img alt="Documentation" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/generic_vector.svg" /></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/dataattributes"><img alt="Available on CurseForge" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg" /></a>
  <a href="https://modrinth.com/mod/dataattributes"><img alt="Available on Modrinth" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg" /></a>
</p>

<p align="center">
  <strong>Minecraft 1.21.1 · Fabric + NeoForge · Java 21</strong>
</p>

# Data Attributes

Data Attributes turns Minecraft's entity attribute system into something pack makers and server owners can actually shape. It can raise or lower an attribute's legal bounds, derive one attribute from another, and assign base values to specific entity types or broader entity groups. Those rules can come from datapacks or from a server-authoritative Fzzy Config setup.

The mod does not add a new progression system or a pile of content on its own. It is the attribute layer underneath those systems: useful by itself for server customization, and useful to other mods that need more flexible attributes.

This project continues the original [Data Attributes](https://modrinth.com/mod/data-attributes) by CleverNucleus. Maintained by Bare Minimum Studios, this continuation is listed as **Data Attributes** on Modrinth and **Data Attributes: DC** on CurseForge.

## What you can configure

### Attribute Definition Overrides

Definition overrides change what an attribute is *allowed* to do. They can replace its minimum or maximum, select flat or diminished stacking, tune diminishing smoothness, and change Data Attributes' display formatting.

They do **not** set an entity's base value. Raising `minecraft:generic.max_health`'s maximum to `2048` only raises the ceiling; it does not give every entity 2048 health.

### Attribute Functions

Functions let one attribute contribute to another. A parent/source attribute can add to or multiply a child/derived attribute using a configurable coefficient, which makes follow-on stats possible without hard-coding them into every downstream mod.

### Entity Base Attributes

Entity entries are the layer that actually changes an entity's base attribute value. You can target concrete entity IDs or Data Attributes' implicit groups and then layer more specific entries on top.

For example, setting `minecraft:player -> minecraft:generic.max_health = 2` gives players a raw max-health base of 2. Definition bounds still control the legal calculated result, but they remain separate from that base value.

## Configuration and datapacks

The effective configuration is built in a predictable order:

```text
Vanilla defaults → datapacks → server configuration
```

The Fzzy Config screen is server-authoritative and includes an informational **Datapack Baseline Preview**, so you can see what loaded datapacks contribute before overriding them. The full field-by-field reference lives in [CONFIGURATION.md](CONFIGURATION.md).

Datapack entries are read from:

```text
data/<namespace>/data_attributes/overrides/*.json
data/<namespace>/data_attributes/functions/*.json
data/<namespace>/data_attributes/entity_types/*.json
```

Normal datapack priority applies when multiple packs provide the same entry.

## Live changes

You generally do not need to restart the server while tuning attributes. Saving an accepted change through the Fzzy Config GUI rebuilds the effective server configuration immediately, and loaded living entities pick up supplier-owned base changes on their next tick.

Datapack JSON changes use vanilla `/reload`. Editing Fzzy's JSON5 file directly on disk is different: `/reload` is a datapack/resource reload and does not act as a general Fzzy config-file reload.

Runtime base values that were deliberately changed by another system, such as `/attribute ... base set`, are preserved instead of being mistaken for a configured supplier value.

## Requirements

| Loader | Required mods |
| --- | --- |
| **Fabric 1.21.1** | Fabric API, Fabric Language Kotlin, Fzzy Config |
| **NeoForge 1.21.1** | Kotlin for Forge (NeoForge), Fzzy Config |

Data Attributes no longer depends on OWO Lib/OOLib, OWO Sentinel, or Endec.

> [!IMPORTANT]
> Data Attributes is not intended to be used alongside **AttributeFix**. Both modify the attribute bounds/capabilities layer and can conflict with each other.

## For developers

Data Attributes is available through Modrinth's Maven endpoint. Using the project ID keeps the coordinate stable even if the public project slug changes:

```kotlin
repositories {
    maven("https://api.modrinth.com/maven")
}

dependencies {
    modImplementation("maven.modrinth:KCGxOJsE:<version>")
}
```

CurseMaven users can reference CurseForge project `955929`:

```kotlin
repositories {
    maven("https://cursemaven.com")
}

dependencies {
    modImplementation("curse.maven:dataattributes-955929:<file-id>")
}
```

The 1.21.1 project is built with **Java 21**, **Kotlin**, and **Cloche**. Shared runtime code deliberately stays loader-neutral; loader-specific Fzzy Config integrations are compiled separately so Fabric mappings cannot leak into the NeoForge/Mojmap side.

Build both targets with:

```bash
./gradlew build
```

## Links

- [Source](https://github.com/BareMinimumStudios/data-attributes)
- [Issues](https://github.com/BareMinimumStudios/data-attributes/issues)
- [Configuration reference](CONFIGURATION.md)
- [Documentation](https://bareminimumstudios.github.io/Bare-Minimum-Docs/data-attributes/home/)
- [Modrinth](https://modrinth.com/mod/dataattributes)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dataattributes)
- [Original Data Attributes](https://modrinth.com/mod/data-attributes)

## Sponsor

[![Sponsor Banner](https://www.bisecthosting.com/partners/custom-banners/db76a74a-a111-4660-98b7-5a75c15a5951.png)](https://bisecthosting.com/bareminimum)

Use code **`bareminimum`** to get **25% off your first month**!

## License

The current Bare Minimum Studios source is licensed under the **Bare Minimum License (BML) v1.0**. See [LICENSE](LICENSE) for the full terms.
