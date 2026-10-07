package net.bms.data_attributes.resource

import net.bms.data_attributes.DataAttributes
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object FabricAttributesReloadListener : IdentifiableResourceReloadListener {
    override fun getFabricId(): ResourceLocation = DataAttributes.id("attribute_config")

    override fun reload(
        barrier: PreparableReloadListener.PreparationBarrier,
        resourceManager: ResourceManager,
        preparationsProfiler: ProfilerFiller,
        reloadProfiler: ProfilerFiller,
        backgroundExecutor: Executor,
        gameExecutor: Executor
    ): CompletableFuture<Void> = AttributesReloadListener.reload(
        barrier,
        resourceManager,
        preparationsProfiler,
        reloadProfiler,
        backgroundExecutor,
        gameExecutor
    )
}
