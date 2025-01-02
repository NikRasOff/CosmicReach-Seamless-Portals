package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.utils.Array;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

public interface IPortalableEntity {
    Array<Portal> cosmicReach_Seamless_Portals$getNearbyPortals();
    void cosmicReach_Seamless_Portals$setIgnorePortals(boolean value);
    boolean cosmicReach_Seamless_Portals$isJustTeleported();
    BlockState cosmicReach_Seamless_Portals$checkIfShouldCollidePortal(Zone instance, int x, int y, int z, Operation<BlockState> original);
    Portal cosmicReach_Seamless_Portals$getTeleportingPortal(); // Not used, too lazy to remove
    static void setIgnorePortals(IPortalableEntity entity, boolean value){
        entity.cosmicReach_Seamless_Portals$setIgnorePortals(value);
    }
}
