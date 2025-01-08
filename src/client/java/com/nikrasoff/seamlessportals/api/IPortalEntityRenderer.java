package com.nikrasoff.seamlessportals.api;

import com.badlogic.gdx.graphics.Camera;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public interface IPortalEntityRenderer {
    void render(Entity entity, Camera renderCamera);
    // This renders the duplicate of the entity when going through a portal
    void renderDuplicate(Entity entity, Camera renderCamera, Portal portal);
    boolean shouldRenderDuplicate(Entity entity, Portal portal);
    default void advanceAnimations(Entity entity){}
}
