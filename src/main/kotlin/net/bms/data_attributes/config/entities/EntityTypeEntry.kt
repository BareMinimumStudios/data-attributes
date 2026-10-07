package net.bms.data_attributes.config.entities

import kotlinx.serialization.Serializable

/** Base value to attach to an entity type. */
@Serializable
data class EntityTypeEntry(var value: Double = 0.0)
