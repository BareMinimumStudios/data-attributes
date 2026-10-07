package net.bms.data_attributes.mixin;

import net.bms.data_attributes.api.DataAttributesAPI;
import net.bms.data_attributes.config.impl.AttributeConfigManager;
import net.bms.data_attributes.ext.LivingEntityKt;
import net.bms.data_attributes.mutable.MutableAttributeMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class, priority = 999)
abstract class LivingEntityMixin {
	@Unique
	private int data_attributes$update_flag;

	@Final @Shadow @Mutable private AttributeMap attributes;

	@Shadow public abstract AttributeMap getAttributes();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(EntityType<? extends LivingEntity> entityType, Level level, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		AttributeConfigManager manager = DataAttributesAPI.getManager(entity.level());
		this.attributes = manager.getContainer(entityType, entity);
		this.data_attributes$update_flag = manager.getUpdateFlag();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void data_attributes$tick(CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		AttributeConfigManager manager = DataAttributesAPI.getManager(entity.level());
		final int updateFlag = manager.getUpdateFlag();

		if (this.data_attributes$update_flag != updateFlag) {
			@SuppressWarnings("unchecked")
			AttributeMap attributeMap = manager.getContainer((EntityType<? extends LivingEntity>) entity.getType(), entity);
			((MutableAttributeMap) attributeMap).data_attributes$migrateLiveState(this.getAttributes());

			this.attributes = attributeMap;
			LivingEntityKt.refreshAttributes(entity);

			// Commit the revision only after the rebuilt map is installed. If a
			// third-party attribute supplier throws while rebuilding, the entity
			// will retry on the next tick instead of becoming stuck on stale data.
			this.data_attributes$update_flag = updateFlag;
		}
	}
}
