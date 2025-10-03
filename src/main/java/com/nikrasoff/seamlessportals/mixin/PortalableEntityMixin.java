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
import com.nikrasoff.seamlessportals.entities.components.PortalCheckComponent;
import com.nikrasoff.seamlessportals.extras.ExtraPortalUtils;
import com.nikrasoff.seamlessportals.extras.interfaces.*;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.components.IUpdateEntityComponent;
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
    @Shadow
    protected transient BoundingBox tmpEntityBoundingBox;
    @Shadow public Vector3 velocity;
    @Shadow public BoundingBox localBoundingBox;
    @Shadow public abstract void addUpdatingComponent(IUpdateEntityComponent c);

    @Shadow private transient Array<IUpdateEntityComponent> updatingComponents;
    @Shadow public transient Zone zone;
    @Shadow public Vector3 position;
    @Unique
    private transient boolean cosmicReach_Seamless_Portals$justTeleported = false;
    @Unique
    private transient Portal cosmicReach_Seamless_Portals$teleportPortal;
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

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("TAIL"))
    private void addPortalComponent(String entityTypeId, CallbackInfo ci){
        this.addUpdatingComponent(PortalCheckComponent.INSTANCE);
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void onProperUpdate(Zone zone, float deltaTime, CallbackInfo ci){
        this.cosmicReach_Seamless_Portals$tmpNonCollideBlocks.clear();
        this.cosmicReach_Seamless_Portals$tmpNonCollideBlocks.addAll(this.cosmicReach_Seamless_Portals$tmpCollidedBlocks);
        this.cosmicReach_Seamless_Portals$tmpCollidedBlocks.clear();
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.setBounds(this.localBoundingBox);
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.getBounds().min.add(new Vector3(-0.01F, -0.05F, -0.01F));
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.getBounds().max.add(new Vector3(0.01F, 0.01F, 0.01F));
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.getBounds().update();
        this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.setBounds(this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox.getBounds());
        this.cosmicReach_Seamless_Portals$justTeleported = false;
        this.cosmicReach_Seamless_Portals$teleportPortal = null;
    }

    @Inject(method = "updatePositions", at = @At("HEAD"))
    private void onEntityUpdate(Zone zone, float deltaTime, CallbackInfo ci) {
        if (this.velocity.cpy().len() > cosmicReach_Seamless_Portals$terminalVelocity){
            this.velocity.clamp(0, cosmicReach_Seamless_Portals$terminalVelocity);
        }
    }

    @WrapOperation(method = "updateConstraints", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Zone;getBlockState(III)Lfinalforeach/cosmicreach/blocks/BlockState;"))
    private BlockState updateConstraintsMixin(Zone instance, int x, int y, int z, Operation<BlockState> original){
        return this.cosmicReach_Seamless_Portals$checkIfShouldCollidePortal(instance, x, y, z, original);
    }

    public BlockState cosmicReach_Seamless_Portals$checkIfShouldCollidePortal(Zone instance, int x, int y, int z, Operation<BlockState> original){
        // Refer to the comment in teleportThroughPortal for an explanation
        BlockState orBlockState = original.call(instance, x, y, z);
        if (!this.updatingComponents.contains(PortalCheckComponent.INSTANCE, true)){
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

            Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
            for (Portal portal : portals){
                if (portal.zone == this.zone && portal.isNotOnSameSideOfPortal(portalCollisionCheckPos, checkCenter) && Intersector.intersectRayOrientedBounds(ray, portal.getMeshBoundingBox(), new Vector3())){
                    if (!portal.getMeshBoundingBox().intersects(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox)){
                        return null;
                    }
                }
            }

            if (this.cosmicReach_Seamless_Portals$isJustTeleported()){
                if (!ExtraPortalUtils.intersectOrientedBounds(this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox, this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox) && this.tmpEntityBoundingBox.intersects(this.cosmicReach_Seamless_Portals$tmpPortalCheckBlockBoundingBox)){
                    this.cosmicReach_Seamless_Portals$tmpNonCollideBlocks.add(curBlockPos);
                    this.cosmicReach_Seamless_Portals$tmpCollidedBlocks.add(curBlockPos);
                    return null;
                }
            }
        }
        return orBlockState;
    }

    @Unique
    public boolean cosmicReach_Seamless_Portals$isJustTeleported(){
        return this.cosmicReach_Seamless_Portals$justTeleported;
    }

    @Unique
    public Portal cosmicReach_Seamless_Portals$getTeleportingPortal(){
        return this.cosmicReach_Seamless_Portals$teleportPortal;
    }

    @Override
    public void cosmicReach_Seamless_Portals$setTmpNextPosition(Vector3 pos) {
        this.cosmicReach_Seamless_Portals$tmpPortalNextPosition.set(pos);
    }

    @Override
    public Vector3 cosmicReach_Seamless_Portals$getTmpNextPosition() {
        return this.cosmicReach_Seamless_Portals$tmpPortalNextPosition;
    }

    @Override
    public void cosmicReach_Seamless_Portals$setJustTeleported(boolean value){
        this.cosmicReach_Seamless_Portals$justTeleported = value;
    }
    public void cosmicReach_Seamless_Portals$setTeleportingPortal(Portal portal){
        this.cosmicReach_Seamless_Portals$teleportPortal = portal;
    }
    public Matrix4 cosmicReach_Seamless_Portals$getTmpTransformMatrix(){
        return this.cosmicReach_Seamless_Portals$tmpPortalTransformMatrix;
    }
    public OrientedBoundingBox cosmicReach_Seamless_Portals$getPortaledBoundingBox(){
        return this.cosmicReach_Seamless_Portals$tmpPortaledBoundingBox;
    }
}
