package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.animations.*;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalablePlayerController;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.PlayerController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PlayerController.class)
public abstract class PlayerControllerMixin implements IPortalablePlayerController {
    @Shadow Player player;

    @Shadow
    Vector3 lastCamPosition;
    @Shadow
    Camera playerCam;
    @Unique
    public transient Quaternion cosmicReach_Seamless_Portals$upVectorRotation = new Quaternion();
    @Unique
    public transient Vector3 cosmicReach_Seamless_Portals$upVectorOffset = new Vector3();

    @Unique
    public transient ISPAnimation cosmicReach_Seamless_Portals$cameraRotationAnimation;

    @Unique
    private transient boolean cosmicReach_Seamless_Portals$alreadyTPdCamera = false;

    @Unique
    private transient Vector3 cosmicReach_Seamless_Portals$preSavedCameraUp = new Vector3();

    @Inject(method = "updateCamera", at = @At("HEAD"))
    private void preUpdateCamera(PerspectiveCamera playerCamera, CallbackInfo ci){
        IPortalableEntity playerEntity = (IPortalableEntity) this.player.getEntity();
        if (playerEntity.cosmicReach_Seamless_Portals$isJustTeleported()){
            if (this.cosmicReach_Seamless_Portals$alreadyTPdCamera){
                return;
            }
            this.cosmicReach_Seamless_Portals$alreadyTPdCamera = true;
            this.lastCamPosition.set(playerEntity.cosmicReach_Seamless_Portals$getTeleportingPortal().getPortaledPos(this.lastCamPosition));
        }
        else{
            this.cosmicReach_Seamless_Portals$alreadyTPdCamera = false;
        }
    }

    @Inject(method = "updateCamera", at = @At("RETURN"))
    private void updateCameraForPortals(PerspectiveCamera playerCamera, CallbackInfo ci){
        this.cosmicReach_Seamless_Portals$preSavedCameraUp.set(playerCamera.up);
        Vector3 playerCameraOffset = this.player.getEntity().viewPositionOffset;
        Vector3 curPlayerPos = playerCamera.position.cpy().sub(playerCameraOffset);

        if (this.cosmicReach_Seamless_Portals$cameraRotationAnimation != null && !this.cosmicReach_Seamless_Portals$cameraRotationAnimation.isFinished()){
            this.cosmicReach_Seamless_Portals$cameraRotationAnimation.update(Gdx.graphics.getDeltaTime());
        }

        playerCameraOffset.mul(this.cosmicReach_Seamless_Portals$upVectorRotation);
        playerCameraOffset.add(this.cosmicReach_Seamless_Portals$upVectorOffset);
        playerCamera.up.mul(this.cosmicReach_Seamless_Portals$upVectorRotation);
        playerCamera.position.set(curPlayerPos.add(playerCameraOffset));

        IPortalableEntity portalableEntity = (IPortalableEntity) this.player.getEntity();

        Vector3 checkCamPos = playerCamera.position;
        Vector3 checkEntityPos = checkCamPos.cpy().sub(playerCameraOffset);

        if (portalableEntity.cosmicReach_Seamless_Portals$isJustTeleported()){
            checkEntityPos = this.player.getEntity().position.cpy().add(0, 0.05f, 0);
        }

        Ray ray = new Ray(checkEntityPos, checkCamPos.cpy().sub(checkEntityPos));
        for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
            Portal portal = portalEntry.getValue();
            if (portal.linkedPortal != null && portal.isNotOnSameSideOfPortal(checkEntityPos, checkCamPos) && (Intersector.intersectRayOrientedBounds(ray, portal.getMeshBoundingBox(), new Vector3()))){
                playerCamera.position.set(portal.getPortaledPos(playerCamera.position));
                playerCamera.direction.set(portal.getPortaledVector(playerCamera.direction));
                playerCamera.up.set(portal.getPortaledVector(playerCamera.up));
                playerCamera.update();
                return;
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$resetPlayerCameraUp(){
        playerCam.up.set(this.cosmicReach_Seamless_Portals$preSavedCameraUp);
    }

    @Override
    public void cosmicReach_Seamless_Portals$portalCurrentCameraTransform(Portal portal, Vector3 offset) {
        Matrix4 upVectorTransform = new Matrix4();
        upVectorTransform.set(this.cosmicReach_Seamless_Portals$upVectorOffset, this.cosmicReach_Seamless_Portals$upVectorRotation);
        upVectorTransform.set(portal.getPortaledTransform(upVectorTransform));
        upVectorTransform.translate(offset);
        upVectorTransform.inv();
        SPAnimationSequence newAnim = new SPAnimationSequence(true);
        newAnim.add(new QuaternionAnimation(upVectorTransform.getRotation(new Quaternion()), new Quaternion(), 0.5F, this.cosmicReach_Seamless_Portals$upVectorRotation));
        newAnim.add(new Vector3Animation(upVectorTransform.getTranslation(new Vector3()), new Vector3(), 0.5F, this.cosmicReach_Seamless_Portals$upVectorOffset));
        this.cosmicReach_Seamless_Portals$cameraRotationAnimation = newAnim;
    }
}
