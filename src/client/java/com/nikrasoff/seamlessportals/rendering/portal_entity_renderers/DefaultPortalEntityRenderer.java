package com.nikrasoff.seamlessportals.rendering.portal_entity_renderers;

import com.badlogic.gdx.graphics.Camera;
import com.nikrasoff.seamlessportals.api.IPortalEntityRenderer;
import com.nikrasoff.seamlessportals.extras.ClientPortalEntityTools;
import finalforeach.cosmicreach.entities.Entity;

public class DefaultPortalEntityRenderer implements IPortalEntityRenderer {
    @Override
    public void render(Entity entity, Camera renderCamera) {
        ClientPortalEntityTools.renderWithoutAnimation(entity, renderCamera);
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
