package net.bms.data_attributes.mutable;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public interface MutableAttributeMap {

    /** Returns a map of custom {@link AttributeInstance}'s associated with this container. */
    default Map<ResourceLocation, AttributeInstance> data_attributes$custom() { return new HashMap<>(); }

    /** Returns the {@link LivingEntity} associated with this container. */
    default LivingEntity data_attributes$getLivingEntity() { return null; }

    /** Sets the {@link LivingEntity} associated with this container. */
    default void data_attributes$setLivingEntity(final LivingEntity livingEntity) {}

    /** Refreshes the attributes of the associated {@link LivingEntity}. */
    default void data_attributes$refresh() {}

    /**
     * Returns the immutable supplier/base value this map was created with for an
     * attribute, or {@link Double#NaN} when the supplier does not contain it.
     *
     * This is deliberately distinct from {@link AttributeMap#getBaseValue}; the
     * latter returns a live instance's possibly-mutated base value once the
     * attribute has been instantiated.
     */
    default double data_attributes$getSupplierBaseValue(final Holder<Attribute> attribute) { return Double.NaN; }

    /**
     * Moves live state from an older map into this map during a Data Attributes
     * hot reload. Runtime modifiers are retained, while base values that still
     * match the old configured supplier baseline adopt the new configured base.
     */
    default void data_attributes$migrateLiveState(final AttributeMap previous) {}
}
