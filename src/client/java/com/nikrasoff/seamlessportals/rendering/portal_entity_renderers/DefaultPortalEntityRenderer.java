package com.nikrasoff.seamlessportals.rendering.portal_entity_renderers;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.api.IPortalEntityRenderer;
import com.nikrasoff.seamlessportals.extras.ClientPortalEntityTools;
import com.nikrasoff.seamlessportals.extras.ClientPortalExtras;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public class DefaultPortalEntityRenderer implements IPortalEntityRenderer {
    @Override
    public void render(Entity entity, Camera renderCamera) {
        ClientPortalEntityTools.renderWithoutAnimation(entity, renderCamera);
    }

    @Override
    public void renderDuplicate(Entity entity, Camera renderCamera, Portal portal) {
        ClientPortalEntityTools.renderDuplicate(entity, renderCamera, portal);
    }

    @Override
    public void renderSliced(Entity entity, Camera renderCamera, Portal portal) {
        ClientPortalEntityTools.renderSliced(entity, renderCamera, portal);
    }

    @Override
    public void renderDuplicateSliced(Entity entity, Camera renderCamera, Portal portal) {
        ClientPortalEntityTools.renderDuplicateSliced(entity, renderCamera, portal);
    }

    @Override
    public void advanceAnimations(Entity entity) {
        if (!ClientPortalEntityTools.hasBeenRenderedThisFrame(entity)){
//            if (ClientPortalExtras.isEntityLocalPlayer(entity)) SeamlessPortals.LOGGER.info("Rendered player through portals");
            ClientPortalEntityTools.advanceAnimations(entity);
        }
        else {
            ClientPortalEntityTools.resetIfEntityRendered(entity);
        }
    }
}
