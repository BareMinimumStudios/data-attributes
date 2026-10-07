package net.bms.data_attributes

import net.bms.data_attributes.config.Configs
import net.bms.data_attributes.networking.ConfigSyncPayload
import net.bms.data_attributes.platform.PlatformNetworking
import net.bms.data_attributes.platform.ServerRuntime
import net.bms.data_attributes.resource.FabricAttributesReloadListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.server.packs.PackType

object DataAttributesFabricEntrypoint : ModInitializer {
    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC)

        PlatformNetworking.install(
            broadcastHandler = { server, payload ->
                for (player in server.playerList.players) {
                    if (ServerPlayNetworking.canSend(player, ConfigSyncPayload.TYPE)) {
                        ServerPlayNetworking.send(player, payload)
                    }
                }
            },
            playerHandler = { player, payload ->
                if (ServerPlayNetworking.canSend(player, ConfigSyncPayload.TYPE)) {
                    ServerPlayNetworking.send(player, payload)
                }
            }
        )

        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(FabricAttributesReloadListener)

        ServerLifecycleEvents.SERVER_STARTING.register(ServerRuntime::attach)
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerRuntime::detach)
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> DataAttributes.sync(handler.player) }

        Configs.init()
    }
}
