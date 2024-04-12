package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.animations.ISPAnimation;
import com.nikrasoff.seamlessportals.animations.Matrix4Animation;
import com.nikrasoff.seamlessportals.animations.SPAnimationSequence;
import com.nikrasoff.seamlessportals.extras.IPortalableEntity;
import com.nikrasoff.seamlessportals.extras.IPortalablePlayer;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.Player;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements IPortalablePlayer {
    @Shadow private Entity controlledEntity;

    @Unique
    public transient Matrix4 upVectorTransform = new Matrix4();

    @Unique
    public transient ISPAnimation cameraRotationAnimation;

    @Inject(method = "updateCamera", at = @At("RETURN"))
    private void updateCameraForPortals(Camera playerCamera, float partTick, CallbackInfo ci){
        Vector3 playerCameraOffset = this.controlledEntity.viewPositionOffset;
        Vector3 curPlayerPos = playerCamera.position.cpy().sub(playerCameraOffset);

        if (this.cameraRotationAnimation != null && !this.cameraRotationAnimation.isFinished()){
            this.cameraRotationAnimation.update(Gdx.graphics.getDeltaTime());
        }

        playerCameraOffset.mul(this.upVectorTransform).sub(new Vector3().mul(this.upVectorTransform));
        playerCamera.up.mul(this.upVectorTransform).sub(new Vector3().mul(this.upVectorTransform));
        playerCamera.position.set(curPlayerPos.add(playerCameraOffset));

        IPortalableEntity portalableEntity = (IPortalableEntity) this.controlledEntity;

        Vector3 checkCamPos = playerCamera.position;
        Vector3 checkEntityPos = checkCamPos.cpy().sub(playerCameraOffset);

        if (portalableEntity.isJustTeleported()){
            checkEntityPos = controlledEntity.position;
        }

        Ray ray = new Ray(checkEntityPos, checkCamPos.cpy().sub(checkEntityPos));
        for (Portal portal : SeamlessPortals.portalManager.createdPortals){
            if (!portal.isOnSameSideOfPortal(checkEntityPos, checkCamPos) && (Intersector.intersectRayOrientedBounds(ray, portal.getMeshBoundingBox(), new Vector3()))){
                playerCamera.position.set(portal.getPortaledPos(playerCamera.position));
                playerCamera.direction.set(portal.getPortaledVector(playerCamera.direction));
                playerCamera.up.set(portal.getPortaledVector(playerCamera.up));
                playerCamera.update();
                return;
            }
        }
    }

    @WrapOperation(method = "proneCheck", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Zone;getBlockState(III)Lfinalforeach/cosmicreach/blocks/BlockState;"))
    private BlockState proneCheckMixin(Zone instance, int x, int y, int z, Operation<BlockState> original){
        IPortalableEntity portalableEntity = (IPortalableEntity) this.controlledEntity;
        return portalableEntity.checkIfShouldCollidePortal(instance, x, y, z, original);
    }

    @Override
    public void portalCurrentCameraTransform(Portal portal, Vector3 offset) {
        this.upVectorTransform.set(portal.getPortaledTransform(this.upVectorTransform));
        this.upVectorTransform.translate(offset);
        this.upVectorTransform.inv();
        Matrix4Animation newAnim = new Matrix4Animation(this.upVectorTransform.cpy(), new Matrix4(), 0.5F, this.upVectorTransform);
        this.cameraRotationAnimation = newAnim;
    }
}
