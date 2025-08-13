package com.nikrasoff.seamlessportals.api;

import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import finalforeach.cosmicreach.entities.Entity;

public interface IPortalSolverInitialiser {
    void initPortalInteractionSolvers();

    static void registerPortalInteractionSolver(Class<? extends Entity> clazz, IPortalInteractionSolver solver){
        SeamlessPortalsConstants.registerPortalInteractionSolver(clazz, solver);
    }
}
