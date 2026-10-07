package net.bms.data_attributes.api.event

/** Events related to Data Attributes configuration lifecycle. */
@Suppress("unused")
object AttributeEvents {
    /**
     * Fired after the effective attribute data has been rebuilt.
     *
     * Server: initial datapack load, datapack reload, or a server-config update.
     * Client: after receiving a fresh effective configuration from the server.
     */
    @JvmField
    val RELOADED = DataAttributeEvent<Reloaded> { listeners ->
        Reloaded { listeners.forEach(Reloaded::onReloadCompleted) }
    }

    fun interface Reloaded {
        fun onReloadCompleted()
    }
}
