package com.nikrasoff.seamlessportals.api;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

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
        portal.getBoundingBox(tmpBB2);
        return tmpBB1.intersects(tmpBB2);
    }
    default void advanceAnimations(Entity entity){}
}
