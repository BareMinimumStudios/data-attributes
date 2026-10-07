package net.bms.data_attributes

import net.bms.data_attributes.config.impl.AttributeConfigManager

object DataAttributesClient {
    @JvmField
    val MANAGER = AttributeConfigManager()

    @Volatile
    private var datapackPreviewSink: ((AttributeConfigManager.Data) -> Unit)? = null

    /**
     * Loader-side Fzzy Config implementations install this optional callback so
     * the GUI can show the raw datapack baseline without coupling common code to
     * Fzzy Config or either loader's mapped classes.
     */
    @JvmStatic
    fun installDatapackPreviewSink(sink: (AttributeConfigManager.Data) -> Unit) {
        datapackPreviewSink = sink
    }

    /** Applies a server-authoritative runtime snapshot received by the loader networking layer. */
    @JvmStatic
    fun receive(packet: AttributeConfigManager.Packet) {
        MANAGER.readPacket(packet)
        MANAGER.onDataUpdate()
        datapackPreviewSink?.invoke(packet.datapackDefaults)
    }
}
