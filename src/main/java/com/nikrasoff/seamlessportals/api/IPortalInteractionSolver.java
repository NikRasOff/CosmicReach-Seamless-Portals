package com.nikrasoff.seamlessportals.api;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public interface IPortalInteractionSolver {
    void solveForPortal(Zone zone, Entity entity, float deltaTime, Portal interactingPortal);

    default void solveForAllPortals(Zone zone, Entity entity, float deltaTime){
        if (SeamlessPortals.portalManager.createdPortals.isEmpty()) return;

        for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
            Portal portal = portalEntry.getValue();
            if (portal.isPortalDestroyed) {
                continue;
            }
            if (portal.zone != zone) continue;
            solveForPortal(zone, entity, deltaTime, portal);
        }
    }
}
