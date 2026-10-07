@file:UseSerializers(ResourceLocationSerializer::class)

package net.bms.data_attributes.config

import net.bms.data_attributes.config.entities.EntityTypeEntry
import net.bms.data_attributes.config.functions.AttributeFunction
import net.bms.data_attributes.config.models.AttributeOverride
import net.bms.data_attributes.serde.ResourceLocationSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.resources.ResourceLocation

@Serializable
data class Cache(val overrides: Overrides = Overrides(), val functions: Functions = Functions(), val types: EntityTypes = EntityTypes()) {
    @Serializable
    data class Overrides(var entries: LinkedHashMap<ResourceLocation, AttributeOverride> = LinkedHashMap())
    @Serializable
    data class Functions(var entries: LinkedHashMap<ResourceLocation, LinkedHashMap<ResourceLocation, AttributeFunction>> = LinkedHashMap())
    @Serializable
    data class EntityTypes(var entries: LinkedHashMap<ResourceLocation, LinkedHashMap<ResourceLocation, EntityTypeEntry>> = LinkedHashMap())
}