@file:UseSerializers(ResourceLocationSerializer::class)

package net.bms.data_attributes.config.entities

import net.bms.data_attributes.endec.Endecs
import net.bms.data_attributes.ext.keyOf
import net.bms.data_attributes.mutable.MutableAttributeSupplier
import net.bms.data_attributes.serde.ResourceLocationSerializer
import io.wispforest.endec.Endec
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.fzzyhmstrs.fzzy_config.util.Walkable
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import kotlin.collections.iterator
import kotlin.jvm.optionals.getOrNull

/**
 * Container for data that modifies the specific [BuiltInRegistries.ENTITY_TYPE] entry with an associated [EntityTypeEntry].
 */
@Serializable
data class EntityTypeData(val data: Map<ResourceLocation, EntityTypeEntry> = mapOf()) : Walkable {
    companion object {
        @JvmField
        val ENDEC: Endec<EntityTypeData> = Endecs.RESOURCE.keyOf(EntityTypeEntry.ENDEC).xmap(::EntityTypeData) { it.data }
    }

    /**
     * Builds the entity-type data with a provided [AttributeSupplier.Builder],
     * along with an optional [DefaultAttributeContainer].
     */
    fun build(builder: AttributeSupplier.Builder, supplier: AttributeSupplier?) {
        (supplier as MutableAttributeSupplier).`data_attributes$copy`(builder)
        for ((key, entry) in this.data) {
            val attribute = BuiltInRegistries.ATTRIBUTE.getHolder(key).getOrNull() ?: continue
            builder.add(attribute, attribute.value().sanitizeValue(entry.value))
        }
    }
}