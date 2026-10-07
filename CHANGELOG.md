# Changelog

Notable changes to Data Attributes are documented here, newest release first.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [3.0.0] - 2026-10-07

Minecraft 1.21.1 rewrite for Fabric and NeoForge.

### Added

- NeoForge support alongside Fabric for Minecraft 1.21.1.
- Server-authoritative Fzzy Config JSON5 configuration for both loaders.
- Live application of accepted in-game configuration changes. Loaded living entities adopt supplier-owned base changes on their next tick.
- An informational Datapack Baseline Preview for attribute overrides, functions, and entity base values, synchronized from the server and never saved back to configuration or datapacks.
- Configuration suggestions for registered entity types and Data Attributes' implicit entity groups.
- Persistent supplier-base tracking so configured base values can update without overwriting deliberate runtime changes, including `/attribute ... base set`.
- Reattachment to supplier ownership when a live base is set back to its current configured value.
- Bounded GZIP synchronization of runtime configuration snapshots.
- Kotlin serialization compiler support for datapack and runtime snapshot serialization.

### Changed

- Attribute Definition Overrides now control bounds, stacking behavior, diminishing smoothness, and display formatting separately from entity base values.
- Entity Base Attributes are the configuration and datapack layer responsible for assigning entity supplier base values.
- Entity configuration resolves from vanilla defaults through matching implicit groups to explicit entity entries. Server configuration overrides the datapack baseline.
- Entity base values remain raw; attribute bounds constrain the calculated result rather than rewriting stored configuration values.
- Entity entries apply as deltas, avoiding unnecessary supplier rebuilds and preserving inherited values not explicitly overridden.
- Configuration fields are organized into Bounds, Stacking, and Display groups. Explicit Override Minimum/Maximum switches replace user-facing `NaN` inheritance values.
- Attribute Function descriptions clarify that the outer key is the parent/source attribute and the nested key is the child/derived attribute.
- Identifiers belonging to absent optional mods can remain configured.
- Datapack reloads rebuild configuration once at reload completion and respect normal resource-pack priority.
- Native Fabric/NeoForge custom-payload networking replaces the OWO network channel, and a loader-neutral event implementation replaces OWO event streams.
- Loader-specific Fzzy Config integrations use `0.7.7+1.21` on Fabric and `0.7.7+1.21+neoforge` on NeoForge, with shared runtime code consuming loader-neutral snapshots.
- Disabled server override entries suppress matching datapack overrides.
- Project metadata and documentation now reference the Bare Minimum Studios repository and the Bare Minimum License (BML) v1.0.

### Removed

- OWO Lib/OOLib, OWO Sentinel, and Endec dependencies and their associated repository declarations.
- The old `/reload` command-tail hook.
- Vanilla sword/tool hooks that incorrectly treated durability damage as attack damage.
- Duplicate mob equipment-comparison injections.
- Unused OWO interface code and legacy helpers.

### Fixed

- Configuration map additions failing with `Identifier invalid or not allowed`, using valid defaults, identifier validation, and live registry suggestions.
- NeoForge compilation failures caused by Fabric-mapped Fzzy Config classes entering shared source.
- Runtime modifiers being lost during live configuration rebuilds.
- Explicit entity entries resetting unrelated inherited values to vanilla defaults.
- Attribute dirty/update/synchronization behavior affecting health, absorption, and other stateful attributes after live rebuilds.
- Resource-stack rebuilding mutating maps during iteration.
- Self-referencing functions and non-finite function coefficients or entity base values are safely ignored.
- Implicit entity-group entries being discarded during registered-ID filtering.
- Standalone and supplier attribute modifier additions, modifier-removal bookkeeping, and dependent-function invalidation after base or transient changes.
- Incorrect Holder-versus-Attribute casts in diminishing stacking and formatted-value lookup.
- Implicit entity-category definitions crashing when no vanilla supplier exists.
- Stale attribute override state remaining after an override is removed.

[Unreleased]: https://github.com/BareMinimumStudios/data-attributes/compare/3.0.0...HEAD
[3.0.0]: https://github.com/BareMinimumStudios/data-attributes/releases/tag/3.0.0
