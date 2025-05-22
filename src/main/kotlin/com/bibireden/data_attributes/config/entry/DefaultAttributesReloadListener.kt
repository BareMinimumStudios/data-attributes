@file:UseSerializers(IdentifierSerializer::class)

package com.bibireden.data_attributes.config.entry

import com.bibireden.data_attributes.serde.IdentifierSerializer
import kotlinx.serialization.UseSerializers

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.entities.EntityTypeEntry
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.registry.Registries
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

typealias DatapackCaches = LinkedHashMap<Identifier, DefaultAttributesReloadListener.Cache>

/**
 * A listener meant to compile [Pack]'s based on load order for preference
 * and then be applied to the manager.
 */
@OptIn(ExperimentalSerializationApi::class)
class DefaultAttributesReloadListener : SimpleResourceReloadListener<DatapackCaches> {
    var caches: DatapackCaches = LinkedHashMap()

    @Serializable
    data class Overrides(var entries: LinkedHashMap<Identifier, AttributeOverride> = LinkedHashMap())
    @Serializable
    data class Functions(var entries: LinkedHashMap<Identifier, LinkedHashMap<Identifier, AttributeFunction>> = LinkedHashMap())
    @Serializable
    data class EntityTypes(var entries: LinkedHashMap<Identifier, LinkedHashMap<Identifier, EntityTypeEntry>> = LinkedHashMap())

    data class Cache(
        val resource: Resource,
        val overrides: Overrides = Overrides(),
        val functions: Functions = Functions(),
        val types: EntityTypes = EntityTypes()
    )

    companion object {
        const val DIRECTORY = DataAttributes.MOD_ID
    }

    private fun isPathJson(id: Identifier) = id.path.endsWith(".json")

    override fun getFabricId(): Identifier = DataAttributes.id(DIRECTORY)

    override fun load(manager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture<DatapackCaches> {
        return CompletableFuture.supplyAsync {
            val caches: DatapackCaches = LinkedHashMap()

            readOverrides(manager, caches)
            readFunctions(manager, caches)
            readEntityTypes(manager, caches)

            // Filtering before applying the caches.
            for (cache in caches.values) {
                cache.overrides.entries = LinkedHashMap(cache.overrides.entries.filter { (id, _) -> Registries.ATTRIBUTE.containsId(id) })
                cache.functions.entries.forEach { (id, functions) ->
                    if (!Registries.ATTRIBUTE.containsId(id)) {
                        cache.functions.entries.remove(id)
                        return@forEach
                    }
                    cache.functions.entries[id] = LinkedHashMap(functions.filter { (id2) -> Registries.ATTRIBUTE.containsId(id2) })
                }
                cache.types.entries.forEach { (id, data) ->
                    if (!Registries.ENTITY_TYPE.containsId(id)) {
                        cache.types.entries.remove(id)
                        return@forEach
                    }
                    cache.types.entries[id] = LinkedHashMap(data.filter { (id2) -> Registries.ATTRIBUTE.containsId(id2) })
                }
            }

            caches
        }
    }

    override fun apply(caches: DatapackCaches, manager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync { DataAttributes.MANAGER.defaults = caches }
    }

    private fun computeResourceToCache(caches: DatapackCaches, id: Identifier, resource: Resource): Cache {
        return caches.computeIfAbsent(Identifier.of(id.namespace, "pack")!!) { Cache(resource) }
    }

    private fun readOverrides(manager: ResourceManager, caches: DatapackCaches) {
        val path = "$DIRECTORY/overrides"

        manager.findResources(path, ::isPathJson).forEach { (resId, res) ->
            try {
                Json.decodeFromStream<Overrides>(res.inputStream).entries.forEach { (id, entry) ->
                    computeResourceToCache(caches, resId, res).overrides.entries.computeIfAbsent(id) { entry }
                }
            }
            catch (why: Exception) {
                DataAttributes.LOGGER.error("Failed to parse overrides@$resId :: {}", why.message)
            }
        }
    }

    private fun readFunctions(manager: ResourceManager, caches: DatapackCaches) {
        val path = "$DIRECTORY/functions"

        manager.findResources(path, ::isPathJson).forEach { (resId, res) ->
            try {
                Json.decodeFromStream<Functions>(res.inputStream).entries.forEach { (id, entry) ->
                    val cache = computeResourceToCache(caches, resId, res)
                    val presentEntry = cache.functions.entries[id]
                    if (presentEntry == null) {
                        cache.functions.entries[id] = entry
                    }
                    else {
                        for ((secondaryId, secondaryValue) in entry) {
                            presentEntry.computeIfAbsent(secondaryId) { secondaryValue }
                        }
                        cache.functions.entries[id] = presentEntry
                    }
                }
            }
            catch (why: Exception) {
                DataAttributes.LOGGER.error("Failed to parse functions@$resId :: {}", why.message)
            }
        }
    }

    private fun readEntityTypes(manager: ResourceManager, caches: DatapackCaches) {
        val path = "$DIRECTORY/entity_types"

        manager.findResources(path, ::isPathJson).forEach { (resId, res) ->
            try {
                Json.decodeFromStream<EntityTypes>(res.inputStream).entries.forEach { (id, entry) ->
                    val cache = computeResourceToCache(caches, resId, res)
                    val presentEntry = cache.types.entries[id]
                    if (presentEntry == null) {
                        cache.types.entries[id] = entry
                    }
                    else {
                        for ((secondaryId, secondaryValue) in entry) {
                            presentEntry.computeIfAbsent(secondaryId) { secondaryValue }
                        }
                        cache.types.entries[id] = presentEntry
                    }
                }
            }
            catch (why: Exception) {
                DataAttributes.LOGGER.error("Failed to parse entity-types@$resId :: {}", why.message)
            }
        }
    }
}