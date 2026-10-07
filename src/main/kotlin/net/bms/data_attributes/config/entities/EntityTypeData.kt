@file:UseSerializers(ResourceLocationSerializer::class)

package net.bms.data_attributes.config.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.bms.data_attributes.serde.ResourceLocationSerializer
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import kotlin.jvm.optionals.getOrNull

/**
 * Base-value overlay attached to one concrete entity type or implicit entity group.
 *
 * These values are intentionally independent from Attribute Overrides. A definition override changes
 * what values an Attribute permits/calculates; it does not become an entity base value. For the same
 * reason, configured bases are stored in the supplier without pre-sanitizing them through the current
 * attribute bounds. The normal AttributeInstance calculation sanitizes the *effective* value when it
 * is read, so changing a bound later does not permanently destroy the configured base.
 */
@Serializable
data class EntityTypeData(val data: Map<ResourceLocation, EntityTypeEntry> = emptyMap()) {
    fun applyTo(builder: AttributeSupplier.Builder) {
        for ((key, entry) in data) {
            if (!entry.value.isFinite()) continue
            val attribute = BuiltInRegistries.ATTRIBUTE.getHolder(key).getOrNull() ?: continue
            builder.add(attribute, entry.value)
        }
    }
}
