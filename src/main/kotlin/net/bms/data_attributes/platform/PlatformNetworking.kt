package net.bms.data_attributes.platform

import net.bms.data_attributes.networking.ConfigSyncPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Tiny loader bridge used by common code. Loader entrypoints install the actual
 * Fabric/NeoForge networking implementation during initialization.
 */
object PlatformNetworking {
    @Volatile private var broadcast: ((MinecraftServer, ConfigSyncPayload) -> Unit)? = null
    @Volatile private var send: ((ServerPlayer, ConfigSyncPayload) -> Unit)? = null

    fun install(
        broadcastHandler: (MinecraftServer, ConfigSyncPayload) -> Unit,
        playerHandler: (ServerPlayer, ConfigSyncPayload) -> Unit
    ) {
        broadcast = broadcastHandler
        send = playerHandler
    }

    fun broadcast(server: MinecraftServer, payload: ConfigSyncPayload) {
        broadcast?.invoke(server, payload)
            ?: error("Data Attributes platform networking has not been initialized")
    }

    fun send(player: ServerPlayer, payload: ConfigSyncPayload) {
        send?.invoke(player, payload)
            ?: error("Data Attributes platform networking has not been initialized")
    }
}
