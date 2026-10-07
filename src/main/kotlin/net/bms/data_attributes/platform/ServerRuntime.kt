package net.bms.data_attributes.platform

import net.minecraft.server.MinecraftServer

/** Current logical server, used only for post-reload client synchronization. */
object ServerRuntime {
    @Volatile
    var server: MinecraftServer? = null
        private set

    fun attach(value: MinecraftServer) {
        server = value
    }

    fun detach(value: MinecraftServer) {
        if (server === value) server = null
    }
}
