package net.bms.data_attributes.api.event

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier

/** Attribute mutation hooks used on both logical sides. */
object AttributeModifiedEvents {
    /** Fired when an attribute instance is modified. */
    @JvmField
    val MODIFIED = DataAttributeEvent<Modified> { listeners ->
        Modified { attribute, entity, modifier, previousValue, wasAdded ->
            listeners.forEach {
                it.onModified(attribute, entity, modifier, previousValue, wasAdded)
            }
        }
    }

    /**
     * Fired immediately before Data Attributes clamps a calculated value.
     * Listeners are applied in registration order and may transform the value.
     */
    @JvmField
    val CLAMPED = DataAttributeEvent<Clamped> { listeners ->
        Clamped { attribute, value ->
            listeners.fold(value) { current, listener -> listener.onClamped(attribute, current) }
        }
    }

    fun interface Modified {
        fun onModified(
            attribute: Attribute?,
            livingEntity: LivingEntity?,
            modifier: AttributeModifier?,
            prevValue: Double,
            isWasAdded: Boolean
        )
    }

    fun interface Clamped {
        fun onClamped(attribute: Attribute?, value: Double): Double
    }
}
