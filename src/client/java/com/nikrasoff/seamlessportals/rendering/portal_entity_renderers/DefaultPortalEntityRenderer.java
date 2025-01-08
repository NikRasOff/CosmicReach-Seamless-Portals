package com.nikrasoff.seamlessportals.rendering.portal_entity_renderers;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.nikrasoff.seamlessportals.api.IPortalEntityRenderer;
import com.nikrasoff.seamlessportals.extras.ClientPortalEntityTools;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public class DefaultPortalEntityRenderer implements IPortalEntityRenderer {
    static protected BoundingBox tmpBB1 = new BoundingBox();
    static protected BoundingBox tmpBB2 = new BoundingBox();
    @Override
    public void render(Entity entity, Camera renderCamera) {
        ClientPortalEntityTools.renderWithoutAnimation(entity, renderCamera);
    }

    @Override
    public void renderDuplicate(Entity entity, Camera renderCamera, Portal portal) {
        ClientPortalEntityTools.renderDuplicate(entity, renderCamera, portal);
    }

    @Override
    public boolean shouldRenderDuplicate(Entity entity, Portal portal) {
        if (entity.zone != portal.zone){
            return false;
        }
        entity.getBoundingBox(tmpBB1);
        portal.getBoundingBox(tmpBB2);
        return tmpBB1.intersects(tmpBB2);
    }

    @Override
    public void advanceAnimations(Entity entity) {
        if (!ClientPortalEntityTools.hasBeenRenderedThisFrame(entity)){
            ClientPortalEntityTools.advanceAnimations(entity);
        }
        else {
            ClientPortalEntityTools.resetIfEntityRendered(entity);
        }
    }
}
