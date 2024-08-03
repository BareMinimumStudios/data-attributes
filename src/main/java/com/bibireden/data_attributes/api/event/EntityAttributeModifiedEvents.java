package com.bibireden.data_attributes.api.event;

import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;

/**
 * Holds some attribute events. Both occur on both server and client.
 * 
 * @author CleverNucleus
 *
 */
public final class EntityAttributeModifiedEvents {
	
	/**
	 * Fired when the value of an attribute instance was modified, either by adding/removing a modifier or changing 
	 * the value of the modifier, or by reloading the datapack and having the living entity renew its attribute container. 
	 * Living entity and modifiers may or may not be null.
	 */
	public static final Event<Modified> MODIFIED = EventFactory.createArrayBacked(Modified.class, callbacks -> (entry, livingEntity, modifier, prevValue, isWasAdded) -> {
		for(Modified callback : callbacks) {
			callback.onModified(entry, livingEntity, modifier, prevValue, isWasAdded);
		}
	});
	
	/**
	 * Fired after the attribute instance value was calculated, but before it was output. This offers one last chance to alter the 
	 * value in some way (for example round a decimal to an integer).
	 */
	public static final Event<Clamped> CLAMPED = EventFactory.createArrayBacked(Clamped.class, callbacks -> (entry, value) -> {
		double cache = value;
		
		for(Clamped callback : callbacks) {
			cache = callback.onClamped(entry, cache);
		}
		
		return cache;
	});
	
	@FunctionalInterface
	public interface Modified {
		void onModified(final RegistryEntry<EntityAttribute> entry, final @Nullable LivingEntity livingEntity, final @Nullable EntityAttributeModifier modifier, final double prevValue, final boolean isWasAdded);
	}
	
	@FunctionalInterface
	public interface Clamped {
		double onClamped(final RegistryEntry<EntityAttribute> entry, final double value);
	}
}
