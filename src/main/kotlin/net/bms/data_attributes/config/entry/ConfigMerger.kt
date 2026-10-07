package net.bms.data_attributes.config.entry

import net.bms.data_attributes.config.ConfigSnapshot
import net.bms.data_attributes.config.entities.EntityTypeData
import net.bms.data_attributes.config.entities.EntityTypeEntry
import net.bms.data_attributes.config.functions.AttributeFunction
import net.bms.data_attributes.config.models.AttributeOverride
import net.minecraft.resources.ResourceLocation

/** Applies user-managed config values on top of datapack defaults. */
object ConfigMerger {
    fun mergeOverrides(
        values: Map<ResourceLocation, AttributeOverride>,
        config: ConfigSnapshot
    ): Map<ResourceLocation, AttributeOverride> {
        return LinkedHashMap(values).apply { putAll(config.overrides) }
    }

    fun mergeFunctions(
        values: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>>,
        config: ConfigSnapshot
    ): Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>> {
        val entries = values.mapValuesTo(LinkedHashMap()) { (_, functions) -> LinkedHashMap(functions) }

        for ((attributeId, configuredFunctions) in config.functions) {
            val merged = LinkedHashMap(entries[attributeId].orEmpty())
            merged.putAll(configuredFunctions)
            entries[attributeId] = merged
        }

        return entries
    }

    fun mergeEntityTypes(
        values: Map<ResourceLocation, Map<ResourceLocation, EntityTypeEntry>>,
        config: ConfigSnapshot
    ): Map<ResourceLocation, EntityTypeData> {
        val entries = values.mapValuesTo(LinkedHashMap()) { (_, attributes) -> LinkedHashMap(attributes) }

        for ((entityTypeId, configuredData) in config.entityTypes) {
            val merged = LinkedHashMap(entries[entityTypeId].orEmpty())
            merged.putAll(configuredData.data)
            entries[entityTypeId] = merged
        }

        return entries.mapValuesTo(LinkedHashMap()) { (_, attributes) -> EntityTypeData(attributes) }
    }
}
