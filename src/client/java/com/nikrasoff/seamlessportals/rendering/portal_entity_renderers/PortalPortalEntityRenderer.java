package com.nikrasoff.seamlessportals.rendering.portal_entity_renderers;

import com.badlogic.gdx.graphics.Camera;
import com.nikrasoff.seamlessportals.api.IPortalEntityRenderer;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public class PortalPortalEntityRenderer implements IPortalEntityRenderer {
    // This class exists so that at some point in the future, I may add recursive portal rendering
    @Override
    public void render(Entity entity, Camera renderCamera) {
//        entity.render(renderCamera);
    }

    @Override
    public void renderDuplicate(Entity entity, Camera renderCamera, Portal portal) {
    }

    @Override
    public void renderSliced(Entity entity, Camera renderCamera, Portal portal) {
    }

    @Override
    public void renderDuplicateSliced(Entity entity, Camera renderCamera, Portal portal) {
    }

    @Override
    public boolean isCloseToPortal(Entity entity, Portal portal) {
        return false;
    }
}
