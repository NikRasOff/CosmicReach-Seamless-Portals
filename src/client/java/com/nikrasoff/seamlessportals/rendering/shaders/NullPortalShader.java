package com.nikrasoff.seamlessportals.rendering.shaders;

import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.models.PortalModelInstance;
import finalforeach.cosmicreach.util.Identifier;

public class NullPortalShader extends PortalShader {
    public NullPortalShader() {
        super(Identifier.of(SeamlessPortalsConstants.MOD_ID, "shaders/default.vert.glsl"), Identifier.of(SeamlessPortalsConstants.MOD_ID, "shaders/null_portal.frag.glsl"));
    }

    @Override
    public void setUniforms(PortalModelInstance modelInstance, Portal portal) {
        tmpVec4[0] = modelInstance.colorOverlay.r;
        tmpVec4[1] = modelInstance.colorOverlay.g;
        tmpVec4[2] = modelInstance.colorOverlay.b;
        tmpVec4[3] = modelInstance.colorOverlay.a;
        program.setUniform4fv("overlayColor", tmpVec4, 0, 4);
    }
}
