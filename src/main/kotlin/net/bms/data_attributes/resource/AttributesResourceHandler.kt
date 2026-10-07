package net.bms.data_attributes.resource

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.bms.data_attributes.DataAttributes
import net.bms.data_attributes.config.Cache
import net.bms.data_attributes.config.impl.AttributeConfigManager
import net.bms.data_attributes.serde.DataAttributesSerialization
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager

@OptIn(ExperimentalSerializationApi::class)
object AttributesResourceHandler {
    private const val DIRECTORY = DataAttributes.MOD_ID

    private fun isPathJson(id: ResourceLocation) = id.path.endsWith(".json")

    /**
     * Builds a fresh immutable-at-application cache. Resource stacks are read in
     * pack order, so higher-priority packs overwrite matching lower-priority keys.
     */
    fun loadIntoCache(manager: ResourceManager): Cache {
        val cache = Cache()
        readOverrides(manager, cache)
        readFunctions(manager, cache)
        readEntityTypes(manager, cache)
        return filterRegisteredEntries(cache)
    }

    private fun filterRegisteredEntries(cache: Cache): Cache {
        val overrides = LinkedHashMap(
            cache.overrides.entries.filterKeys { BuiltInRegistries.ATTRIBUTE.containsKey(it) }
        )

        val functions = LinkedHashMap<ResourceLocation, LinkedHashMap<ResourceLocation, net.bms.data_attributes.config.functions.AttributeFunction>>()
        for ((parent, children) in cache.functions.entries) {
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(parent)) continue
            functions[parent] = LinkedHashMap(children.filterKeys { BuiltInRegistries.ATTRIBUTE.containsKey(it) })
        }

        val types = LinkedHashMap<ResourceLocation, LinkedHashMap<ResourceLocation, net.bms.data_attributes.config.entities.EntityTypeEntry>>()
        for ((entityType, attributes) in cache.types.entries) {
            val isExplicitType = BuiltInRegistries.ENTITY_TYPE.containsKey(entityType)
            val isImplicitGroup = AttributeConfigManager.ENTITY_TYPE_INSTANCES.containsKey(entityType)
            if (!isExplicitType && !isImplicitGroup) continue
            types[entityType] = LinkedHashMap(attributes.filterKeys { BuiltInRegistries.ATTRIBUTE.containsKey(it) })
        }

        return Cache(Cache.Overrides(overrides), Cache.Functions(functions), Cache.EntityTypes(types))
    }

    private fun readOverrides(manager: ResourceManager, cache: Cache) {
        forEachResource(manager, "$DIRECTORY/overrides") { id, resource ->
            decode<Cache.Overrides>(id, resource)?.entries?.let(cache.overrides.entries::putAll)
        }
    }

    private fun readFunctions(manager: ResourceManager, cache: Cache) {
        forEachResource(manager, "$DIRECTORY/functions") { id, resource ->
            val decoded = decode<Cache.Functions>(id, resource) ?: return@forEachResource
            for ((attributeId, functions) in decoded.entries) {
                cache.functions.entries.getOrPut(attributeId) { LinkedHashMap() }.putAll(functions)
            }
        }
    }

    private fun readEntityTypes(manager: ResourceManager, cache: Cache) {
        forEachResource(manager, "$DIRECTORY/entity_types") { id, resource ->
            val decoded = decode<Cache.EntityTypes>(id, resource) ?: return@forEachResource
            for ((entityTypeId, attributes) in decoded.entries) {
                cache.types.entries.getOrPut(entityTypeId) { LinkedHashMap() }.putAll(attributes)
            }
        }
    }

    private inline fun forEachResource(
        manager: ResourceManager,
        path: String,
        action: (ResourceLocation, Resource) -> Unit
    ) {
        for ((id, stack) in manager.listResourceStacks(path, ::isPathJson)) {
            for (resource in stack) action(id, resource)
        }
    }

    private inline fun <reified T> decode(id: ResourceLocation, resource: Resource): T? {
        return try {
            resource.open().use { DataAttributesSerialization.JSON.decodeFromStream<T>(it) }
        } catch (why: Exception) {
            DataAttributes.LOGGER.error(
                "Failed to parse Data Attributes resource [{}] from pack [{}].",
                id,
                resource.sourcePackId(),
                why
            )
            null
        }
    }
}
