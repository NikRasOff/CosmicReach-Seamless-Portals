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
    public transient Quaternion upVectorRotation = new Quaternion();
    @Unique
    public transient Vector3 upVectorOffset = new Vector3();

    @Unique
    public transient ISPAnimation cameraRotationAnimation;

    @Unique
    private transient boolean alreadyTPdCamera = false;

    @Unique
    private transient Vector3 preSavedCameraUp = new Vector3();

    @Inject(method = "updateCamera", at = @At("HEAD"))
    private void preUpdateCamera(PerspectiveCamera playerCamera, CallbackInfo ci){
        IPortalableEntity playerEntity = (IPortalableEntity) this.player.getEntity();
        if (playerEntity.isJustTeleported()){
            if (this.alreadyTPdCamera){
                return;
            }
            this.alreadyTPdCamera = true;
            this.lastCamPosition.set(playerEntity.getTeleportingPortal().getPortaledPos(this.lastCamPosition));
        }
        else{
            this.alreadyTPdCamera = false;
        }
    }

    @Inject(method = "updateCamera", at = @At("RETURN"))
    private void updateCameraForPortals(PerspectiveCamera playerCamera, CallbackInfo ci){
        this.preSavedCameraUp.set(playerCamera.up);
        Vector3 playerCameraOffset = this.player.getEntity().viewPositionOffset;
        Vector3 curPlayerPos = playerCamera.position.cpy().sub(playerCameraOffset);

        if (this.cameraRotationAnimation != null && !this.cameraRotationAnimation.isFinished()){
            this.cameraRotationAnimation.update(Gdx.graphics.getDeltaTime());
        }

        playerCameraOffset.mul(this.upVectorRotation);
        playerCameraOffset.add(this.upVectorOffset);
        playerCamera.up.mul(this.upVectorRotation);
        playerCamera.position.set(curPlayerPos.add(playerCameraOffset));

        IPortalableEntity portalableEntity = (IPortalableEntity) this.player.getEntity();

        Vector3 checkCamPos = playerCamera.position;
        Vector3 checkEntityPos = checkCamPos.cpy().sub(playerCameraOffset);

        if (portalableEntity.isJustTeleported()){
            checkEntityPos = this.player.getEntity().position.cpy().add(0, 0.05f, 0);
        }

        Ray ray = new Ray(checkEntityPos, checkCamPos.cpy().sub(checkEntityPos));
        for (Map.Entry<Integer, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
            Portal portal = portalEntry.getValue();
            if (portal.linkedPortal != null && !portal.isOnSameSideOfPortal(checkEntityPos, checkCamPos) && (Intersector.intersectRayOrientedBounds(ray, portal.getMeshBoundingBox(), new Vector3()))){
                playerCamera.position.set(portal.getPortaledPos(playerCamera.position));
                playerCamera.direction.set(portal.getPortaledVector(playerCamera.direction));
                playerCamera.up.set(portal.getPortaledVector(playerCamera.up));
                playerCamera.update();
                return;
            }
        }
    }

    @Override
    public void resetPlayerCameraUp(){
        playerCam.up.set(this.preSavedCameraUp);
    }

    @Override
    public void portalCurrentCameraTransform(Portal portal, Vector3 offset) {
        Matrix4 upVectorTransform = new Matrix4();
        upVectorTransform.set(this.upVectorOffset, this.upVectorRotation);
        upVectorTransform.set(portal.getPortaledTransform(upVectorTransform));
        upVectorTransform.translate(offset);
        upVectorTransform.inv();
        SPAnimationSequence newAnim = new SPAnimationSequence(true);
        newAnim.add(new QuaternionAnimation(upVectorTransform.getRotation(new Quaternion()), new Quaternion(), 0.5F, this.upVectorRotation));
        newAnim.add(new Vector3Animation(upVectorTransform.getTranslation(new Vector3()), new Vector3(), 0.5F, this.upVectorOffset));
        this.cameraRotationAnimation = newAnim;
    }
}
