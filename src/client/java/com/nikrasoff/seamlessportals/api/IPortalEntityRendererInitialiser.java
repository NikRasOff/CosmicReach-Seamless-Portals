package com.nikrasoff.seamlessportals.api;

import com.nikrasoff.seamlessportals.SPClientConstants;
import finalforeach.cosmicreach.entities.Entity;

public interface IPortalEntityRendererInitialiser {
    void initPortalEntityRenderers();

    static void registerPortalEntityRenderer(Class<? extends Entity> clazz, IPortalEntityRenderer entityRenderer){
        SPClientConstants.registerPortalEntityRenderer(clazz, entityRenderer);
    }
}
