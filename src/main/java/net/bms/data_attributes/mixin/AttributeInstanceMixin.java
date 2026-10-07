package net.bms.data_attributes.mixin;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.bms.data_attributes.mutable.MutableAttribute;
import net.bms.data_attributes.mutable.MutableAttributeMap;
import net.bms.data_attributes.mutable.MutableAttributeModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.bms.data_attributes.api.attribute.StackingBehavior;
import net.bms.data_attributes.api.attribute.IAttribute;
import net.bms.data_attributes.api.attribute.IAttributeInstance;
import net.bms.data_attributes.api.attribute.StackingFormula;
import net.bms.data_attributes.api.event.AttributeModifiedEvents;
import net.bms.data_attributes.api.util.VoidConsumer;
import net.bms.data_attributes.mutable.MutableAttributeInstance;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttributeInstance.class)
abstract class AttributeInstanceMixin implements MutableAttributeInstance, IAttributeInstance {
	@Unique
	private static final String DATA_ATTRIBUTES_SUPPLIER_BASE = "data_attributes:supplier_base";

	@Unique
	private static final String DATA_ATTRIBUTES_SUPPLIER_OWNED = "data_attributes:supplier_owned";

	@Unique
	private AttributeMap data_attributes$container;

	@Unique
	private ResourceLocation data_attributes$id;

	@Unique
	private boolean data_attributes$supplierOwnedBase = true;

	@Unique
	private boolean data_attributes$applyingSupplierBase = false;

	@Final
	@Shadow
	private Holder<Attribute> attribute;

	@Final
	@Shadow
	private Map<ResourceLocation, AttributeModifier> modifierById;

