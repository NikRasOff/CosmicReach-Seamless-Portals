package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.extras.*;
import com.nikrasoff.seamlessportals.extras.interfaces.*;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Entity.class)
public abstract class PortalableEntityMixin implements IPortalableEntity{
    @Unique
    private static Vector3 cosmicReach_Seamless_Portals$portalPosCheckEpsilon = new Vector3(0f, 0.05f, 0f);

    @Shadow public Vector3 position;
    @Shadow public Vector3 viewDirection;
    @Shadow private transient BoundingBox tmpEntityBoundingBox;
    @Shadow public Vector3 velocity;
    @Shadow public Vector3 onceVelocity;
    @Shadow private Vector3 acceleration;
    @Shadow public BoundingBox localBoundingBox;
    @Shadow
    public transient BoundingBox globalBoundingBox;

    @Shadow public abstract void setPosition(Vector3 position);

    @Shadow public IEntityModelInstance modelInstance;

    @Shadow private transient float pendingDamage;
    @Shadow public transient Zone zone;
    @Shadow public boolean noClip;
    @Shadow private transient float floorFriction;

    @Shadow public abstract void getBoundingBox(BoundingBox boundingBox);
    @Unique
    private transient boolean cosmicReach_Seamless_Portals$justTeleported = false;
    @Unique
    private transient Portal cosmicReach_Seamless_Portals$teleportPortal;
    @Unique
    private transient boolean cosmicReach_Seamless_Portals$ignorePortals = false;
    @Unique
    public transient Array<Portal> cosmicReach_Seamless_Portals$nearbyPortals = new Array<>();
    @Unique
    private static final float cosmicReach_Seamless_Portals$terminalVelocity = 100; // This value was chosen randomly

    @Unique
    private transient Array<BlockPosition> cosmicReach_Seamless_Portals$tmpNonCollideBlocks = new Array<>();
    @Unique
    private transient Array<BlockPosition> cosmicReach_Seamless_Portals$tmpCollidedBlocks = new Array<>();
    @Unique
    private transient OrientedBoundingBox cosmicReach_Seamless_Portals$tmpPortaledBoundingBox = new OrientedBoundingBox();
    @Unique
    private transient BoundingBox cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox = new BoundingBox();
    @Unique
    private transient Matrix4 cosmicReach_Seamless_Portals$tmpPortalTransformMatrix = new Matrix4();
    @Unique
    private transient Vector3 cosmicReach_Seamless_Portals$tmpPortalNextPosition = new Vector3();

    @Inject(method = "updatePositions", at = @At("HEAD"))
    private void onEntityUpdate(Zone zone, double deltaTime, CallbackInfo ci) {
        if (this.velocity.cpy().len() > cosmicReach_Seamless_Portals$terminalVelocity){
            this.velocity.clamp(0, cosmicReach_Seamless_Portals$terminalVelocity);
        }
        if (this.cosmicReach_Seamless_Portals$ignorePortals) return;
        this.cosmicReach_Seamless_Portals$tmpNonCollideBlocks.clear();
        this.cosmicReach_Seamless_Portals$tmpNonCollideBlocks.addAll(this.cosmicReach_Seamless_Portals$tmpCollidedBlocks);
        this.cosmicReach_Seamless_Portals$tmpCollidedBlocks.clear();
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.setBounds(this.localBoundingBox);
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.getBounds().min.add(new Vector3(-0.01F, -0.01F, -0.01F));
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.getBounds().max.add(new Vector3(0.01F, 0.01F, 0.01F));
        this.cosmicReach_Seamless_Portals$justTeleported = false;
        this.cosmicReach_Seamless_Portals$teleportPortal = null;
        this.cosmicReach_Seamless_Portals$nearbyPortals.clear();
        cosmicReach_Seamless_Portals$updateTrackedPortals(deltaTime, zone);
    }

    @Unique
    private void cosmicReach_Seamless_Portals$applyFriction(Vector3 vel, float coefficient){
        coefficient *= 0.999997F;
        float f;
        if (coefficient >= 1.0F) {
            f = (float)Math.pow(Math.exp(-coefficient * 0.05F), 20.0);
        } else {
            f = (float)Math.pow(1.0F - coefficient, 0.05000000074505806);
        }

        vel.x *= f;
        vel.z *= f;
        if (Math.abs(vel.x) < 1.0E-4F) {
            vel.x = 0.0F;
        }

        if (Math.abs(vel.z) < 1.0E-4F) {
            vel.z = 0.0F;
        }
    }

