## 3.0.0 - Minecraft 1.21.1 multiplatform rewrite

Data Attributes 3.0.0 is the first release of the rebuilt 1.21.1 codebase for both Fabric and NeoForge. It replaces the old OWO-based configuration/networking stack, separates attribute-definition rules from entity base values, and makes datapack/config changes apply cleanly to live entities.

### Attribute semantics
- **Attribute Definition Overrides** now strictly modify an Attribute's allowed minimum/maximum, stacking behavior, diminishing smoothness, and display format. Definition bounds never become an entity base value.
- **Entity Base Attributes** are the only configuration/datapack layer that writes entity supplier base values.
- Entity resolution is deterministic: vanilla/default supplier → matching implicit entity groups → explicit entity values.
- Entity base values are stored raw rather than being pre-clamped through the current definition bounds. Bounds constrain the effective calculated value instead of destructively rewriting entity configuration.
- The entity container builder applies entity-data deltas directly, reducing unnecessary supplier rebuilding.

### Live entity updates
- Fzzy Config changes accepted in-game rebuild the effective attribute model immediately.
- Datapack changes rebuild through vanilla `/reload`; existing loaded entities adopt supplier-owned changes on their next tick.
- Existing runtime modifiers are preserved during live rebuilds.
- Explicit entity entries are true delta overlays and no longer reset unrelated inherited group values back to vanilla.
- Restored Minecraft's normal attribute dirty/update/sync path so max-health, max-absorption, and other stateful attributes react normally after a live rebuild.
- Attribute instances persist `data_attributes:supplier_base` and `data_attributes:supplier_owned` provenance so configured bases can follow future reloads without overwriting deliberate runtime `/attribute ... base set` changes.
- Setting a live base back to the current configured supplier value reattaches it to supplier ownership, providing a safe repair path for worlds tested with earlier development builds.

### Fzzy Config
- Rebuilt the server configuration around Fzzy Config JSON5 for both Fabric and NeoForge.
- Fixed map additions failing with `Identifier invalid or not allowed`; new entries use valid defaults and syntax-safe identifier validation with live registry suggestions.
- Optional-mod identifiers can remain configured even when their owning mod is absent.
- Organized Attribute Definition Overrides into clear **Bounds**, **Stacking**, and **Display** groups.
- Native min/max inheritance uses explicit **Override Minimum/Maximum** switches instead of exposing `NaN` to users.
- Clarified Attribute Function direction: outer key is the parent/source attribute and nested key is the child/derived attribute.
- Added a transient **Datapack Baseline Preview** below the authoritative server configuration for overrides, functions, and entity base attributes.
- Preview values are informational only and are never written back to the config or datapacks.
- Added suggestions for Data Attributes implicit entity groups as well as concrete entity types.

### Datapacks and reloads
- Datapacks remain the lower-priority baseline; matching server-config entries override them.
- Resource stacks respect normal pack priority and are rebuilt without mutating maps during iteration.
- Resource reload applies once at actual reload completion rather than through the old `/reload` command tail hook.
- The raw datapack baseline is synchronized to clients with the effective runtime snapshot so multiplayer previews show the server's actual data.
- Self-referencing functions, non-finite function coefficients, and non-finite entity base values are ignored safely.
- Implicit entity-group entries are preserved during registered-ID filtering.

### Multiplatform / dependencies
- Targets **Minecraft 1.21.1** on **Fabric** and **NeoForge**.
- Fixed the NeoForge compile failure caused by Fabric-mapped Fzzy Config classes leaking through common source.
- Fzzy integration now stays in loader-specific source sets while the common runtime consumes loader-neutral snapshots.
- Fabric uses Fzzy Config `0.7.7+1.21`; NeoForge uses `0.7.7+1.21+neoforge`.
- Added the Kotlin serialization compiler plugin used by datapack/runtime snapshot serialization.
- Removed OWO Lib/OOLib, OWO Sentinel, Endec, and their associated repositories/dependency declarations.

### Networking and API
- Replaced the OWO network channel with native 1.21.1 Fabric/NeoForge custom-payload networking.
- Added bounded GZIP runtime snapshot synchronization.
- Replaced OWO event streams with a small loader-neutral event implementation.
- Fixed standalone/supplier `AttributeInstance` modifier additions, modifier-removal bookkeeping, and dependent-function invalidation after base/transient changes.
- Fixed Holder-vs-Attribute casts in diminished stacking and formatted-value lookup.
- Disabled server override entries now act as true tombstones over matching datapack overrides.

### Cleanup and correctness
- Fixed implicit entity-category definitions crashing when no vanilla supplier exists.
- Fixed stale attribute override state after an override is removed.
- Removed vanilla sword/tool hooks that treated durability damage as attack damage.
- Removed duplicate mob equipment-comparison injections.
- Removed dead OWO UI code and unused legacy helpers discovered during the 1.21.1 cleanup.
- Updated project metadata and public documentation for the Bare Minimum Studios repository and BML 1.0 license.
