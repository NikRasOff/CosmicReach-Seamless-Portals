package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Array;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.extras.IPortalableEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Chunk;
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
    @Shadow public BoundingBox localBoundingBox;
    @Shadow public boolean isOnGround;
    @Unique
    private transient final Array<Portal> nearbyPortals = new Array<>();
    @Unique
    private transient Portal cameraInterpolatePortal = null;
    @Unique
    private transient boolean justTeleported = false;
    @Unique
    private transient boolean ignorePortals = false;

    @Unique
    private transient Array<BlockPosition> nonCollideBlocks = new Array<>();
    @Unique
    private transient Array<BlockPosition> tmpCollidedBlocks = new Array<>();
    @Unique
    private transient OrientedBoundingBox tmpPortaledBoundingBox = new OrientedBoundingBox();
    @Unique
    private transient BoundingBox tmpPortalCheckBlockBoundingBox = new BoundingBox();

    @Inject(method = "updatePositions", at = @At("HEAD"))
    private void onEntityUpdate(Zone zone, double deltaTime, CallbackInfo ci) {
        if (this.ignorePortals) return;
        if (this.cameraInterpolatePortal != null){
            this.cameraInterpolatePortal.isInterpProtectionActive = false;
            this.cameraInterpolatePortal = null;
        }
        this.nonCollideBlocks.clear();
        this.nonCollideBlocks.addAll(this.tmpCollidedBlocks);
        this.tmpCollidedBlocks.clear();
        this.tmpPortaledBoundingBox.setBounds(this.localBoundingBox);
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
                        portal.linkedPortal.portalEndPosition = portal.getPortaledPos(nextCameraPos);
                    }
                    else {
                        this.cameraInterpolatePortal = portal;
                        portal.isInterpProtectionActive = true;
                        portal.portalEndPosition = nextCameraPos;
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

    @WrapOperation(method = "updateConstraints", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Zone;getBlockState(III)Lfinalforeach/cosmicreach/blocks/BlockState;"))
    private BlockState updateConstraintsMixin(Zone instance, int x, int y, int z, Operation<BlockState> original){
        return this.checkIfShouldCollidePortal(instance, x, y, z, original);
    }

    public BlockState checkIfShouldCollidePortal(Zone instance, int x, int y, int z, Operation<BlockState> original){
        Chunk c = instance.getChunkAtBlock(x, y, z);
        BlockPosition curBlockPos = new BlockPosition(c, x - c.blockX, y - c.blockY, z - c.blockZ);
        if (this.nonCollideBlocks.contains(curBlockPos, false)){
            if (!this.tmpCollidedBlocks.contains(curBlockPos, false)){
                this.tmpCollidedBlocks.add(curBlockPos);
            }
            return null;
        }
        BlockState orBlockState = original.call(instance, x, y, z);
        if (this.isJustTeleported() && orBlockState != null && !orBlockState.walkThrough){
            orBlockState.getBoundingBox(this.tmpPortalCheckBlockBoundingBox, x, y, z);
            if (!this.tmpPortaledBoundingBox.intersects(this.tmpPortalCheckBlockBoundingBox) && this.tmpEntityBoundingBox.intersects(this.tmpPortalCheckBlockBoundingBox)){
                this.nonCollideBlocks.add(curBlockPos);
                this.tmpCollidedBlocks.add(curBlockPos);
                return null;
            }
        }
        return orBlockState;
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
        if (this.isLocalPlayer()){
            portal.linkedPortal.updatePortalMeshScale((PerspectiveCamera) GameState.IN_GAME.getWorldCamera());
        }
        if (!this.nearbyPortals.contains(portal.linkedPortal, true)){
            this.nearbyPortals.add(portal.linkedPortal);
        }
        Vector3 orPos = portal.linkedPortal.getPortaledPos(this.position);
        if (this.isOnGround){
            this.tmpPortaledBoundingBox.setTransform(new Matrix4().setToLookAt(orPos, orPos.cpy().add(portal.linkedPortal.getPortaledVector(this.viewDirection)), portal.linkedPortal.getPortaledVector(new Vector3(0, 1, 0))).inv());
            float lowestPoint = 0;
            for (Vector3 v : this.tmpPortaledBoundingBox.getVertices()){
                if (v.y - orPos.y < lowestPoint) lowestPoint = v.y - orPos.y;
            }
            this.position.add(portal.getPortaledVector(new Vector3(0, -lowestPoint, 0)));
        }

        this.justTeleported = true;
        orPos.set(this.position).add(portal.getPortaledPos(new Vector3(0, 0.05F, 0)));
        this.tmpPortaledBoundingBox.setTransform(new Matrix4().setToLookAt(orPos, orPos.cpy().add(portal.linkedPortal.getPortaledVector(this.viewDirection)), portal.linkedPortal.getPortaledVector(new Vector3(0, 1, 0))).inv());
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
