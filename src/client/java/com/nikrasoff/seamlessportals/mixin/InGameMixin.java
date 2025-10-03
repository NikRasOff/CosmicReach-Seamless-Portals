package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.SPClientConstants;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.ClientPortalEntityTools;
import com.nikrasoff.seamlessportals.extras.ClientPortalExtras;
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
import finalforeach.cosmicreach.singletons.GameSingletons;
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
        // This renders duplicates of entities as they're coming out the other side of the portal
        for (Entity e : getLocalPlayer().getZone().getAllEntities()){
            IPortalEntityRenderer r = SPClientConstants.getPortalEntityRenderer(e.getClass());
            if (r != null) {
                Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
                if (ClientPortalExtras.isEntityJustTeleportedPlayer(e) && GameSingletons.client().isFirstPerson()) {
                    r.advanceAnimations(e);
                    continue;
                }
                for (Portal portal : portals){
                    if (portal.linkedPortal == null) continue;
                    if (r.isCloseToPortal(e, portal)){
                        r.renderDuplicateSliced(e, renderFromCamera, portal);
                    }
                }
                r.advanceAnimations(e);
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void resetPlayerCamera(CallbackInfo ci){
        SeamlessPortalsRenderUtil.renderContext.end();
//        ((IPortalablePlayerController) playerController).cosmicReach_Seamless_Portals$resetPlayerCameraUp();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/entities/Entity;render(Lcom/badlogic/gdx/graphics/Camera;)V"))
    private void fuckUpEntityRendering(Entity instance, Camera worldCamera, Operation<Void> original){
        IPortalEntityRenderer r = SPClientConstants.getPortalEntityRenderer(instance.getClass());
        if (r == null) {
            original.call(instance, worldCamera);
            return;
        }
        if (getLocalPlayer() != null && getLocalPlayer().getEntity() == instance && GameSingletons.client().isFirstPerson() && !ClientPortalExtras.isPlayerCameraTeleported()){
            original.call(instance, worldCamera);
            return;
        }
        // This slices entities close to the portal to provide the illusion that they're partially through
        Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
        for (Portal portal : portals){
            if (r.isCloseToPortal(instance, portal)){
                r.renderSliced(instance, worldCamera, portal);
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
