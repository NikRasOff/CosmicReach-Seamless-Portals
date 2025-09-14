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

        SeamlessPortals.portalManager.forEachPortal((Portal portal) -> {
            if (portal.isPortalDestroyed) {
                return;
            }
            if (portal.zone != zone) return;
            solveForPortal(zone, entity, deltaTime, portal);
        });
    }
}
