package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.graphics.Camera;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public class ClientPortalEntityTools {
    // Tools specifically for interacting with mixins
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
    public static void renderSliced(Entity entity, Camera renderCamera, Portal portal){
        IModEntity e = (IModEntity) entity;
        e.cosmicReach_Seamless_Portals$renderSliced(renderCamera, portal);
    }

    public static void renderDuplicateSliced(Entity entity, Camera renderCamera, Portal portal){
        IModEntity e = (IModEntity) entity;
        e.cosmicReach_Seamless_Portals$renderDuplicateSliced(renderCamera, portal);
    }

    public static boolean isJustTeleported(Entity entity){
        IPortalableEntity p = (IPortalableEntity) entity;
        return p.cosmicReach_Seamless_Portals$isJustTeleported();
    }
}
