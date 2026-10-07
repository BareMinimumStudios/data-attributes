package net.bms.data_attributes.config.impl

import net.bms.data_attributes.config.entities.EntityTypeData
import net.bms.data_attributes.config.impl.AttributeConfigManager.Companion.ENTITY_TYPE_INSTANCES
import net.bms.data_attributes.config.impl.AttributeConfigManager.Tuple
import net.bms.data_attributes.mutable.MutableAttributeMap
import net.bms.data_attributes.mutable.MutableAttributeSupplier
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import org.jetbrains.annotations.ApiStatus

typealias ImplicitEntityData = Map<Int, Tuple<EntityTypeData>>
typealias ExplicitEntityData = Map<EntityType<out LivingEntity>, EntityTypeData>

/**
 * Builds live [AttributeMap]s from vanilla suppliers plus Data Attributes entity-base overlays.
 *
 * Attribute definition overrides (minimum/maximum bounds, stacking, formatting, functions) are
 * deliberately absent from this class. They modify the registered [net.minecraft.world.entity.ai.attributes.Attribute]
 * definition itself and must never be interpreted as an entity's base value.
 *
 * Base-value precedence is:
 * `vanilla supplier -> matching implicit groups -> explicit entity entry`.
 */
@ApiStatus.Internal
class AttributeMapHandler(
    private var implicitData: ImplicitEntityData = emptyMap(),
    private var explicitData: ExplicitEntityData = emptyMap()
) {
    /**
     * Creates the supplier used by one live entity.
     *
     * Vanilla values are copied once, then only [EntityTypeData] is allowed to write base values.
     * Keeping raw entity data here instead of prebuilding overlay suppliers makes the separation
     * between definition bounds and entity base values explicit and avoids an unnecessary supplier
     * allocation for every configured group/entity during each manager rebuild.
     */
    fun getContainer(entityType: EntityType<out LivingEntity>, entity: LivingEntity): AttributeMap {
        val builder = AttributeSupplier.Builder()
        (DefaultAttributes.getSupplier(entityType) as? MutableAttributeSupplier)?.`data_attributes$copy`(builder)

        // buildContainers stores this map in hierarchy order; do not sort again for every entity.
        implicitData.values.forEach { (type, data) ->
            if (type.isInstance(entity)) data.applyTo(builder)
        }

        explicitData[entityType]?.applyTo(builder)

        val container = AttributeMap(builder.build()) as MutableAttributeMap
        container.`data_attributes$setLivingEntity`(entity)
        return container as AttributeMap
    }

    /**
     * Reindexes entity configuration. No attribute supplier/base value is created here; this method
     * only separates implicit class groups from concrete entity types. Actual base-value writes happen
     * exclusively in [EntityTypeData.applyTo] while [getContainer] is constructing a live supplier.
     */
    @Suppress("UNCHECKED_CAST")
    fun buildContainers(
        entries: Map<ResourceLocation, EntityTypeData>,
        instances: Map<ResourceLocation, Tuple<Int>> = ENTITY_TYPE_INSTANCES
    ) {
        val validEntityTypes = BuiltInRegistries.ENTITY_TYPE.keySet()
            .filter { DefaultAttributes.hasSupplier(BuiltInRegistries.ENTITY_TYPE[it]) }
            .toSet()

        val implicits = LinkedHashMap<Int, Tuple<EntityTypeData>>()
        val explicits = LinkedHashMap<EntityType<out LivingEntity>, EntityTypeData>()

        entries.keys.mapNotNull { identifier ->
            val entry = instances[identifier] ?: return@mapNotNull null
            Triple(entry.value, entry.livingEntity, identifier)
        }.sortedBy { it.first }.forEach { (hierarchy, entityClass, identifier) ->
            val data = entries[identifier] ?: return@forEach
            implicits[hierarchy] = Tuple(entityClass, data)
        }

        entries.forEach { (identifier, data) ->
            if (identifier in instances || identifier !in validEntityTypes) return@forEach
            val entityType = BuiltInRegistries.ENTITY_TYPE[identifier] as EntityType<out LivingEntity>
            explicits[entityType] = data
        }

        implicitData = implicits
        explicitData = explicits
    }
}
