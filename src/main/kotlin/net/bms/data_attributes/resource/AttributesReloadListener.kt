package net.bms.data_attributes.resource

import net.bms.data_attributes.DataAttributes
import net.bms.data_attributes.config.Cache
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller

/** Loads Data Attributes datapack JSON off-thread and applies it atomically. */
object AttributesReloadListener : SimplePreparableReloadListener<Cache>() {
    override fun prepare(resourceManager: ResourceManager, profiler: ProfilerFiller): Cache {
        return AttributesResourceHandler.loadIntoCache(resourceManager)
    }

    override fun apply(cache: Cache, resourceManager: ResourceManager, profiler: ProfilerFiller) {
        DataAttributes.applyDatapackReload(cache)
    }
}
