package net.bms.data_attributes

import net.bms.data_attributes.config.Cache
import net.bms.data_attributes.config.impl.AttributeConfigManager
import net.bms.data_attributes.networking.ConfigSyncPayload
import net.bms.data_attributes.platform.PlatformNetworking
import net.bms.data_attributes.platform.ServerRuntime
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DataAttributes {
    companion object {
        const val MOD_ID = "data_attributes"

        @JvmField
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)

        @JvmField
        val MANAGER = AttributeConfigManager()

        /** Creates a [ResourceLocation] in the Data Attributes namespace. */
        @JvmStatic
        fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)

        /** Acquires the proper manager based on the level's logical side. */
        @JvmStatic
        fun getManagerFromLevel(world: Level): AttributeConfigManager =
            if (world.isClientSide) DataAttributesClient.MANAGER else MANAGER

        /** Rebuilds effective data after an in-game Fzzy Config update and syncs clients. */
        @JvmStatic
        fun reload(server: MinecraftServer) {
            MANAGER.nextUpdateFlag()
            MANAGER.update()
            PlatformNetworking.broadcast(server, ConfigSyncPayload(MANAGER.toPacket()))
        }

        /** Applies freshly loaded datapack defaults, then reapplies explicit server config overrides. */
        @JvmStatic
        fun applyDatapackReload(cache: Cache) {
            MANAGER.defaults = cache
            MANAGER.nextUpdateFlag()
            MANAGER.update()

            ServerRuntime.server?.let { server ->
                PlatformNetworking.broadcast(server, ConfigSyncPayload(MANAGER.toPacket()))
            }
        }

        /** Sends the current effective state to one joining player. */
        @JvmStatic
        fun sync(player: ServerPlayer) {
            PlatformNetworking.send(player, ConfigSyncPayload(MANAGER.toPacket()))
        }
    }
}
