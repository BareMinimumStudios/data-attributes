package net.bms.data_attributes.api.attribute

import kotlinx.serialization.Serializable

@Serializable
enum class AttributeFormat(val function: (min: Double, max: Double, value: Double) -> String) {
    Percentage({ min, max, value ->
        var minimum = 0.0
        val maximum: Double
        val final: Double

        if (value < 0.0) {
            if (max < 0) minimum = max
            maximum = min
            final = (value + minimum) / (maximum + minimum)
        } else {
            if (min > 0) minimum = min
            maximum = max
            final = (value - minimum) / (maximum - minimum)
        }

        "%.2f".format(final * 100) + "%"
    }),
    Whole({ _, _, value -> "%.2f".format(value) });

    companion object {
        fun of(id: String) = if (id.equals("percentage", ignoreCase = true)) Percentage else Whole
    }
}
