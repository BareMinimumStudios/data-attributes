package net.bms.data_attributes

import net.bms.data_attributes.config.impl.AttributeConfigManager
import net.bms.data_attributes.networking.NetworkingChannels

object DataAttributesClient {
    @JvmField
    val MANAGER = AttributeConfigManager()

    /** Whenever the client receives a sync packet from the server to update the world-state via. configuration. */
    private fun onPacketReceived(packet: AttributeConfigManager.Packet) {
        MANAGER.readPacket(packet)
        MANAGER.onDataUpdate()
    }

    fun init() {
        NetworkingChannels.RELOAD.registerClientbound(AttributeConfigManager.Packet::class.java, AttributeConfigManager.Packet.ENDEC) { packet, _ ->
            onPacketReceived(packet)
        }
    }
}