package com.bibireden.data_attributes.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.api.item.ItemHelper;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
    private void data_init(ItemConvertible item, int count, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (item != null) {
            ((ItemHelper) item.asItem()).onStackCreated(stack, count);
        }
    }
}