package com.nikrasoff.seamlessportals.entities.portal_solvers;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.api.IPortalInteractionSolver;
import com.nikrasoff.seamlessportals.extras.DirectionVector;
import com.nikrasoff.seamlessportals.extras.PortalEntityTools;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.IDamageSource;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public class DefaultPortalInteractionSolver implements IPortalInteractionSolver {
    // This is used when no other solver is registered for the entity or the
    // entity's parent classes
    private static final Vector3 portalPosCheckEpsilon = new Vector3(0f, 0.05f, 0f);
    private final Vector3 prevPos = new Vector3();
    private final Vector3 targetPos = new Vector3();
    private Ray posChange;
    private boolean stopCheck = false;

    private static final BoundingBox testBox = new BoundingBox();
    private static final BoundingBox testBox2 = new BoundingBox();

    public void teleportThroughPortal(Zone zone, Entity entity, Portal portal){
        if (entity instanceof PlayerEntity pe){
            Player pl = pe.getPlayer();
            pl.setZone(portal.linkedPortal.zone.zoneId);
        }
        if (portal.zone != portal.linkedPortal.zone){
            GameSingletons.world.getZoneIfExists(portal.zone.zoneId).removeEntity(entity);
            GameSingletons.world.getZoneIfExists(portal.linkedPortal.zone.zoneId).addEntity(entity);
        }
        PortalEntityTools.setTmpNextPosition(entity, portal.getPortaledPos(PortalEntityTools.getTmpNextPosition(entity)));
        entity.viewDirection = portal.getPortaledVector(entity.viewDirection);
        entity.velocity.sub(portal.velocity);
        entity.velocity.sub(portal.onceVelocity);
        entity.setPosition(portal.getPortaledPos(entity.position));
        entity.velocity = portal.getPortaledVector(entity.velocity);
        entity.onceVelocity = portal.getPortaledVector(entity.onceVelocity);
        entity.acceleration.set(portal.getPortaledVector(entity.acceleration));
        entity.velocity.add(portal.linkedPortal.velocity);
        entity.velocity.add(portal.linkedPortal.onceVelocity);
        Vector3 originalPos = entity.position.cpy();

        // A bunch of magic to make mismatched portals more intuitive
        // to the player and less intuitive to any poor soul
        // who happens to be looking through this code

        // sorry not sorry
        this.snapOnGoThroughPortal(zone, entity, portal);

        // Animating camera turning
        if (GameSingletons.isClient){
            if (entity instanceof PlayerEntity pe && pe.getPlayer() == GameSingletons.client().getLocalPlayer()){
                SeamlessPortals.clientConstants.animateCameraTurning(originalPos, entity.position, portal);
            }
            SeamlessPortals.clientConstants.flagEntityModelInstanceForTeleporting(entity, portal);
        }

        PortalEntityTools.setJustTeleported(entity, true);
        PortalEntityTools.setTeleportingPortal(entity, portal);
        Vector3 orPos = new Vector3(entity.position);
        PortalEntityTools.getTmpTransformMatrix(entity).setToLookAt(orPos, orPos.cpy().add(portal.linkedPortal.getPortaledVector(new Vector3(0, 0, 1))), portal.linkedPortal.getPortaledVector(new Vector3(0, 1, 0))).inv();
        PortalEntityTools.getPortaledBoundingBox(entity).setTransform(PortalEntityTools.getTmpTransformMatrix(entity));
    }

    private void snapOnGoThroughPortal(Zone zone, Entity entity, Portal portal){
        // Making this is pure suffering
        // Why is collision with blocks so hard to do?
        // Isn't it, like, THE thing this game should be good at?

        // First, figure out the direction we should be checking
        DirectionVector direction = DirectionVector.getClosestDirection(portal.getPortaledVector(DirectionVector.POS_Y.getVector()));

        // Now, we get the new bounding box
        testBox.set(entity.localBoundingBox);
        testBox.min.add(entity.position);
        testBox.max.add(entity.position);
        testBox.update();

        // Now, get the range of blocks to check against
        Vector3 minPoint = new Vector3();
        Vector3 maxPoint = new Vector3();

        switch (direction.getName()){
            case "negZ":
                testBox.getCorner001(minPoint);
                testBox.getCorner111(maxPoint);
                break;
            case "posX":
                testBox.getCorner000(minPoint);
                testBox.getCorner011(maxPoint);
                break;
            case "negX":
                testBox.getCorner100(minPoint);
                testBox.getCorner111(maxPoint);
                break;
            case "posY":
                testBox.getCorner000(minPoint);
                testBox.getCorner101(maxPoint);
                break;
            case "negY":
                testBox.getCorner010(minPoint);
                testBox.getCorner111(maxPoint);
                break;
            default:
                testBox.getCorner000(minPoint);
                testBox.getCorner110(maxPoint);
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
                        checkBlock.getBoundingBox(testBox2, bx, by, bz);
                        // Figure out if the block can just be discarded (to prevent some weirdness)
                        float checkPoint;
                        switch (direction.getName()){
                            case "negZ", "negX", "negY" -> checkPoint = portal.linkedPortal.getPortaledPos(testBox2.min).y;
                            default -> checkPoint = portal.linkedPortal.getPortaledPos(testBox2.max).y;
                        }
                        if (checkPoint > portal.linkedPortal.getPortaledPos(entity.position).y + 0.01) continue;

                        if (testBox.intersects(testBox2)){
                            // Figure out how high the player should be snapped
                            float curPoint;
                            Vector3 blockOffsetMin = testBox2.max.cpy().sub(testBox.min);
                            Vector3 blockOffsetMax = testBox2.min.cpy().sub(testBox.max).scl(-1);
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
        entity.position.add(bump);
        PortalEntityTools.getTmpNextPosition(entity).add(bump);
    }

    @Override
    public void solveForPortal(Zone zone, Entity entity, float deltaTime, Portal interactingPortal) {
        if (interactingPortal.isPortalDestroyed) return;
        if (interactingPortal.zone != zone) return;
        if (interactingPortal.isNotOnSameSideOfPortal(prevPos.cpy().add(portalPosCheckEpsilon), targetPos.cpy().add(portalPosCheckEpsilon)) && Intersector.intersectRayOrientedBounds(posChange, interactingPortal.getMeshBoundingBox(), new Vector3())){
            if (interactingPortal.linkedPortal == null){
                entity.hit(new IDamageSource() {
                    @Override
                    public boolean isEntity() {
                        return false;
                    }
                }, 1000000);
                stopCheck = true;
                return;
            }
            this.teleportThroughPortal(zone, entity, interactingPortal);
            stopCheck = true;
        }
    }

    private void applyFriction(Vector3 vel, float coefficient){
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

    @Override
    public void solveForAllPortals(Zone zone, Entity entity, float deltaTime) {
        if (SeamlessPortals.portalManager.createdPortals.isEmpty()) return;

        prevPos.set(entity.position);
        float ax = entity.acceleration.x * deltaTime;
        float ay = entity.acceleration.y * deltaTime;
        float az = entity.acceleration.z * deltaTime;
        Vector3 testVelocity = entity.velocity.cpy();
        testVelocity.add(ax, ay, az);
        testVelocity.add(entity.onceVelocity);

        if (entity.isNoClip()) {
            applyFriction(testVelocity, 1.0F);
        } else {
            applyFriction(testVelocity, entity.floorFriction);
        }

        float vx = testVelocity.x * deltaTime;
        float vy = testVelocity.y * deltaTime;
        float vz = testVelocity.z * deltaTime;
        Vector3 posDiff = new Vector3(vx, vy, vz);
        targetPos.set(entity.position).add(posDiff);
        ((IPortalableEntity) entity).cosmicReach_Seamless_Portals$setTmpNextPosition(targetPos);

        this.posChange = new Ray(prevPos.cpy().add(portalPosCheckEpsilon), targetPos.cpy().add(portalPosCheckEpsilon).sub(prevPos));

        Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
        for (Portal portal : portals){
            this.solveForPortal(zone, entity, deltaTime, portal);
            if (stopCheck){
                stopCheck = false;
                break;
            }
        }
    }
}
