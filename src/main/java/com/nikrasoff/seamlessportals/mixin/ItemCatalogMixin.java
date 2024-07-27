package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemCatalog;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.SlotContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCatalog.class)
public abstract class ItemCatalogMixin extends SlotContainer {
    public ItemCatalogMixin(int numSlots) {
        super(numSlots);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addCustomItems(int numSlots, CallbackInfo ci){
//        this.addItemStack(new ItemStack(Item.getItem("seamlessportals:handheld_portal_generator"), 1));
    }
}
