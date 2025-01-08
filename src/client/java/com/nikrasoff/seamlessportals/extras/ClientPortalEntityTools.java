package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.graphics.Camera;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public class ClientPortalEntityTools {
    public static void renderWithoutAnimation(Entity entity, Camera camera){
        IModEntity portalableEntity = (IModEntity) entity;
        portalableEntity.cosmicReach_Seamless_Portals$renderNoAnim(camera);
    }
    public static void renderDuplicate(Entity entity, Camera renderCamera, Portal portal){
        IModEntity e = (IModEntity) entity;
        e.cosmicReach_Seamless_Portals$renderDuplicate(renderCamera, portal);
    }
    public static void advanceAnimations(Entity entity){
        IModEntity e = (IModEntity) entity;
        e.cosmicReach_Seamless_Portals$advanceAnimations();
    }

    public static boolean hasBeenRenderedThisFrame(Entity entity){
        IModEntity entity1 = (IModEntity) entity;
        return entity1.cosmicReach_Seamless_Portals$checkIfHasBeenRendered();
    }

    public static void resetIfEntityRendered(Entity entity){
        IModEntity entity1 = (IModEntity) entity;
        entity1.cosmicReach_Seamless_Portals$resetRender();
    }
}
