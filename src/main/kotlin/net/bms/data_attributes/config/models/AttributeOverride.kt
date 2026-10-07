package net.bms.data_attributes.config.models

import kotlinx.serialization.Serializable
import net.bms.data_attributes.api.attribute.AttributeFormat
import net.bms.data_attributes.api.attribute.StackingFormula
import net.bms.data_attributes.mutable.MutableAttribute
import net.minecraft.world.entity.ai.attributes.Attribute

/**
 * User-facing override for a registered Minecraft attribute.
 *
 * A NaN minimum/maximum means "inherit the attribute's native bound". Keeping
 * that marker in the loader-neutral model lets datapacks and Fzzy Config use
 * the same runtime representation without depending on either loader.
 */
@Serializable
data class AttributeOverride(
    @JvmField var enabled: Boolean = true,
    @JvmField var min: Double = Double.NaN,
    @JvmField var max: Double = Double.NaN,
    @JvmField var smoothness: Double = 1.0,
    @JvmField var formula: StackingFormula = StackingFormula.Flat,
    @JvmField var format: AttributeFormat = AttributeFormat.Whole,
) {
    fun override(attribute: Attribute) {
        (attribute as MutableAttribute).`data_attributes$override`(this)
    }
}
