package net.bms.data_attributes.config.entry

import net.bms.data_attributes.config.Configs
import net.bms.data_attributes.config.functions.AttributeFunction
import net.bms.data_attributes.config.models.AttributeOverride
import net.bms.data_attributes.config.entities.EntityTypeData
import net.bms.data_attributes.config.entities.EntityTypeEntry
import net.minecraft.resources.ResourceLocation

object ConfigMerger {
    fun mergeOverrides(values: Map<ResourceLocation, AttributeOverride>): Map<ResourceLocation, AttributeOverride> {
        val entries = values.toMutableMap()
        for ((id, override) in Configs.CONFIG.serverConfig.overrides) {
            entries[id] = override
        }
        return entries
    }

    fun mergeFunctions(values: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>>): Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>> {
        val entries = values.toMutableMap()
        for ((primaryId, primaryEntry) in Configs.CONFIG.serverConfig.functions) {
            val secondaryEntry = entries[primaryId]?.toMutableMap()
            if (secondaryEntry == null) {
                entries[primaryId] = primaryEntry
            }
            else {
                for ((id, value) in primaryEntry) {
                    secondaryEntry[id] = value
                }
                entries[primaryId] = secondaryEntry
            }
        }
        return entries
    }

    fun mergeEntityTypes(values: Map<ResourceLocation, Map<ResourceLocation, EntityTypeEntry>>): Map<ResourceLocation, EntityTypeData> {
        val entries = values.toMutableMap()
        for ((primaryId, primaryEntry) in Configs.CONFIG.serverConfig.entities) {
           val secondaryEntry = entries[primaryId]?.toMutableMap()
           if (secondaryEntry == null) {
               entries[primaryId] = primaryEntry.data
           }
           else {
               for ((id, value) in primaryEntry.data) {
                   secondaryEntry[id] = value
               }
               entries[primaryId] = secondaryEntry
           }
        }
        return entries.entries.associate { (k, v) -> k to EntityTypeData(v) }
    }
}