	@Shadow
	private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation operation) {
		return Collections.emptySet();
	}

	@Shadow
	protected void setDirty() {}

	@Shadow public abstract double getBaseValue();
	@Shadow public abstract void setBaseValue(double value);

	@Shadow @Nullable public abstract AttributeModifier getModifier(ResourceLocation id);

	@Shadow abstract Map<ResourceLocation, AttributeModifier> getModifiers(AttributeModifier.Operation operation);

	@Shadow public abstract Holder<Attribute> getAttribute();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(Holder<Attribute> attribute, Consumer<AttributeInstance> onDirty, CallbackInfo ci) {
		this.data_attributes$id = BuiltInRegistries.ATTRIBUTE.getKey(this.attribute.value());
	}

	@ModifyReturnValue(method = "getAttribute", at = @At("RETURN"))
	private Holder<Attribute> data_attributes$getAttribute(Holder<Attribute> original) {
		if (this.data_attributes$id == null) return original;
		Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(this.data_attributes$id);
		return attribute != null ? BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute) : original;
	}

	@SuppressWarnings("UnreachableCode")
	@Inject(method = "calculateValue", at = @At("HEAD"), cancellable = true)
	private void data_attributes$calculateValue(CallbackInfoReturnable<Double> cir) {
		Attribute attribute = this.getAttribute().value();
		StackingFormula formula = ((MutableAttribute) attribute).data_attributes$formula();

		// If the formula is set to Flat and there is no associated container, drop out early.
		if (formula == StackingFormula.Flat && this.data_attributes$container == null) return;

		AtomicReference<Double> k = new AtomicReference<>(0.0D);
		AtomicReference<Double> v = new AtomicReference<>(0.0D);

		double k2 = 0.0D;
		double v2 = 0.0D;

		if (this.getBaseValue() > 0.0D) {
			k.set(formula.stack(k.get(), this.getBaseValue()));
			k2 = formula.max(k2, this.getBaseValue());
		} else {
			v.set(formula.stack(v.get(), this.getBaseValue()));
			v2 = formula.max(v2, this.getBaseValue());
		}

		for (AttributeModifier modifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
			double value = modifier.amount();

			if (value > 0.0D) {
				k.set(formula.stack(k.get(), value));
				k2 = formula.max(k2, value);
			} else {
				v.set(formula.stack(v.get(), value));
				v2 = formula.max(v2, value);
			}
		}

		if (this.data_attributes$container != null) {
			((MutableAttribute) attribute).data_attributes$parentsMutable().forEach((parentAttribute, function) -> {
				if (!function.getEnabled() || function.getBehavior() != StackingBehavior.Add) return;

				AttributeInstance parentInstance = this.data_attributes$container.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) parentAttribute));
				if (parentInstance == null) return;

				double multiplier = function.getValue();
				double value = multiplier * parentInstance.getValue();

				if (value > 0.0D) {
					k.set(formula.stack(k.get(), value));
				} else {
					v.set(formula.stack(v.get(), value));
				}
			});
		}

		double d = ((MutableAttribute) attribute).data_attributes$sum(k.get(), k2, v.get(), v2, (AttributeInstance) (Object) this);
		AtomicReference<Double> e = new AtomicReference<>(d);

		for (AttributeModifier modifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
			e.set(e.get() + (d * modifier.amount()));
		}

		for (AttributeModifier modifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
			e.set(e.get() * (1.0D + modifier.amount()));
		}

		if (this.data_attributes$container != null) {
			((MutableAttribute) attribute).data_attributes$parentsMutable().forEach((parentAttribute, function) -> {
				if (!function.getEnabled() || function.getBehavior() != StackingBehavior.Multiply) return;

				AttributeInstance parentInstance = this.data_attributes$container.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) parentAttribute));
				if (parentInstance == null) return;

				e.set(e.get() * (1.0D + (parentInstance.getValue() * function.getValue())));
			});
		}

		cir.setReturnValue(attribute.sanitizeValue(e.get()));
	}

	@Inject(method = "addModifier", at = @At("HEAD"), cancellable = true)
	private void data_attributes$addModifier(AttributeModifier modifier, CallbackInfo ci) {
		ResourceLocation key = modifier.id();
		AttributeModifier AttributeModifier = this.modifierById.get(key);

		if (AttributeModifier != null) {
			throw new IllegalArgumentException("Modifier is already applied on this attribute!");
		} else {
			this.data_attributes$actionModifier(
				() -> {
					this.modifierById.put(key, modifier);
					this.getModifiers(modifier.operation()).put(key, modifier);
				}, (AttributeInstance) (Object) this, modifier, true
			);
		}

		ci.cancel();
	}

	@WrapMethod(method = "addOrUpdateTransientModifier")
	private void data_attributes$addOrUpdateTransientModifier(AttributeModifier modifier, Operation<Void> original) {
		AttributeModifier previous = this.modifierById.get(modifier.id());
		if (previous == modifier) {
			original.call(modifier);
			return;
		}

		this.data_attributes$actionModifier(
			() -> original.call(modifier),
			(AttributeInstance) (Object) this,
			modifier,
			previous == null
		);
	}

	@WrapMethod(method = "setBaseValue")
	private void data_attributes$setBaseValue(double value, Operation<Void> original) {
		// Runtime base mutations normally become independent entity state. If the
		// caller deliberately sets the base back to the entity supplier/config
		// baseline, however, opt it back into supplier ownership. This also gives
		// worlds affected by older builds a safe one-time repair path: setting an
		// entity back to its configured/default base makes future hot reloads track
		// Entity Base Attributes again.
		if (this.data_attributes$container != null && !this.data_attributes$applyingSupplierBase) {
			double supplierBase = ((MutableAttributeMap) this.data_attributes$container)
				.data_attributes$getSupplierBaseValue(this.getAttribute());
			this.data_attributes$supplierOwnedBase = !Double.isNaN(supplierBase)
				&& Double.compare(value, supplierBase) == 0;
		}

		if (Double.compare(value, this.getBaseValue()) == 0) {
			original.call(value);
			return;
		}

		this.data_attributes$actionModifier(
			() -> original.call(value),
			(AttributeInstance) (Object) this,
			null,
			false
		);
	}

	@WrapMethod(method = "removeModifier(Lnet/minecraft/resources/ResourceLocation;)Z")
	private boolean data_attributes$removeModifier(ResourceLocation id, Operation<Boolean> original) {
		AttributeModifier modifier = this.modifierById.get(id);
		if (modifier == null || this.data_attributes$container == null) {
			return original.call(id);
		}

		final boolean[] removed = { false };
		this.data_attributes$actionModifier(
			() -> removed[0] = original.call(id),
			(AttributeInstance) (Object) this,
			modifier,
			false
		);
		return removed[0];
	}

	@WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putString(Ljava/lang/String;Ljava/lang/String;)V", ordinal = 0))
	private void data_attributes$save(CompoundTag instance, String key, String value, Operation<Void> original) {
		String entry;
		if (this.data_attributes$id == null) {
			ResourceLocation location = BuiltInRegistries.ATTRIBUTE.getKey(this.attribute.value());
			if (location != null) {
				entry = location.toString();
			}
			else {
				original.call(instance, key, value);
				return;
			}
		}
		else {
			entry = this.data_attributes$id.toString();
		}
		instance.putString("id", entry);
	}

	@Inject(method = "save", at = @At("RETURN"))
	private void data_attributes$saveSupplierBaseline(CallbackInfoReturnable<CompoundTag> cir) {
		if (this.data_attributes$container == null) return;

		double supplierBase = ((MutableAttributeMap) this.data_attributes$container)
			.data_attributes$getSupplierBaseValue(this.getAttribute());
		if (!Double.isNaN(supplierBase)) {
			cir.getReturnValue().putDouble(DATA_ATTRIBUTES_SUPPLIER_BASE, supplierBase);
			cir.getReturnValue().putBoolean(DATA_ATTRIBUTES_SUPPLIER_OWNED, this.data_attributes$supplierOwnedBase);
		}
	}

	@Inject(method = "load", at = @At("RETURN"))
	private void data_attributes$loadSupplierBaseline(CompoundTag tag, CallbackInfo ci) {
		if (this.data_attributes$container == null) return;

		// 3.0.0 stores explicit provenance. Early 3.0 development builds stored
		// only the previous supplier baseline, so numeric equality is used solely as
		// a compatibility bridge for those test saves. Older saves remain preserved.
		if (tag.contains(DATA_ATTRIBUTES_SUPPLIER_OWNED, 1)) {
			this.data_attributes$supplierOwnedBase = tag.getBoolean(DATA_ATTRIBUTES_SUPPLIER_OWNED);
		}
		else if (tag.contains(DATA_ATTRIBUTES_SUPPLIER_BASE, 6)) {
			double previousSupplierBase = tag.getDouble(DATA_ATTRIBUTES_SUPPLIER_BASE);
			double serializedBase = tag.getDouble("base");
			this.data_attributes$supplierOwnedBase = Double.compare(serializedBase, previousSupplierBase) == 0;
		}
		else {
			this.data_attributes$supplierOwnedBase = false;
		}

		if (!this.data_attributes$supplierOwnedBase) return;

		double currentSupplierBase = ((MutableAttributeMap) this.data_attributes$container)
			.data_attributes$getSupplierBaseValue(this.getAttribute());
		if (!Double.isNaN(currentSupplierBase) && Double.compare(this.getBaseValue(), currentSupplierBase) != 0) {
			this.data_attributes$applySupplierBase(currentSupplierBase);
		}
	}

	@Override
	public ResourceLocation data_attributes$get_id() {
		return this.data_attributes$id;
	}

	@Override
	public void data_attributes$actionModifier(final VoidConsumer consumerIn, final AttributeInstance instanceIn, final AttributeModifier modifierIn, final boolean isWasAdded) {
		if (this.data_attributes$container == null) {
			consumerIn.accept();
			this.setDirty();
			return;
		}

		Attribute parent = this.getAttribute().value();

		for (IAttribute child : ((MutableAttribute) parent).data_attributes$childrenMutable().keySet()) {
			AttributeInstance instance = this.data_attributes$container.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) child));
			if (instance != null) instance.getValue();
		}

		final double value = instanceIn.getValue();

		consumerIn.accept();

		this.setDirty();

		LivingEntity livingEntity = ((MutableAttributeMap) this.data_attributes$container).data_attributes$getLivingEntity();

		AttributeModifiedEvents.MODIFIED.invoker().onModified(parent, livingEntity, modifierIn, value, isWasAdded);

		for (IAttribute child : ((MutableAttribute) parent).data_attributes$childrenMutable().keySet()) {
			AttributeInstance instance = this.data_attributes$container.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) child));
			if (instance != null) {
				((MutableAttributeInstance) instance).data_attributes$actionModifier(() -> {}, instance, modifierIn, isWasAdded);
			}
		}
	}

	@Override
	public void data_attributes$setContainerCallback(final AttributeMap mapIn) {
		this.data_attributes$container = mapIn;
	}

	@Override
	public void data_attributes$updateId(final ResourceLocation resource) {
		this.data_attributes$id = resource;
	}

	@Override
	public boolean data_attributes$isSupplierOwnedBase() {
		return this.data_attributes$supplierOwnedBase;
	}

	@Override
	public void data_attributes$setSupplierOwnedBase(final boolean supplierOwned) {
		this.data_attributes$supplierOwnedBase = supplierOwned;
	}

	@Override
	public void data_attributes$applySupplierBase(final double value) {
		this.data_attributes$applyingSupplierBase = true;
		try {
			this.setBaseValue(value);
			this.data_attributes$supplierOwnedBase = true;
		}
		finally {
			this.data_attributes$applyingSupplierBase = false;
		}
	}

	@Override
	public void data_attributes$updateModifier(final ResourceLocation id, final double value) {
		AttributeModifier modifier = this.getModifier(id);
		if (modifier == null) return;

		this.data_attributes$actionModifier(() -> ((MutableAttributeModifier) (Object) modifier).data_attributes$updateValue(value), (AttributeInstance) (Object) this, modifier, false);
	}

	@Override
	public void data_attributes$refresh() { this.setDirty(); }
}
