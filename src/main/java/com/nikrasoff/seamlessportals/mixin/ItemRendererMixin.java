package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin{
    @Unique
    private static boolean isHpgEquipped = false;
    @Unique
    private static boolean isHpgJustEquipped = false;

    @Inject(method = "renderHeldItem(Lcom/badlogic/gdx/math/Vector3;Lfinalforeach/cosmicreach/items/Item;Lcom/badlogic/gdx/graphics/PerspectiveCamera;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/items/ItemRenderer;popUpHeldItem()V"))
    private static void detectHpg(Vector3 worldPosition, Item heldItem, PerspectiveCamera worldCamera, CallbackInfo ci){
        if (heldItem.getID().equals(HandheldPortalGen.hpgID)){
            isHpgEquipped = true;
            isHpgJustEquipped = true;
        }
        else{
            isHpgEquipped = false;
            isHpgJustEquipped = false;
        }
    }

    @Inject(method = "swingHeldItem", at = @At("HEAD"))
    static private void fireHpgAnim(CallbackInfo ci){
    }
}
