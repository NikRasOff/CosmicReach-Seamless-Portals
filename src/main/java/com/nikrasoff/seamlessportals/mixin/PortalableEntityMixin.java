package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.Portal;
import com.nikrasoff.seamlessportals.PortalManager;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.entities.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PortalableEntityMixin {
    @Shadow public Vector3 position;

    @Shadow public Vector3 viewDirection;
    @Shadow private transient BoundingBox tmpEntityBoundingBox;
    @Shadow public Vector3 velocity;
    @Shadow public Vector3 onceVelocity;
    @Shadow private Vector3 acceleration;
    @Unique
    private transient final Array<Portal> nearbyPortals = new Array<>();
    @Unique
    private transient Portal teleportingPortal = null;
    @Unique
    private transient final boolean ignorePortals = false;

    @Inject(method = "updatePositions", at = @At("HEAD"))
    private void onEntityUpdate(World world, double deltaTime, CallbackInfo ci) {
        if (this.ignorePortals) return;
        if (this.teleportingPortal != null){
//            this.teleportingPortal.linkedPortal.thicknessMult = 1;
            this.teleportingPortal.isPortalBeingUsed = false;
            this.teleportingPortal = null;
        }
        this.nearbyPortals.clear();
        for (Portal curPortal : SeamlessPortals.portalManager.createdPortals) {
            if (this.tmpEntityBoundingBox.intersects(curPortal.getGlobalBoundingBox())) {
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

        for (Portal portal : this.nearbyPortals){
            if (portal.isPortalDestroyed) {
                continue;
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
        this.viewDirection = portal.getPortaledVector(this.viewDirection);
        this.position = portal.getPortaledPos(this.position);
        this.velocity = portal.getPortaledVector(this.velocity);
        this.onceVelocity = portal.getPortaledVector(this.onceVelocity);
        portal.linkedPortal.isPortalBeingUsed = true;
        this.teleportingPortal = portal.linkedPortal;
    }
}
