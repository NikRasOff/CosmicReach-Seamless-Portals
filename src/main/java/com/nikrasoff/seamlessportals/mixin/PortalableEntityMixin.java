package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.extras.DirectionVector;
import com.nikrasoff.seamlessportals.extras.IPortalableEntity;
import com.nikrasoff.seamlessportals.extras.IPortalablePlayer;
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
    private transient boolean justTeleported = false;
    @Unique
    private transient Portal teleportPortal;
    @Unique
    private transient boolean ignorePortals = false;
    @Unique
    private static final float terminalVelocity = 100; // This value was chosen randomly

    @Unique
    private transient Array<BlockPosition> tmpNonCollideBlocks = new Array<>();
    @Unique
    private transient Array<BlockPosition> tmpCollidedBlocks = new Array<>();
    @Unique
    private transient OrientedBoundingBox tmpPortaledBoundingBox = new OrientedBoundingBox();
    @Unique
    private transient BoundingBox tmpPortalCheckBlockBoundingBox = new BoundingBox();
    @Unique
    private transient Matrix4 tmpPortalTransformMatrix = new Matrix4();
    @Unique
    private transient Vector3 tmpPortalNextPosition = new Vector3();

    @Inject(method = "updatePositions", at = @At("HEAD"))
    private void onEntityUpdate(Zone zone, double deltaTime, CallbackInfo ci) {
        if (this.velocity.cpy().len() > terminalVelocity){
            this.velocity.clamp(0, terminalVelocity);
        }
        if (this.ignorePortals) return;
        this.tmpNonCollideBlocks.clear();
        this.tmpNonCollideBlocks.addAll(this.tmpCollidedBlocks);
        this.tmpCollidedBlocks.clear();
        this.tmpPortaledBoundingBox.setBounds(this.localBoundingBox);
        this.tmpPortaledBoundingBox.getBounds().min.add(new Vector3(-0.01F, -0.01F, -0.01F));
        this.tmpPortaledBoundingBox.getBounds().max.add(new Vector3(0.01F, 0.01F, 0.01F));
        this.justTeleported = false;
        this.teleportPortal = null;
        // A bit of a hack since entities don't keep track of the zones they're in
        // And since for now there's only one entity - the player
        // TODO: Fix when more entities/multiplayer gets added
        updateTrackedPortals(deltaTime);
    }

    @Unique
    private void updateTrackedPortals(double deltaTime){
        if (SeamlessPortals.portalManager.createdPortals.isEmpty()) return;

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
        this.tmpPortalNextPosition.set(targetPosition);

        Ray posChange = new Ray(prevPos.cpy().add(new Vector3(0, 0.05F, 0)), targetPosition.cpy().sub(prevPos));

        for (Portal portal : SeamlessPortals.portalManager.createdPortals){
            if (portal.isPortalDestroyed || !portal.isPortalStable()) {
                continue;
            }
            if (portal.zoneID.equals(InGame.getLocalPlayer().zoneId) && !portal.isOnSameSideOfPortal(prevPos, targetPosition) && Intersector.intersectRayOrientedBounds(posChange, portal.getMeshBoundingBox(), new Vector3())){
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
        // Refer to the comment in method teleportThroughPortal for an explanation
        BlockState orBlockState = original.call(instance, x, y, z);
        if (this.ignorePortals){
            return orBlockState;
        }

        Chunk c = instance.getChunkAtBlock(x, y, z);
        BlockPosition curBlockPos = new BlockPosition(c, x - c.blockX, y - c.blockY, z - c.blockZ);
        if (this.tmpNonCollideBlocks.contains(curBlockPos, false)){
            if (!this.tmpCollidedBlocks.contains(curBlockPos, false)){
                this.tmpCollidedBlocks.add(curBlockPos);
            }
            return null;
        }

        if (orBlockState != null && !orBlockState.walkThrough){
            orBlockState.getBoundingBox(this.tmpPortalCheckBlockBoundingBox, x, y, z);
            Vector3 checkCenter = new Vector3();
            this.tmpPortalCheckBlockBoundingBox.getCenter(checkCenter);
            Vector3 portalCollisionCheckPos = this.isJustTeleported() ? this.tmpPortalNextPosition : this.position.cpy();
            Ray ray = new Ray(portalCollisionCheckPos, checkCenter.cpy().sub(portalCollisionCheckPos));
            for (Portal portal : SeamlessPortals.portalManager.createdPortals){
                if (portal.zoneID.equals(InGame.getLocalPlayer().zoneId) && !portal.isOnSameSideOfPortal(portalCollisionCheckPos, checkCenter) && Intersector.intersectRayOrientedBounds(ray, portal.getMeshBoundingBox(), new Vector3())){
                    if (!portal.getMeshBoundingBox().intersects(this.tmpPortalCheckBlockBoundingBox)){
                        return null;
                    }
                }
            }

            if (this.isJustTeleported()){
                if (!this.tmpPortaledBoundingBox.intersects(this.tmpPortalCheckBlockBoundingBox) && this.tmpEntityBoundingBox.intersects(this.tmpPortalCheckBlockBoundingBox)){
                    this.tmpNonCollideBlocks.add(curBlockPos);
                    this.tmpCollidedBlocks.add(curBlockPos);
                    return null;
                }
            }
        }
        return orBlockState;
    }

    @Unique
    public void teleportThroughPortal(Portal portal) {
        // TODO: Fix when more entities/multiplayer gets added
        InGame.getLocalPlayer().zoneId = new String(portal.linkedPortal.zoneID);
        this.tmpPortalNextPosition.set(portal.getPortaledPos(this.tmpPortalNextPosition));
        this.viewDirection = portal.getPortaledVector(this.viewDirection);
        this.velocity.sub(portal.velocity);
        this.velocity.sub(portal.onceVelocity);
        this.position = portal.getPortaledPos(this.position);
        this.velocity = portal.getPortaledVector(this.velocity);
        this.onceVelocity = portal.getPortaledVector(this.onceVelocity);
        this.acceleration.set(portal.getPortaledVector(this.acceleration));
        this.velocity.add(portal.linkedPortal.velocity);
        this.velocity.add(portal.linkedPortal.onceVelocity);
        if (this.isLocalPlayer()){
            portal.linkedPortal.updatePortalMeshScale((PerspectiveCamera) GameState.IN_GAME.getWorldCamera());
        }
        Vector3 originalPos = this.position.cpy();

        // A bunch of magic to make mismatched portals more intuitive
        // to the player and less intuitive to any poor soul
        // who happens to be looking through this code

        // sorry not sorry
        this.snapOnGoThroughPortal(portal);

        // Animating camera turning
        if (this.isLocalPlayer()){
            IPortalablePlayer locPlayer = (IPortalablePlayer) InGame.getLocalPlayer();
            Vector3 offset = originalPos.sub(this.position);
            locPlayer.portalCurrentCameraTransform(portal, offset);
        }

        this.justTeleported = true;
        this.teleportPortal = portal;
        Vector3 orPos = new Vector3(this.position);
        this.tmpPortalTransformMatrix.setToLookAt(orPos, orPos.cpy().add(portal.linkedPortal.getPortaledVector(new Vector3(0, 0, 1))), portal.linkedPortal.getPortaledVector(new Vector3(0, 1, 0))).inv();
        this.tmpPortaledBoundingBox.setTransform(this.tmpPortalTransformMatrix);
    }

    @Unique
    private void snapOnGoThroughPortal(Portal portal){
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

        // TODO: Fix when more entities/multiplayer gets added
        Zone curZone = InGame.getLocalPlayer().getZone(InGame.world); // For now just assume that this entity is the player

        float highestPoint = 0;
        for (int bx = minbx; bx <= maxbx; ++bx){
            for (int by = minby; by <= maxby; ++by){
                for (int bz = minbz; bz <= maxbz; ++bz){
                    BlockState checkBlock = curZone.getBlockState(bx, by, bz);
                    if (checkBlock != null && !checkBlock.walkThrough){
                        checkBlock.getBoundingBox(this.tmpPortalCheckBlockBoundingBox, bx, by, bz);
                        // Figure out if the block can just be discarded (to prevent some weirdness)
                        float checkPoint;
                        switch (direction.getName()){
                            case "negZ", "negX", "negY" -> checkPoint = portal.linkedPortal.getPortaledPos(this.tmpPortalCheckBlockBoundingBox.min).y;
                            default -> checkPoint = portal.linkedPortal.getPortaledPos(this.tmpPortalCheckBlockBoundingBox.max).y;
                        }
                        if (checkPoint > portal.linkedPortal.getPortaledPos(this.position).y + 0.01) continue;

                        if (this.tmpEntityBoundingBox.intersects(this.tmpPortalCheckBlockBoundingBox)){
                            // Figure out how high the player should be snapped
                            float curPoint;
                            Vector3 blockOffsetMin = this.tmpPortalCheckBlockBoundingBox.max.cpy().sub(this.tmpEntityBoundingBox.min);
                            Vector3 blockOffsetMax = this.tmpPortalCheckBlockBoundingBox.min.cpy().sub(this.tmpEntityBoundingBox.max).scl(-1);
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
        this.tmpPortalNextPosition.add(bump);
    }

    @Unique
    private boolean isLocalPlayer(){
        return ((IPortalableEntity) InGame.getLocalPlayer().getEntity() == this);
    }

    @Unique
    public boolean isJustTeleported(){
        return this.justTeleported;
    }

    @Unique
    public void setIgnorePortals(boolean value){
        this.ignorePortals = value;
    }

    @Unique
    public Portal getTeleportingPortal(){
        return this.teleportPortal;
    }
}
