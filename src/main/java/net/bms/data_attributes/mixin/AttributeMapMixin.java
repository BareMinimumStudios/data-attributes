package net.bms.data_attributes.mixin;

import java.util.*;

import net.bms.data_attributes.api.event.AttributeModifiedEvents;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.bms.data_attributes.mutable.MutableAttributeInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.bms.data_attributes.mutable.MutableAttributeMap;

@Mixin(AttributeMap.class)
abstract class AttributeMapMixin implements MutableAttributeMap {
	@Unique
	private final Map<ResourceLocation, AttributeInstance> data_attributes$custom = new HashMap<>();

	@Unique
	private LivingEntity data_attributes$livingEntity;

	@Final
	@Shadow
	private AttributeSupplier supplier;

	@Shadow
	private void onAttributeModified(AttributeInstance instance) {}

	@Shadow @Nullable public abstract AttributeInstance getInstance(Holder<Attribute> attribute);

	@ModifyExpressionValue(method = "getSyncableAttributes", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<?> data_attributes$getAttributesToSend(Collection<?> original) {
		return this.data_attributes$custom.values();
	}

	@Inject(method = "getInstance", at = @At("HEAD"), cancellable = true)
	private void data_attributes$getInstance(Holder<Attribute> holder, CallbackInfoReturnable<AttributeInstance> cir) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		if (identifier == null) return;

		AttributeInstance instance = this.data_attributes$custom.computeIfAbsent(identifier, id -> this.supplier.createInstance(this::onAttributeModified, holder));
		if (instance != null) {
			MutableAttributeInstance mai = ((MutableAttributeInstance) instance);
			mai.data_attributes$setContainerCallback((AttributeMap) (Object) this);
			if (mai.data_attributes$get_id() == null) {
				mai.data_attributes$updateId(identifier);
			}
		}

		// Cancel before vanilla's Holder-keyed map can create a second copy of
		// the same AttributeInstance. All live instances are ResourceLocation-
		// keyed in data_attributes$custom.
		cir.setReturnValue(instance);
	}

	@ModifyReturnValue(method = "hasAttribute", at = @At("RETURN"))
	private boolean data_attributes$hasAttribute(boolean original, Holder<Attribute> attribute) {
		var identifier = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
		return this.data_attributes$custom.get(identifier) != null || original;
	}

	@ModifyExpressionValue(method = "hasModifier", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$hasModifier(Object original, @Local(argsOnly = true) Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		AttributeInstance value = this.data_attributes$custom.get(identifier);
		return value != null ? value : original;
	}

	@ModifyExpressionValue(method = "getValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getValue(Object original, @Local(argsOnly = true) Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		return this.data_attributes$custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getBaseValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getBaseValue(Object attribute, @Local(argsOnly = true) Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		return this.data_attributes$custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getModifierValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getModifierValue(Object attribute, @Local(argsOnly = true) Holder<Attribute> holder) {
		return this.data_attributes$custom.get(BuiltInRegistries.ATTRIBUTE.getKey(holder.value()));
	}

	@Inject(method = "removeAttributeModifiers", at = @At("HEAD"), cancellable = true)
	private void data_attributes$removeModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers, CallbackInfo ci) {
		modifiers.asMap().forEach((holder, collection) -> {
			ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
			AttributeInstance AttributeInstance = this.data_attributes$custom.get(identifier);

			if (AttributeInstance != null) {
				collection.forEach(AttributeInstance::removeModifier);
			}
		});

		ci.cancel();
	}

	@Inject(method = "assignAllValues", at = @At("HEAD"), cancellable = true)
	private void data_attributes$setFrom(AttributeMap other, CallbackInfo ci) {
		((MutableAttributeMap) other).data_attributes$custom().values().forEach(attributeInstance -> {
			Holder<Attribute> holder = attributeInstance.getAttribute();
			AttributeInstance targetInstance = this.getInstance(holder);

			if (targetInstance != null) {
				double previousValue = targetInstance.getValue();
				targetInstance.replaceFrom(attributeInstance);
				AttributeModifiedEvents.MODIFIED.invoker().onModified(
					holder.value(),
					this.data_attributes$livingEntity,
					null,
					previousValue,
					false
				);
			}
		});

		ci.cancel();
	}

	@Inject(method = "assignBaseValues", at = @At("HEAD"), cancellable = true)
	private void data_attributes$setBaseValuesFrom(AttributeMap other, CallbackInfo ci) {
		((MutableAttributeMap) other).data_attributes$custom().values().forEach(attributeInstance -> {
			AttributeInstance target = this.getInstance(attributeInstance.getAttribute());
			if (target != null) {
				target.setBaseValue(attributeInstance.getBaseValue());
			}
		});

		ci.cancel();
	}

	@ModifyExpressionValue(method = "save", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<?> data_attributes$save(Collection<?> original) {
		return this.data_attributes$custom.values();
	}

	@Override
	public Map<ResourceLocation, AttributeInstance> data_attributes$custom() {
		return this.data_attributes$custom;
	}

	@Override
	public LivingEntity data_attributes$getLivingEntity() {
		return this.data_attributes$livingEntity;
	}

	@Override
	public void data_attributes$setLivingEntity(final LivingEntity livingEntity) {
		this.data_attributes$livingEntity = livingEntity;
	}

	@Override
	public void data_attributes$refresh() {
		this.data_attributes$custom.values().forEach(instance -> ((MutableAttributeInstance) instance).data_attributes$refresh());
	}

	@Override
	public double data_attributes$getSupplierBaseValue(final Holder<Attribute> attribute) {
		return this.supplier.hasAttribute(attribute) ? this.supplier.getBaseValue(attribute) : Double.NaN;
	}

	@Override
	public void data_attributes$migrateLiveState(final AttributeMap previous) {
		MutableAttributeMap oldMap = (MutableAttributeMap) previous;

		for (AttributeInstance oldInstance : oldMap.data_attributes$custom().values()) {
			Holder<Attribute> holder = oldInstance.getAttribute();
			AttributeInstance newInstance = this.getInstance(holder);
			if (newInstance == null) continue; // Attribute was removed from the rebuilt supplier.

			double newSupplierBase = this.data_attributes$getSupplierBaseValue(holder);
			MutableAttributeInstance oldMutable = (MutableAttributeInstance) oldInstance;
			MutableAttributeInstance newMutable = (MutableAttributeInstance) newInstance;
			boolean supplierOwnedBase = oldMutable.data_attributes$isSupplierOwnedBase();

			// replaceFrom is still the safest vanilla-compatible way to retain all
			// transient/permanent modifiers, but it also copies the old base. Base
			// provenance determines whether that copied value is retained or replaced
			// by the newly rebuilt entity supplier/config value.
			double previousValue = oldInstance.getValue();
			newInstance.replaceFrom(oldInstance);
			newMutable.data_attributes$setSupplierOwnedBase(supplierOwnedBase);

			boolean adoptedNewSupplierBase = supplierOwnedBase
				&& !Double.isNaN(newSupplierBase)
				&& Double.compare(newInstance.getBaseValue(), newSupplierBase) != 0;

			if (adoptedNewSupplierBase) {
				newMutable.data_attributes$applySupplierBase(newSupplierBase);
			}
			else {
				// replaceFrom dirties vanilla tracking but bypasses Data Attributes'
				// public mutation event. Preserve the historical assignAllValues
				// behavior without double-firing when supplier-base application handled it.
				AttributeModifiedEvents.MODIFIED.invoker().onModified(
					holder.value(),
					this.data_attributes$livingEntity,
					null,
					previousValue,
					false
				);
			}
		}
	}
}
