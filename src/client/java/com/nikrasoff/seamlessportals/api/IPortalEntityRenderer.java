package com.nikrasoff.seamlessportals.api;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.entities.instances.EntityModelInstance;

public interface IPortalEntityRenderer {
    BoundingBox tmpBB1 = new BoundingBox();
    BoundingBox tmpBB2 = new BoundingBox();
    void render(Entity entity, Camera renderCamera);
    // This renders the duplicate of the entity when going through a portal
    void renderDuplicate(Entity entity, Camera renderCamera, Portal portal);
    // This renders the entity when close to a portal and slices its model
    // Basically the same as the above but doesn't transport the model to the other side of the portal
    void renderSliced(Entity entity, Camera renderCamera, Portal portal);
    // Determines if above two functions should be called instead of the default render
    default boolean isCloseToPortal(Entity entity, Portal portal){
        if (entity.zone != portal.zone){
            return false;
        }
        entity.getBoundingBox(tmpBB1);
        return portal.getFatBoundingBox().intersects(tmpBB1);
    }
    default void advanceAnimations(Entity entity){}
    // For rendering duplicates of base game entity model instances
    static void renderModelInstanceDuplicate(EntityModelInstance instance, Entity entity, Camera renderCamera, Matrix4 modelMatrix, Portal portal){
        ((IModEntityModelInstance) instance).cosmicReach_Seamless_Portals$renderSliced(entity, renderCamera, modelMatrix, portal, true);
    }
    // For rendering sliced base game entity model instances
    static void renderModelInstanceSliced(EntityModelInstance instance, Entity entity, Camera renderCamera, Matrix4 modelMatrix, Portal portal){
        ((IModEntityModelInstance) instance).cosmicReach_Seamless_Portals$renderSliced(entity, renderCamera, modelMatrix, portal, false);
    }
}
