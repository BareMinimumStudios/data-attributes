package net.bms.data_attributes

import net.bms.data_attributes.config.Configs
import net.bms.data_attributes.networking.ConfigSyncPayload
import net.bms.data_attributes.platform.PlatformNetworking
import net.bms.data_attributes.platform.ServerRuntime
import net.bms.data_attributes.resource.AttributesReloadListener
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.AddReloadListenerEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.minecraft.server.level.ServerPlayer

@Mod(DataAttributes.MOD_ID)
class DataAttributesNeoforgeEntrypoint(modBus: IEventBus) {
    init {
        PlatformNetworking.install(
            broadcastHandler = { _, payload -> PacketDistributor.sendToAllPlayers(payload) },
            playerHandler = { player, payload -> PacketDistributor.sendToPlayer(player, payload) }
        )

        modBus.addListener(::registerPayloads)
        NeoForge.EVENT_BUS.addListener(::addReloadListener)
        NeoForge.EVENT_BUS.addListener(::playerLoggedIn)
        NeoForge.EVENT_BUS.addListener(::serverStarting)
        NeoForge.EVENT_BUS.addListener(::serverStopped)

        Configs.init()
    }

    private fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        event.registrar("2").playToClient(
            ConfigSyncPayload.TYPE,
            ConfigSyncPayload.STREAM_CODEC
        ) { payload, context ->
            context.enqueueWork(Runnable { DataAttributesClient.receive(payload.packet) })
        }
    }

    private fun addReloadListener(event: AddReloadListenerEvent) {
        event.addListener(AttributesReloadListener)
    }

    private fun playerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        DataAttributes.sync(player)
    }

    private fun serverStarting(event: ServerStartingEvent) {
        ServerRuntime.attach(event.server)
    }

    private fun serverStopped(event: ServerStoppedEvent) {
        ServerRuntime.detach(event.server)
    }
}
