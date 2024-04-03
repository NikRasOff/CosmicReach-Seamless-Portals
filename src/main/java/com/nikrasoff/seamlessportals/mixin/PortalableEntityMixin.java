package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.IPortalableEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PortalableEntityMixin implements IPortalableEntity {
    @Shadow public Vector3 position;

    @Shadow public Vector3 viewDirection;
    @Shadow private transient BoundingBox tmpEntityBoundingBox;
    @Shadow public Vector3 velocity;
    @Shadow public Vector3 onceVelocity;
    @Shadow private Vector3 acceleration;
    @Shadow public Vector3 viewPositionOffset;
    @Unique
    private transient final Array<Portal> nearbyPortals = new Array<>();
    @Unique
    private transient Portal cameraInterpolatePortal = null;
    @Unique
    private transient boolean justTeleported = false;
    @Unique
    private transient boolean ignorePortals = false;

    @Inject(method = "updatePositions", at = @At("HEAD"))
    private void onEntityUpdate(Zone zone, double deltaTime, CallbackInfo ci) {
        if (this.ignorePortals) return;
        if (this.cameraInterpolatePortal != null){
            this.cameraInterpolatePortal.isInterpProtectionActive = false;
            this.cameraInterpolatePortal = null;
        }
        this.justTeleported = false;
        this.nearbyPortals.clear();
        // A bit of a hack since entities don't keep track of the zones they're in
        // And since for now there's only one entity - the player
        // TODO: Fix when more entities/multiplayer gets added
        for (Portal curPortal : SeamlessPortals.portalManager.createdPortals) {
            if (InGame.getLocalPlayer().zoneId.equals(curPortal.zoneID) && curPortal.getGlobalBoundingBox().intersects(this.tmpEntityBoundingBox)) {
                this.nearbyPortals.add(curPortal);
            }
        }
        updateTrackedPortals(deltaTime);
    }

    @Unique
    private void updateTrackedPortals(double deltaTime){
        if (this.nearbyPortals.isEmpty()) return;

        Vector3 prevPos = this.position.cpy();
        float ax = this.acceleration.x * (float)deltaTime;
        float ay = this.acceleration.y * (float)deltaTime;
        float az = this.acceleration.z * (float)deltaTime;
        Vector3 testVelocity = this.velocity.cpy();
        testVelocity.add(ax, ay, az);
        testVelocity.add(this.onceVelocity);
        float vx = testVelocity.x * (float)deltaTime;
        float vy = testVelocity.y * (float)deltaTime;
        float vz = testVelocity.z * (float)deltaTime;
        Vector3 posDiff = new Vector3(vx, vy, vz);
        Vector3 targetPosition = (new Vector3(this.position)).add(posDiff);

        Vector3 prevCameraPos = prevPos.cpy().add(this.viewPositionOffset);
        Vector3 nextCameraPos = targetPosition.cpy().add(this.viewPositionOffset);

        for (Portal portal : this.nearbyPortals){
            if (portal.isPortalDestroyed || !portal.isPortalStable()) {
                continue;
            }
            if (this.isLocalPlayer()){
                if (!portal.isOnSameSideOfPortal(prevCameraPos, nextCameraPos)){
                    if (portal.isOnSameSideOfPortal(prevPos, prevCameraPos)){
                        this.cameraInterpolatePortal = portal.linkedPortal;
                        portal.linkedPortal.isInterpProtectionActive = true;
                    }
                    else {
                        this.cameraInterpolatePortal = portal;
                        portal.isInterpProtectionActive = true;
                    }
                }
            }
            if (!portal.isOnSameSideOfPortal(prevPos, targetPosition)){
                portal.linkedPortal.portalEndPosition = portal.getPortaledPos(targetPosition);
                this.teleportThroughPortal(portal);
                break;
            }
        }
    }

    @Unique
    public void teleportThroughPortal(Portal portal) {
        // TODO: Fix when more entities/multiplayer gets added
        InGame.getLocalPlayer().zoneId = new String(portal.linkedPortal.zoneID);
        this.viewDirection = portal.getPortaledVector(this.viewDirection);
        this.velocity.sub(portal.velocity);
        this.velocity.sub(portal.onceVelocity);
        this.position = portal.getPortaledPos(this.position);
        this.velocity = portal.getPortaledVector(this.velocity);
        this.onceVelocity = portal.getPortaledVector(this.onceVelocity);
        this.velocity.add(portal.linkedPortal.velocity);
        this.velocity.add(portal.linkedPortal.onceVelocity);
        if (!this.nearbyPortals.contains(portal.linkedPortal, true)){
            this.nearbyPortals.add(portal.linkedPortal);
        }
        this.justTeleported = true;
    }

    @Unique
    private boolean isLocalPlayer(){
        return ((IPortalableEntity) InGame.getLocalPlayer().getEntity() == this);
    }

    @Unique
    public Array<Portal> getNearbyPortals(){
        return this.nearbyPortals;
    }

    @Unique
    public boolean isJustTeleported(){
        return this.justTeleported;
    }

    @Unique
    public boolean hasCameraJustTeleported(Portal portal){
        return (this.cameraInterpolatePortal == portal);
    }

    public void setIgnorePortals(boolean value){
        this.ignorePortals = value;
    }
}
