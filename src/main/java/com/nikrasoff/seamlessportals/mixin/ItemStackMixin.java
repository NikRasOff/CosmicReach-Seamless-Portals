package com.nikrasoff.seamlessportals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.items.SyncedOmniumCrystal;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @WrapOperation(method = "getName", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/items/Item;getName()Ljava/lang/String;"))
    public String specialGetName(Item instance, Operation<String> original){
        if (instance instanceof SyncedOmniumCrystal s){
            return s.getName((ItemStack) (Object) this);
        }
        return original.call(instance);
    }
}
