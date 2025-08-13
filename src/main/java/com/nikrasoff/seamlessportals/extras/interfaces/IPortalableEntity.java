package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Array;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

public interface IPortalableEntity {
    Array<Portal> cosmicReach_Seamless_Portals$getNearbyPortals();
    void cosmicReach_Seamless_Portals$setTmpNextPosition(Vector3 pos);
    Vector3 cosmicReach_Seamless_Portals$getTmpNextPosition();
    void cosmicReach_Seamless_Portals$setJustTeleported(boolean value);
    void cosmicReach_Seamless_Portals$setTeleportingPortal(Portal portal);
    Matrix4 cosmicReach_Seamless_Portals$getTmpTransformMatrix();
    OrientedBoundingBox cosmicReach_Seamless_Portals$getPortaledBoundingBox();
    boolean cosmicReach_Seamless_Portals$isJustTeleported();
    BlockState cosmicReach_Seamless_Portals$checkIfShouldCollidePortal(Zone instance, int x, int y, int z, Operation<BlockState> original);
    Portal cosmicReach_Seamless_Portals$getTeleportingPortal(); // Not used, too lazy to remove
}
