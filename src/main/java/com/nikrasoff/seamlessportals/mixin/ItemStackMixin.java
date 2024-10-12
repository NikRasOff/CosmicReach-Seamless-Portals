package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.extras.interfaces.CustomPropertyItem;
import com.nikrasoff.seamlessportals.extras.interfaces.IModItemStack;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IModItemStack {
    @Shadow public abstract Item getItem();

    @Unique
    public Map<String, Object> cosmicReach_Seamless_Portals$customProperties = new HashMap<>();

    @Override
    public Map<String, Object> getCustomProperties(){
        return cosmicReach_Seamless_Portals$customProperties;
    }

    @Inject(method = "read", at = @At("TAIL"))
    void readCustomProperties(CRBinDeserializer crbd, CallbackInfo ci){
        if (this.getItem() instanceof CustomPropertyItem cpi){
            cpi.readCustomProperties(crbd, cosmicReach_Seamless_Portals$customProperties);
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    void writeCustomProperties(CRBinSerializer crbs, CallbackInfo ci){
        if (this.getItem() instanceof CustomPropertyItem cpi){
            cpi.writeCustomProperties(crbs, cosmicReach_Seamless_Portals$customProperties);
        }
    }
}
