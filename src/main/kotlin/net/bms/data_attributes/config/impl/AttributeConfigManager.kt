@file:UseSerializers(ResourceLocationSerializer::class)

package net.bms.data_attributes.config.impl

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.bms.data_attributes.DataAttributes
import net.bms.data_attributes.api.EntityInstances
import net.bms.data_attributes.api.attribute.IAttribute
import net.bms.data_attributes.api.event.AttributeEvents
import net.bms.data_attributes.config.Cache
import net.bms.data_attributes.config.ConfigState
import net.bms.data_attributes.config.entities.EntityTypeData
import net.bms.data_attributes.config.entry.ConfigMerger
import net.bms.data_attributes.config.functions.AttributeFunction
import net.bms.data_attributes.config.models.AttributeOverride
import net.bms.data_attributes.data.AttributeData
import net.bms.data_attributes.mutable.MutableAttribute
import net.bms.data_attributes.serde.ResourceLocationSerializer
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Monster

/**
 * Effective runtime attribute configuration.
 *
 * Datapack values are stored in [defaults]. User-managed Fzzy Config values are
 * merged on top by [update], after which this manager rebuilds attribute state
 * and entity attribute containers.
 */
class AttributeConfigManager(
    var data: Data = Data(),
    private val handler: AttributeMapHandler = AttributeMapHandler()
) {
    var updateFlag: Int = 0
        private set

    var defaults: Cache = Cache()

    /** Raw datapack baseline before server-managed Fzzy Config values are merged on top. */
    var datapackDefaults: Data = Data()
        private set

    @JvmRecord
    data class Tuple<T>(val livingEntity: Class<out LivingEntity>, val value: T)

    @Serializable
    @JvmRecord
    data class Packet(
        val data: Data,
        val datapackDefaults: Data = Data(),
        val updateFlag: Int
    )

    @Serializable
    data class Data(
        var overrides: Map<ResourceLocation, AttributeOverride> = emptyMap(),
        var functions: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>> = emptyMap(),
        var entityTypes: Map<ResourceLocation, EntityTypeData> = emptyMap()
    )

    companion object {
        fun getAttribute(identifier: ResourceLocation): Attribute? = BuiltInRegistries.ATTRIBUTE[identifier]

        val ENTITY_TYPE_INSTANCES = mapOf(
            EntityInstances.LIVING to Tuple(LivingEntity::class.java, 0),
            EntityInstances.MOB to Tuple(Mob::class.java, 1),
            EntityInstances.PATHFINDER to Tuple(PathfinderMob::class.java, 2),
            EntityInstances.HOSTILE to Tuple(Monster::class.java, 3),
            EntityInstances.PASSIVE to Tuple(AgeableMob::class.java, 4),
            EntityInstances.ANIMAL to Tuple(Animal::class.java, 5)
        )
    }

    val overrides: Map<ResourceLocation, AttributeOverride>
        get() = data.overrides

    val functions: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>>
        get() = data.functions

    val entityTypes: Map<ResourceLocation, EntityTypeData>
        get() = data.entityTypes

    /** Advances the revision observed by living entities and returns the new value. */
    fun nextUpdateFlag(): Int {
        updateFlag = if (updateFlag == Int.MAX_VALUE) 0 else updateFlag + 1
        return updateFlag
    }

    /** Merges datapack defaults with server-config values and applies the result. */
    fun update() {
        val config = ConfigState.current()
        datapackDefaults = Data(
            overrides = LinkedHashMap(defaults.overrides.entries),
            functions = defaults.functions.entries.mapValuesTo(LinkedHashMap()) { (_, functions) -> LinkedHashMap(functions) },
            entityTypes = defaults.types.entries.mapValuesTo(LinkedHashMap()) { (_, attributes) -> EntityTypeData(LinkedHashMap(attributes)) }
        )
        data = Data(
            overrides = ConfigMerger.mergeOverrides(defaults.overrides.entries, config),
            functions = ConfigMerger.mergeFunctions(defaults.functions.entries, config),
            entityTypes = ConfigMerger.mergeEntityTypes(defaults.types.entries, config)
        )
        onDataUpdate()
    }

    fun toPacket() = Packet(data, datapackDefaults, updateFlag)

    /** Replaces this manager with a synchronized server snapshot. */
    fun readPacket(packet: Packet) {
        data = packet.data
        datapackDefaults = packet.datapackDefaults
        updateFlag = packet.updateFlag
    }

    fun getContainer(type: EntityType<out LivingEntity>, entity: LivingEntity): AttributeMap =
        handler.getContainer(type, entity)

    /** Rebuilds all derived attribute state from [data]. */
    fun onDataUpdate() {
        val attributeData = LinkedHashMap<ResourceLocation, AttributeData>()

        insertOverrides(overrides, attributeData)
        insertFunctions(functions, attributeData)

        for (attribute in BuiltInRegistries.ATTRIBUTE) {
            (attribute as? MutableAttribute)?.`data_attributes$clear`()
        }

        for ((id, runtimeData) in attributeData) {
            getAttribute(id)?.let(runtimeData::override)
        }

        for ((id, runtimeData) in attributeData) {
            getAttribute(id)?.let(runtimeData::copy)
        }

        handler.buildContainers(entityTypes)
        AttributeEvents.RELOADED.invoker().onReloadCompleted()

        DataAttributes.LOGGER.info(
            "Updated Data Attributes manager with {} configured attributes and {} entity types (revision {}).",
            attributeData.size,
            entityTypes.size,
            updateFlag
        )
    }

    private fun insertOverrides(
        overrides: Map<ResourceLocation, AttributeOverride>,
        attributeData: MutableMap<ResourceLocation, AttributeData>
    ) {
        for ((id, configured) in overrides) {
            // Disabled entries are intentional server-level tombstones. They
            // may be overriding an enabled datapack entry, but must not leak
            // their formula/smoothness/format into runtime attribute state.
            if (!configured.enabled) continue

            val attribute = getAttribute(id)
            if (attribute == null) {
                DataAttributes.LOGGER.warn("Skipping override for unregistered attribute [{}].", id)
                continue
            }

            val mutable = attribute as IAttribute
            val smoothness = configured.smoothness.takeIf { it.isFinite() && it > 0.0 && it <= 1.0 } ?: run {
                DataAttributes.LOGGER.warn(
                    "Override for [{}] has invalid smoothness {}. Falling back to 1.0.",
                    id,
                    configured.smoothness
                )
                1.0
            }
            val resolved = configured.copy(
                min = if (configured.min.isNaN()) mutable.`data_attributes$min_fallback`() else configured.min,
                max = if (configured.max.isNaN()) mutable.`data_attributes$max_fallback`() else configured.max,
                smoothness = smoothness
            )

            if (resolved.min > resolved.max) {
                DataAttributes.LOGGER.warn(
                    "Skipping override for [{}]: minimum {} is greater than maximum {}.",
                    id,
                    resolved.min,
                    resolved.max
                )
                continue
            }

            attributeData[id] = AttributeData(resolved)
        }
    }

    private fun insertFunctions(
        store: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>>,
        attributeData: MutableMap<ResourceLocation, AttributeData>
    ) {
        for ((id, configuredFunctions) in store) {
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(id)) {
                DataAttributes.LOGGER.warn("Skipping functions for unregistered parent attribute [{}].", id)
                continue
            }

            val runtimeData = attributeData[id] ?: AttributeData()
            val validFunctions = LinkedHashMap<ResourceLocation, AttributeFunction>()
            for ((childId, function) in configuredFunctions) {
                if (!function.enabled) continue
                if (childId == id) {
                    DataAttributes.LOGGER.warn("Skipping self-referencing attribute function [{}] -> [{}].", id, childId)
                    continue
                }
                if (!function.value.isFinite()) {
                    DataAttributes.LOGGER.warn(
                        "Skipping attribute function [{}] -> [{}]: coefficient {} is not finite.",
                        id,
                        childId,
                        function.value
                    )
                    continue
                }
                validFunctions[childId] = function
            }
            runtimeData.putFunctions(validFunctions)
            attributeData[id] = runtimeData
        }
    }
}
