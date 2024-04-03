package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.portals.Portal;

public interface IPortalableEntity {
    public Array<Portal> getNearbyPortals();
    public void setIgnorePortals(boolean value);
    public boolean isJustTeleported();
    public boolean hasCameraJustTeleported(Portal portal);

    public static void setIgnorePortals(IPortalableEntity entity, boolean value){
        entity.setIgnorePortals(value);
    }
}
