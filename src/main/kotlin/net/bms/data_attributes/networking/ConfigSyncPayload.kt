package net.bms.data_attributes.networking

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.bms.data_attributes.DataAttributes
import net.bms.data_attributes.config.impl.AttributeConfigManager
import net.bms.data_attributes.serde.DataAttributesSerialization
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/** Native 1.21.1 payload for synchronizing the server-authoritative attribute configuration. */
data class ConfigSyncPayload(val packet: AttributeConfigManager.Packet) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<ConfigSyncPayload> = TYPE

    companion object {
        private const val MAX_WIRE_BYTES = 1_000_000
        private const val MAX_JSON_BYTES = 8 * 1024 * 1024

        @JvmField
        val TYPE = CustomPacketPayload.Type<ConfigSyncPayload>(DataAttributes.id("config_sync"))

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> =
            object : StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> {
                override fun encode(buffer: RegistryFriendlyByteBuf, value: ConfigSyncPayload) {
                    val bytes = encodePacket(value.packet)
                    require(bytes.size <= MAX_WIRE_BYTES) {
                        "Data Attributes config sync payload is too large (${bytes.size} bytes)"
                    }
                    buffer.writeByteArray(bytes)
                }

                override fun decode(buffer: RegistryFriendlyByteBuf): ConfigSyncPayload {
                    return ConfigSyncPayload(decodePacket(buffer.readByteArray(MAX_WIRE_BYTES)))
                }
            }

        private fun encodePacket(packet: AttributeConfigManager.Packet): ByteArray {
            val json = DataAttributesSerialization.JSON.encodeToString(packet)
            val raw = json.toByteArray(Charsets.UTF_8)
            require(raw.size <= MAX_JSON_BYTES) {
                "Data Attributes config snapshot is too large (${raw.size} bytes before compression)"
            }

            return ByteArrayOutputStream().use { output ->
                GZIPOutputStream(output).use { gzip -> gzip.write(raw) }
                output.toByteArray()
            }
        }

        private fun decodePacket(bytes: ByteArray): AttributeConfigManager.Packet {
            val output = ByteArrayOutputStream(minOf(bytes.size * 4, MAX_JSON_BYTES))
            GZIPInputStream(ByteArrayInputStream(bytes)).use { gzip ->
                val chunk = ByteArray(8192)
                var total = 0
                while (true) {
                    val read = gzip.read(chunk)
                    if (read < 0) break
                    total += read
                    require(total <= MAX_JSON_BYTES) { "Data Attributes config snapshot exceeds the decode limit" }
                    output.write(chunk, 0, read)
                }
            }
            return DataAttributesSerialization.JSON.decodeFromString(output.toByteArray().toString(Charsets.UTF_8))
        }
    }
}
