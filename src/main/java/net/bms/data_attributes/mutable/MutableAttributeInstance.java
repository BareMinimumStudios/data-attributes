package net.bms.data_attributes.mutable;

import net.bms.data_attributes.api.util.VoidConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface MutableAttributeInstance {

    /** Returns the unique identifier associated with this instance. */
    default ResourceLocation data_attributes$get_id() { return null; }

    /** Executes an action involving the instance and a provided {@link AttributeModifier}. */
    default void data_attributes$actionModifier(final VoidConsumer consumerIn, final AttributeInstance instanceIn, final AttributeModifier modifierIn, final boolean isWasAdded) {}

    /** Sets a callback for changes to the associated {@link AttributeMap}. */
    default void data_attributes$setContainerCallback(final AttributeMap mapIn) {}

    /** Updates the identifier of the instance. */
    default void data_attributes$updateId(final ResourceLocation identifierIn) {}

    /** True when the live base still belongs to the entity supplier/config rather than a runtime mutation. */
    default boolean data_attributes$isSupplierOwnedBase() { return true; }

    /** Updates whether the live base belongs to the entity supplier/config. */
    default void data_attributes$setSupplierOwnedBase(final boolean supplierOwned) {}

    /** Applies a supplier/config base without marking it as an external runtime base mutation. */
    default void data_attributes$applySupplierBase(final double value) {}

    /** Updates the instance. */
    default void data_attributes$refresh() {}
}
