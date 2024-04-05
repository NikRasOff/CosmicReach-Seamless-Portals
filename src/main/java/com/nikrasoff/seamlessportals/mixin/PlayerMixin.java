package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.extras.IPortalableEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.Player;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow private Entity controlledEntity;

    @Inject(method = "updateCamera", at = @At("RETURN"))
    private void updateCameraForPortals(Camera playerCamera, float partTick, CallbackInfo ci){
        IPortalableEntity portalableEntity = (IPortalableEntity) this.controlledEntity;
        Vector3 checkCamPos = playerCamera.position;
        Vector3 checkEntityPos = checkCamPos.cpy().sub(this.controlledEntity.viewPositionOffset);
        if (portalableEntity.isJustTeleported()){
            checkEntityPos = this.controlledEntity.position;
            checkCamPos = checkEntityPos.cpy().add(this.controlledEntity.viewPositionOffset);
        }
        Ray ray = new Ray(checkEntityPos, checkCamPos.cpy().sub(checkEntityPos));
        for (Portal portal : portalableEntity.getNearbyPortals()){
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
}
