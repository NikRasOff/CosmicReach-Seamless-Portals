package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.items.UnstableHandheldPortalGen;
import com.nikrasoff.seamlessportals.rendering.models.ObjItemModel;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin{
    @Shadow
    public static <T extends Item> ItemModel getModel(T item, boolean createIfNull) {
        return null;
    }

    @Unique
    private static boolean cosmicReach_Seamless_Portals$isHpgEquipped = false;
    @Unique
    private static boolean cosmicReach_Seamless_Portals$isUnstableHpgEquipped = false;

    @Inject(method = "renderHeldItem(Lcom/badlogic/gdx/math/Vector3;Lfinalforeach/cosmicreach/items/Item;Lcom/badlogic/gdx/graphics/PerspectiveCamera;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/items/ItemRenderer;popUpHeldItem()V"))
    private static void detectHpg(Vector3 worldPosition, Item heldItem, PerspectiveCamera worldCamera, CallbackInfo ci){
        if (heldItem.getID().equals(HandheldPortalGen.hpgID)){
            cosmicReach_Seamless_Portals$isHpgEquipped = true;
            cosmicReach_Seamless_Portals$isUnstableHpgEquipped = false;
        } else if (heldItem.getID().equals(UnstableHandheldPortalGen.hpgID)) {
            cosmicReach_Seamless_Portals$isHpgEquipped = false;
            cosmicReach_Seamless_Portals$isUnstableHpgEquipped = true;
        } else{
            cosmicReach_Seamless_Portals$isHpgEquipped = false;
            cosmicReach_Seamless_Portals$isUnstableHpgEquipped = false;
        }
    }

    @Inject(method = "swingHeldItem", at = @At("HEAD"))
    static private void fireHpgAnim(CallbackInfo ci){
        if (cosmicReach_Seamless_Portals$isHpgEquipped){
            ObjItemModel hpgModel = (ObjItemModel) getModel(Item.getItem(HandheldPortalGen.hpgID), false);
            if (hpgModel == null) return;

            hpgModel.setViewAnimation("armature|anim_fire", 1);
            hpgModel.queueViewAnimation("armature|anim_idle", -1);
        }
        if (cosmicReach_Seamless_Portals$isUnstableHpgEquipped){
            ObjItemModel hpgModel = (ObjItemModel) getModel(Item.getItem(UnstableHandheldPortalGen.hpgID), false);
            if (hpgModel == null) return;

            hpgModel.setViewAnimation("armature|anim_fire", 1);
            hpgModel.queueViewAnimation("armature|anim_idle", -1);
        }
    }
}
