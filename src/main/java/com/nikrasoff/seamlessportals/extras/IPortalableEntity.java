package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.utils.Array;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

public interface IPortalableEntity {
    public Array<Portal> getNearbyPortals();
    public void setIgnorePortals(boolean value);
    public boolean isJustTeleported();
    public boolean hasCameraJustTeleported(Portal portal);
    public BlockState checkIfShouldCollidePortal(Zone instance, int x, int y, int z, Operation<BlockState> original);

    public static void setIgnorePortals(IPortalableEntity entity, boolean value){
        entity.setIgnorePortals(value);
    }
}