    @Unique
    private void cosmicReach_Seamless_Portals$updateTrackedPortals(double deltaTime, Zone zone){
        if (SeamlessPortals.portalManager.createdPortals.isEmpty()) return;

        Vector3 prevPos = this.position.cpy();
        float ax = this.acceleration.x * (float)deltaTime;
        float ay = this.acceleration.y * (float)deltaTime;
        float az = this.acceleration.z * (float)deltaTime;
        Vector3 testVelocity = this.velocity.cpy();
        testVelocity.add(ax, ay, az);
        testVelocity.add(this.onceVelocity);

        if (this.noClip) {
            this.cosmicReach_Seamless_Portals$applyFriction(testVelocity, 1.0F);
        } else {
            this.cosmicReach_Seamless_Portals$applyFriction(testVelocity, this.floorFriction);
        }

        float vx = testVelocity.x * (float)deltaTime;
        float vy = testVelocity.y * (float)deltaTime;
        float vz = testVelocity.z * (float)deltaTime;
        Vector3 posDiff = new Vector3(vx, vy, vz);
        Vector3 targetPosition = (new Vector3(this.position)).add(posDiff);
        this.cosmicReach_Seamless_Portals$tmpPortalNextPosition.set(targetPosition);

        Ray posChange = new Ray(prevPos.cpy().add(cosmicReach_Seamless_Portals$portalPosCheckEpsilon), targetPosition.cpy().add(cosmicReach_Seamless_Portals$portalPosCheckEpsilon).sub(prevPos));

        this.getBoundingBox(this.globalBoundingBox);
        for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
            Portal portal = portalEntry.getValue();
            if (portal.isPortalDestroyed) {
                continue;
            }
            if (portal.zone != zone) continue;
            portal.getBoundingBox(cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox);
            if (this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox.intersects(this.globalBoundingBox)) {
                this.cosmicReach_Seamless_Portals$nearbyPortals.add(portal);
            }
            if (portal.isNotOnSameSideOfPortal(prevPos.cpy().add(cosmicReach_Seamless_Portals$portalPosCheckEpsilon), targetPosition.cpy().add(cosmicReach_Seamless_Portals$portalPosCheckEpsilon)) && Intersector.intersectRayOrientedBounds(posChange, portal.getMeshBoundingBox(), new Vector3())){
                if (portal.linkedPortal == null){
                    this.pendingDamage += 1000000;
                    break;
                }
                this.cosmicReach_Seamless_Portals$teleportThroughPortal(portal, zone);
                break;
            }
        }
    }

    @WrapOperation(method = "updateConstraints", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Zone;getBlockState(III)Lfinalforeach/cosmicreach/blocks/BlockState;"))
    private BlockState updateConstraintsMixin(Zone instance, int x, int y, int z, Operation<BlockState> original){
        return this.cosmicReach_Seamless_Portals$checkIfShouldCollidePortal(instance, x, y, z, original);
    }

    public BlockState cosmicReach_Seamless_Portals$checkIfShouldCollidePortal(Zone instance, int x, int y, int z, Operation<BlockState> original){
        // Refer to the comment in teleportThroughPortal for an explanation
        BlockState orBlockState = original.call(instance, x, y, z);
        if (this.cosmicReach_Seamless_Portals$ignorePortals){
            return orBlockState;
        }

        Chunk c = instance.getChunkAtBlock(x, y, z);
        if (c == null) return orBlockState;
        BlockPosition curBlockPos = new BlockPosition(c, x - c.blockX, y - c.blockY, z - c.blockZ);
        if (this.cosmicReach_Seamless_Portals$tmpNonCollideBlocks.contains(curBlockPos, false)){
            if (!this.cosmicReach_Seamless_Portals$tmpCollidedBlocks.contains(curBlockPos, false)){
                this.cosmicReach_Seamless_Portals$tmpCollidedBlocks.add(curBlockPos);
            }
            return null;
        }

        if (orBlockState != null && !orBlockState.walkThrough){
            orBlockState.getBoundingBox(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox, x, y, z);
            Vector3 checkCenter = new Vector3();
            this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox.getCenter(checkCenter);
            Vector3 portalCollisionCheckPos = this.cosmicReach_Seamless_Portals$isJustTeleported() ? this.cosmicReach_Seamless_Portals$tmpPortalNextPosition : this.position.cpy();
            Ray ray = new Ray(portalCollisionCheckPos, checkCenter.cpy().sub(portalCollisionCheckPos));
            for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
                Portal portal = portalEntry.getValue();
                if (portal.zone == zone && portal.isNotOnSameSideOfPortal(portalCollisionCheckPos, checkCenter) && Intersector.intersectRayOrientedBounds(ray, portal.getMeshBoundingBox(), new Vector3())){
                    if (!portal.getMeshBoundingBox().intersects(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox)){
                        return null;
                    }
                }
            }

            if (this.cosmicReach_Seamless_Portals$isJustTeleported()){
                if (!this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.intersects(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox) && this.tmpEntityBoundingBox.intersects(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox)){
                    this.cosmicReach_Seamless_Portals$tmpNonCollideBlocks.add(curBlockPos);
                    this.cosmicReach_Seamless_Portals$tmpCollidedBlocks.add(curBlockPos);
                    return null;
                }
            }
        }
        return orBlockState;
    }

    @Unique
    public void cosmicReach_Seamless_Portals$teleportThroughPortal(Portal portal, Zone zone) {
        if (this.cosmicReach_Seamless_Portals$isPlayer()){
            Player pl = ((PlayerEntity) (Object) this).player;
            pl.setZone(portal.linkedPortal.zone.zoneId);
        }
        if (portal.zone != portal.linkedPortal.zone){
            GameSingletons.world.getZoneIfExists(portal.zone.zoneId).removeEntity((Entity) (Object) this);
            GameSingletons.world.getZoneIfExists(portal.linkedPortal.zone.zoneId).addEntity((Entity) (Object) this);
        }
        this.cosmicReach_Seamless_Portals$tmpPortalNextPosition.set(portal.getPortaledPos(this.cosmicReach_Seamless_Portals$tmpPortalNextPosition));
        this.viewDirection = portal.getPortaledVector(this.viewDirection);
        this.velocity.sub(portal.velocity);
        this.velocity.sub(portal.onceVelocity);
        this.setPosition(portal.getPortaledPos(this.position));
        this.velocity = portal.getPortaledVector(this.velocity);
        this.onceVelocity = portal.getPortaledVector(this.onceVelocity);
        this.acceleration.set(portal.getPortaledVector(this.acceleration));
        this.velocity.add(portal.linkedPortal.velocity);
        this.velocity.add(portal.linkedPortal.onceVelocity);
        Vector3 originalPos = this.position.cpy();

        // A bunch of magic to make mismatched portals more intuitive
        // to the player and less intuitive to any poor soul
        // who happens to be looking through this code

        // sorry not sorry
        this.cosmicReach_Seamless_Portals$snapOnGoThroughPortal(portal, zone);

        // Animating camera turning
        if (GameSingletons.isClient && this.cosmicReach_Seamless_Portals$isLocalPlayer()){
            SeamlessPortals.clientConstants.animateCameraTurning(originalPos, this.position, portal);
        }

        this.cosmicReach_Seamless_Portals$justTeleported = true;
        this.cosmicReach_Seamless_Portals$teleportPortal = portal;
        Vector3 orPos = new Vector3(this.position);
        this.cosmicReach_Seamless_Portals$tmpPortalTransformMatrix.setToLookAt(orPos, orPos.cpy().add(portal.linkedPortal.getPortaledVector(new Vector3(0, 0, 1))), portal.linkedPortal.getPortaledVector(new Vector3(0, 1, 0))).inv();
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.setTransform(this.cosmicReach_Seamless_Portals$tmpPortalTransformMatrix);
    }

    @Unique
    private void cosmicReach_Seamless_Portals$snapOnGoThroughPortal(Portal portal, Zone zone){
        // Making this is pure suffering
        // Why is collision with blocks so hard to do?
        // Isn't it, like, THE thing this game should be good at?

        // First, figure out the direction we should be checking
        DirectionVector direction = DirectionVector.getClosestDirection(portal.getPortaledVector(DirectionVector.POS_Y.getVector()));

        // Now, we get the new bounding box
        this.tmpEntityBoundingBox.set(this.localBoundingBox);
        this.tmpEntityBoundingBox.min.add(this.position);
        this.tmpEntityBoundingBox.max.add(this.position);
        this.tmpEntityBoundingBox.update();

        // Now, get the range of blocks to check against
        Vector3 minPoint = new Vector3();
        Vector3 maxPoint = new Vector3();

        switch (direction.getName()){
            case "negZ":
                this.tmpEntityBoundingBox.getCorner001(minPoint);
                this.tmpEntityBoundingBox.getCorner111(maxPoint);
                break;
            case "posX":
                this.tmpEntityBoundingBox.getCorner000(minPoint);
                this.tmpEntityBoundingBox.getCorner011(maxPoint);
                break;
            case "negX":
                this.tmpEntityBoundingBox.getCorner100(minPoint);
                this.tmpEntityBoundingBox.getCorner111(maxPoint);
                break;
            case "posY":
                this.tmpEntityBoundingBox.getCorner000(minPoint);
                this.tmpEntityBoundingBox.getCorner101(maxPoint);
                break;
            case "negY":
                this.tmpEntityBoundingBox.getCorner010(minPoint);
                this.tmpEntityBoundingBox.getCorner111(maxPoint);
                break;
            default:
                this.tmpEntityBoundingBox.getCorner000(minPoint);
                this.tmpEntityBoundingBox.getCorner110(maxPoint);
        }

        int minbx = (int) Math.floor(minPoint.x);
        int minby = (int) Math.floor(minPoint.y);
        int minbz = (int) Math.floor(minPoint.z);
        int maxbx = (int) Math.floor(maxPoint.x);
        int maxby = (int) Math.floor(maxPoint.y);
        int maxbz = (int) Math.floor(maxPoint.z);

        // And now for the actual checking collisions part

        float highestPoint = 0;
        for (int bx = minbx; bx <= maxbx; ++bx){
            for (int by = minby; by <= maxby; ++by){
                for (int bz = minbz; bz <= maxbz; ++bz){
                    BlockState checkBlock = zone.getBlockState(bx, by, bz);
                    if (checkBlock != null && !checkBlock.walkThrough){
                        checkBlock.getBoundingBox(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox, bx, by, bz);
                        // Figure out if the block can just be discarded (to prevent some weirdness)
                        float checkPoint;
                        switch (direction.getName()){
                            case "negZ", "negX", "negY" -> checkPoint = portal.linkedPortal.getPortaledPos(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox.min).y;
                            default -> checkPoint = portal.linkedPortal.getPortaledPos(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox.max).y;
                        }
                        if (checkPoint > portal.linkedPortal.getPortaledPos(this.position).y + 0.01) continue;

                        if (this.tmpEntityBoundingBox.intersects(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox)){
                            // Figure out how high the player should be snapped
                            float curPoint;
                            Vector3 blockOffsetMin = this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox.max.cpy().sub(this.tmpEntityBoundingBox.min);
                            Vector3 blockOffsetMax = this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox.min.cpy().sub(this.tmpEntityBoundingBox.max).scl(-1);
                            switch (direction.getName()){
                                case "negZ" -> curPoint = blockOffsetMax.z;
                                case "posX" -> curPoint = blockOffsetMin.x;
                                case "negX" -> curPoint = blockOffsetMax.x;
                                case "posY" -> curPoint = blockOffsetMin.y;
                                case "negY" -> curPoint = blockOffsetMax.y;
                                default -> curPoint = blockOffsetMin.z;
                            }
                            highestPoint = Math.max(highestPoint, curPoint);
                        }
                    }
                }
            }
        }
        if (highestPoint > 0){
            highestPoint += 0.01F;
        }
        // finally, snap the player in the chosen direction by the chosen amount
        Vector3 bump = direction.getVector().cpy().scl(highestPoint);
        this.position.add(bump);
        this.cosmicReach_Seamless_Portals$tmpPortalNextPosition.add(bump);
    }

    @Unique
    private boolean cosmicReach_Seamless_Portals$isPlayer(){
        return ((Entity) (Object) this) instanceof PlayerEntity;
    }

    @Unique
    private boolean cosmicReach_Seamless_Portals$isLocalPlayer(){
        return ((Entity) (Object) this) == GameSingletons.client().getLocalPlayer().getEntity();
    }

    @Unique
    public boolean cosmicReach_Seamless_Portals$isJustTeleported(){
        return this.cosmicReach_Seamless_Portals$justTeleported;
    }

    @Unique
    public void cosmicReach_Seamless_Portals$setIgnorePortals(boolean value){
        this.cosmicReach_Seamless_Portals$ignorePortals = value;
    }

    @Unique
    public Portal cosmicReach_Seamless_Portals$getTeleportingPortal(){
        return this.cosmicReach_Seamless_Portals$teleportPortal;
    }

    @Override
    public Array<Portal> cosmicReach_Seamless_Portals$getNearbyPortals() {
        return cosmicReach_Seamless_Portals$nearbyPortals;
    }
}
