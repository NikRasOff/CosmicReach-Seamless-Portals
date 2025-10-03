package com.nikrasoff.seamlessportals.entities.components;

import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.api.IPortalInteractionSolver;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.components.IUpdateEntityComponent;
import finalforeach.cosmicreach.world.Zone;

public class PortalCheckComponent implements IUpdateEntityComponent {
    // Activates the appropriate portal interaction solver for the entity

    public static final IUpdateEntityComponent INSTANCE = new PortalCheckComponent();

    public PortalCheckComponent(){
    }
    public void update(Zone zone, Entity entity, float deltaTime) {
        IPortalInteractionSolver s = SeamlessPortalsConstants.getPortalInteractionSolver(entity.getClass());

        if (s != null){
            s.solveForAllPortals(zone, entity, deltaTime);
        }
    }
}
