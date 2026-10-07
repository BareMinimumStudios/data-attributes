package net.bms.data_attributes.api.attribute

import kotlinx.serialization.Serializable

/** Operation used by an attribute function when combining parent and child values. */
@Serializable
enum class StackingBehavior {
    Add,
    Multiply;

    val translationKey: String
        get() = "text.data_attributes.stackingBehavior.${name.lowercase()}"

    companion object {
        @JvmStatic
        fun of(id: String): StackingBehavior = if (id.equals("multiply", ignoreCase = true)) Multiply else Add
    }
}
