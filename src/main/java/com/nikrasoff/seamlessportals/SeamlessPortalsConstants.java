package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.nikrasoff.seamlessportals.api.IPortalInteractionSolver;
import com.nikrasoff.seamlessportals.entities.portal_solvers.DefaultPortalInteractionSolver;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.util.GameTag;

public class SeamlessPortalsConstants {
    public static String MOD_ID = "seamlessportals";
    public static final Vector3 portalCheckEpsilon = new Vector3(0, 0.05f, 0);
    private static final ObjectMap<Class<? extends Entity>, IPortalInteractionSolver> entityPortalSolverMap = new ObjectMap<>();
    private static final DefaultPortalInteractionSolver defaultSolver = new DefaultPortalInteractionSolver();
    public static final GameTag PORTAL_WHITELISTED = GameTag.get("portal_whitelisted");
    public static final GameTag PORTAL_BLACKLISTED = GameTag.get("portal_blacklisted");

    public static void registerPortalInteractionSolver(Class<? extends Entity> clazz, IPortalInteractionSolver checker){
        if (entityPortalSolverMap.containsKey(clazz)){
            throw new RuntimeException("Cannot have multiple interaction solvers registered to the same entity class: " + clazz.getSimpleName());
        }
        entityPortalSolverMap.put(clazz, checker);
    }

    public static IPortalInteractionSolver getPortalInteractionSolver(Class<? extends Entity> clazz){
        Class<? extends Entity> cl = clazz;
        for(IPortalInteractionSolver r; cl != null; cl = (Class<? extends Entity>) cl.getSuperclass()) {
            r = entityPortalSolverMap.get(cl);
            if (r != null) {
                return r;
            }
        }
        return defaultSolver;
    }
}
