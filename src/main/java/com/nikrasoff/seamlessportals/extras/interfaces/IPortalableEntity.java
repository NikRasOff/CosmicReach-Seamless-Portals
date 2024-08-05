package com.nikrasoff.seamlessportals.extras.interfaces;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

public interface IPortalableEntity {
    public void setIgnorePortals(boolean value);
    public boolean isJustTeleported();
    public BlockState checkIfShouldCollidePortal(Zone instance, int x, int y, int z, Operation<BlockState> original);
    public Portal getTeleportingPortal(); // Not used, too lazy to remove
    public static void setIgnorePortals(IPortalableEntity entity, boolean value){
        entity.setIgnorePortals(value);
    }
}
