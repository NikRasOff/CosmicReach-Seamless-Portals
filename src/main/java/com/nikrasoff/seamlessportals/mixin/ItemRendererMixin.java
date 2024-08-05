package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemModel;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.ui.UI;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow
    static float swingTimer;

    @Shadow
    static float maxSwingTimer;

    @Shadow
    static ItemModel lastHeldItemModel;

    @Shadow @Final private static PerspectiveCamera heldItemCamera;

    @Shadow @Final private static Matrix4 tmpHeldMat4;

    @Unique
    private static boolean isHpgEquipped = false;

    @Inject(method = "renderHeldItem(Lfinalforeach/cosmicreach/items/Item;Lcom/badlogic/gdx/graphics/PerspectiveCamera;)V", at = @At("HEAD"), cancellable = true)
    static private void customHeldItemRender(Item heldItem, PerspectiveCamera worldCamera, CallbackInfo ci){
        if (UI.renderUI && heldItem != null && heldItem.getID().equals("seamlessportals:handheld_portal_generator")){
            Entity dummyEntity = HandheldPortalGen.dummyEntity;
            EntityModel model = HandheldPortalGen.hpgEntityModel;
            swingTimer = 0F;
            if (model != null) {
//                HandheldPortalGen.resetAnimationTimer();
                if (!isHpgEquipped) {
                    lastHeldItemModel = null;
                    isHpgEquipped = true;
                    HandheldPortalGen.setCurrentAnimation("equip");
                } else if (HandheldPortalGen.isAnimOver("equip", 1.25F) || HandheldPortalGen.isAnimOver("fire", 1F)) {
                    HandheldPortalGen.setCurrentAnimation("idle");
                }

                heldItemCamera.fieldOfView = 50.0F;
                heldItemCamera.viewportHeight = worldCamera.viewportHeight;
                heldItemCamera.viewportWidth = worldCamera.viewportWidth;
                heldItemCamera.near = worldCamera.near;
                heldItemCamera.far = worldCamera.far;
                heldItemCamera.update();
                tmpHeldMat4.idt();

                tmpHeldMat4.scale(0.5F, 0.5F, 0.5F);
                tmpHeldMat4.translate(0.4F, -0.55F, -1.75F);
                tmpHeldMat4.rotate(Vector3.Y, 175F);
                tmpHeldMat4.translate(-0.25F, -0.25F, -0.25F);

//                Gdx.gl.glDisable(2929);
                model.render(dummyEntity, heldItemCamera, tmpHeldMat4);
//                Gdx.gl.glEnable(2929);
            } else {
                swingTimer = 0.0F;
                lastHeldItemModel = null;
            }
            ci.cancel();
        }
        else {
            isHpgEquipped = false;
        }
    }
    @Inject(method = "swingHeldItem", at = @At("HEAD"))
    static private void fireHpgAnim(CallbackInfo ci){
        if (isHpgEquipped){
            HandheldPortalGen.setCurrentAnimation("fire");
        }
    }
}
