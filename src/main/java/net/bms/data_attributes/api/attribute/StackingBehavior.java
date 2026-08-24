package net.bms.data_attributes.api.attribute;

import net.bms.data_attributes.config.functions.AttributeFunction;

/**
 * Used to factor into {@link AttributeFunction}'s
 * to determine what operation to use when calculating how their modifier(s) are applied.
 *	<p/>
 * @since 1.4.0
 * @author Bare Minimum Studios (B.M.S), CleverNucleus
 */
public enum StackingBehavior {
	/**
	 * Addition of values as defined by the parent attribute.
	 * <p/>
	 * Equivalent to {@link net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation#ADDITION}.
	 */
	Add,
	/**
	 * Multiplication of parent attribute.
	 * <p>
	 * Equivalent to {@link net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation#MULTIPLY_TOTAL}.
	 */
	Multiply;

	public static StackingBehavior of(final String id) {
		return id.equalsIgnoreCase("multiply") ? Multiply : Add;
	}

	public String getTranslationKey() { return "text.data_attributes.stackingBehavior." + this.name().toLowerCase(); }
}
