package com.bibireden.data_attributes.api.attribute;

import java.util.function.Function;

import net.minecraft.util.math.MathHelper;

public enum StackingFormula {
	Flat((byte)0, x -> x, (k, k2, v, v2, m) -> k - v),
	Diminished((byte)1, x -> MathHelper.clamp(x, -1.0D, 1.0D), (k, k2, v, v2, m) -> ((1.0D - v2) * Math.pow(1.0D - m, (v - v2) / m)) - ((1.0D - k2) * Math.pow(1.0D - m, (k - k2) / m)));
	
	private final byte id;
	private final Function<Double, Double> clamp;
	private final StackingFunction stack;
	
	StackingFormula(final byte id, final Function<Double, Double> clamp, final StackingFunction stack) {
		this.id = id;
		this.clamp = clamp;
		this.stack = stack;
	}

	public static StackingFormula of(final String id) {
		return id.equalsIgnoreCase("diminished") ? Diminished : Flat;
	}

	public static StackingFormula of(final byte id) {
		return switch(id) {
			case 0 -> Flat;
			case 1 -> Diminished;
			default -> Flat;
		};
	}
	
	public byte id() {
		return this.id;
	}
	
	public double max(final double current, final double input) {
		final double value = this.clamp.apply(input);
		return Math.max(current, Math.abs(value));
	}
	
	public double stack(final double current, final double input) {
		final double value = this.clamp.apply(input);
		return current + Math.abs(value);
	}
	
	public double result(final double k, final double k2, final double v, final double v2, final double increment) {
		return this.stack.result(k, k2, v, v2, increment);
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.id);
	}
}
