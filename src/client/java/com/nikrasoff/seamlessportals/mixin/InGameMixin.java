package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.SPClientConstants;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalIngame;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalablePlayerController;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.rendering.models.ObjItemModel;
import com.nikrasoff.seamlessportals.api.IPortalEntityRenderer;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(InGame.class)
public abstract class InGameMixin implements IPortalIngame {
    @Shadow public abstract Camera getWorldCamera();

    @Shadow
    static PlayerController playerController;

    @Shadow
    public static Player getLocalPlayer() {
        return null;
    }

    @Unique
    private float cosmicReach_Seamless_Portals$tempFovForPortals = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void startRenderContext(CallbackInfo ci){
        SeamlessPortalsRenderUtil.renderContext.begin();
        ObjItemModel.updateAnimations();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/utils/viewport/Viewport;apply()V"))
    private void storeTempFOV(CallbackInfo ci){
        this.cosmicReach_Seamless_Portals$tempFovForPortals = ((PerspectiveCamera)getWorldCamera()).fieldOfView;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/items/ItemRenderer;renderHeldItem(Lcom/badlogic/gdx/math/Vector3;Lfinalforeach/cosmicreach/items/ItemStack;Lcom/badlogic/gdx/graphics/PerspectiveCamera;)V"))
    private void seamlessPortalsCustomRender(CallbackInfo ci){
        Camera renderFromCamera = getWorldCamera();

        SeamlessPortals.effectManager.render(renderFromCamera);
        if (getLocalPlayer() == null) return;
        for (Entity e : getLocalPlayer().getZone().getAllEntities()){
            IPortalEntityRenderer r = SPClientConstants.getPortalEntityRenderer(e.getClass());
            if (r != null) {
                for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
                    Portal portal = portalEntry.getValue();
                    if (portal.linkedPortal == null) continue;
                    if (r.isCloseToPortal(e, portal)){
                        r.renderDuplicate(e, renderFromCamera, portal);
                    }
                }
                r.advanceAnimations(e);
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void resetPlayerCamera(CallbackInfo ci){
        SeamlessPortalsRenderUtil.renderContext.end();
        ((IPortalablePlayerController) playerController).cosmicReach_Seamless_Portals$resetPlayerCameraUp();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/entities/Entity;render(Lcom/badlogic/gdx/graphics/Camera;)V"))
    private void fuckUpEntityRendering(Entity instance, Camera worldCamera, Operation<Void> original){
        IPortalEntityRenderer r = SPClientConstants.getPortalEntityRenderer(instance.getClass());
        if (r == null) {
            original.call(instance, worldCamera);
            return;
        }
        if (getLocalPlayer() != null && getLocalPlayer().getEntity() == instance){
            original.call(instance, worldCamera);
            return;
        }
        for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
            Portal portal = portalEntry.getValue();
            if (r.isCloseToPortal(instance, portal)){
                r.renderSliced(instance, worldCamera, portal);
//                SeamlessPortals.LOGGER.info("Rendering duplicate now!");
                return;
            }
        }
        original.call(instance, worldCamera);
    }

    @Accessor(value = "playerController")
    public abstract PlayerController getPlayerController();

    @Override
    public float cosmicReach_Seamless_Portals$getTempFovForPortals() {
        return cosmicReach_Seamless_Portals$tempFovForPortals;
    }
}
