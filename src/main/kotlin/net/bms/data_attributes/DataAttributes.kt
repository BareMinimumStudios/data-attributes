package net.bms.data_attributes

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import net.bms.data_attributes.config.DataAttributesConfig
import net.bms.data_attributes.config.impl.AttributeConfigManager
import net.bms.data_attributes.networking.ConfigPacketBufs
import net.bms.data_attributes.networking.NetworkingChannels
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DataAttributes {
    companion object {
        const val MOD_ID = "data_attributes"

        @JvmField val LOGGER: Logger = LogManager.getLogger()

        val MANAGER = AttributeConfigManager()

        /** Creates an [ResourceLocation] associated with the [MOD_ID]. */
        fun id(str: String) = ResourceLocation.tryBuild(MOD_ID, str)!!

        /** Acquires the proper manager based on the level. */
        fun getManagerFromLevel(world: Level) = if (world.isClientSide) DataAttributesClient.MANAGER else MANAGER

        /**
         * Initiates a reload of config and default data for the server's [AttributeConfigManager].
         * Changes are then networked to the client.
         * */
        @JvmStatic
        fun reload(server: MinecraftServer) {
            MANAGER.update()
            MANAGER.nextUpdateFlag()

            NetworkingChannels.RELOAD.serverHandle(server).send(MANAGER.toPacket())
        }

        init {
            ConfigPacketBufs.registerPacketSerializers()
        }
    }
}