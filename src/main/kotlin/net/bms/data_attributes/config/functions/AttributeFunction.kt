package net.bms.data_attributes.config.functions

import kotlinx.serialization.Serializable
import net.bms.data_attributes.api.attribute.StackingBehavior
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.Attribute

/**
 * A relationship from one attribute to another. [behavior] defines whether the
 * child value is added or multiplied and [value] controls the relationship.
 */
@Serializable
data class AttributeFunction(
    var enabled: Boolean = true,
    var behavior: StackingBehavior = StackingBehavior.Add,
    var value: Double = 0.0
)
