package net.bms.data_attributes.config

import net.bms.data_attributes.config.entities.EntityTypeData
import net.bms.data_attributes.config.functions.AttributeFunction
import net.bms.data_attributes.config.models.AttributeOverride
import net.minecraft.resources.ResourceLocation

/**
 * Loader-neutral snapshot of the user-managed Fzzy Config values.
 *
 * Fzzy Config itself is intentionally kept in the Fabric/NeoForge source sets so
 * mapped loader classes can never leak through the shared source set.
 */
data class ConfigSnapshot(
    val overrides: Map<ResourceLocation, AttributeOverride> = emptyMap(),
    val functions: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>> = emptyMap(),
    val entityTypes: Map<ResourceLocation, EntityTypeData> = emptyMap()
)

object ConfigState {
    @Volatile
    private var snapshot: ConfigSnapshot = ConfigSnapshot()

    fun current(): ConfigSnapshot = snapshot

    /** Publishes one defensively-copied snapshot for the next manager rebuild. */
    fun replace(value: ConfigSnapshot) {
        snapshot = ConfigSnapshot(
            overrides = value.overrides.mapValuesTo(LinkedHashMap()) { (_, override) -> override.copy() },
            functions = value.functions.mapValuesTo(LinkedHashMap()) { (_, functions) ->
                functions.mapValuesTo(LinkedHashMap()) { (_, function) -> function.copy() }
            },
            entityTypes = value.entityTypes.mapValuesTo(LinkedHashMap()) { (_, data) ->
                EntityTypeData(data.data.mapValuesTo(LinkedHashMap()) { (_, entry) -> entry.copy() })
            }
        )
    }
}
