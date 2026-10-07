package net.bms.data_attributes.config.functions

import net.minecraft.resources.ResourceLocation

/** Legacy API container retained for source compatibility. */
data class AttributeFunctionConfig(
    val data: MutableMap<ResourceLocation, MutableMap<ResourceLocation, AttributeFunction>> = mutableMapOf()
)
