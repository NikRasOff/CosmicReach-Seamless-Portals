package com.nikrasoff.seamlessportals.rendering.portal_entity_renderers;

import com.badlogic.gdx.graphics.Camera;
import finalforeach.cosmicreach.entities.Entity;

public interface IPortalEntityRenderer {
    void render(Entity entity, Camera renderCamera);
    default void advanceAnimations(Entity entity){}
}
