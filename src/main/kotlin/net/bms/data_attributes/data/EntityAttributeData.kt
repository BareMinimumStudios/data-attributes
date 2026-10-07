package net.bms.data_attributes.data

import net.bms.data_attributes.DataAttributes
import net.bms.data_attributes.config.functions.AttributeFunction
import net.bms.data_attributes.config.models.AttributeOverride
import net.bms.data_attributes.mutable.MutableAttribute
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.Attribute

/** Runtime aggregation of an override and its child attribute functions. */
class AttributeData(
    val override: AttributeOverride? = null,
    val functions: MutableMap<ResourceLocation, AttributeFunction> = mutableMapOf()
) {
    constructor(value: AttributeOverride) : this(value, mutableMapOf())

    fun override(attribute: Attribute) {
        override?.override(attribute)
    }

    fun copy(attribute: Attribute) {
        with(attribute as MutableAttribute) {
            for ((id, function) in functions) {
                val childAttribute = BuiltInRegistries.ATTRIBUTE[id] as? MutableAttribute ?: continue
                this.`data_attributes$addChild`(childAttribute, function)
            }
        }
    }

    fun putFunctions(functions: Map<ResourceLocation, AttributeFunction>) {
        for ((id, function) in functions) {
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(id)) {
                DataAttributes.LOGGER.warn(
                    "The attribute function child [{}] is not registered. The function will be ignored until that attribute exists.",
                    id
                )
                continue
            }
            this.functions[id] = function
        }
    }
}
