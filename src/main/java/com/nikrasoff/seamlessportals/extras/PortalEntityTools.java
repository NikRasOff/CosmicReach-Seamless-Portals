package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public class PortalEntityTools {
    public static Array<Portal> getNearbyPortals(Entity of){
        IPortalableEntity pe = (IPortalableEntity) of;
        return pe.cosmicReach_Seamless_Portals$getNearbyPortals();
    }
    public static Portal getNearestTouchingPortal(Entity to){ // This only counts nearby portals
        Array<Portal> nearbyPortals = getNearbyPortals(to);
        Portal n = null;
        for (Portal portal : nearbyPortals){
            if (n == null){
                n = portal;
                continue;
            }
            if (n.position.dst2(to.position) > portal.position.dst2(to.position)) n = portal;
        }
        return n;
    }
}
