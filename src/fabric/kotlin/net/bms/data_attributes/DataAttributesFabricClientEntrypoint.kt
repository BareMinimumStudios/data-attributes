package net.bms.data_attributes

import net.bms.data_attributes.networking.ConfigSyncPayload
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object DataAttributesFabricClientEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE) { payload, _ ->
            DataAttributesClient.receive(payload.packet)
        }
    }
}
