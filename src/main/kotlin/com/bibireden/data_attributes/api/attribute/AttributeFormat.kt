package com.bibireden.data_attributes.api.attribute

import io.wispforest.endec.Endec

enum class AttributeFormat(val function: (min: Double, max: Double, value: Double) -> String) {
    Percentage ({min, max, value -> "${(value-min)/((max-min)/100)}%" }),
    Whole({_, _, value -> "$value"} );

    companion object {
        val ENDEC: Endec<AttributeFormat> = Endec.STRING.xmap(AttributeFormat::of) { it.name }

        fun of(id: String) = if (id.equals("percentage", ignoreCase = true)) Percentage else Whole
    }
}