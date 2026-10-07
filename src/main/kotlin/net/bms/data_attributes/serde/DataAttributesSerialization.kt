package net.bms.data_attributes.serde

import kotlinx.serialization.json.Json

/** Shared JSON settings for datapacks and the server -> client runtime snapshot. */
object DataAttributesSerialization {
    @JvmField
    val JSON: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        allowSpecialFloatingPointValues = true
    }
}
