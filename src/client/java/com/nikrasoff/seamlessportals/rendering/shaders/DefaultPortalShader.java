package com.nikrasoff.seamlessportals.rendering.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.models.PortalModel;
import com.nikrasoff.seamlessportals.rendering.models.PortalModelInstance;
import finalforeach.cosmicreach.util.Identifier;

public class DefaultPortalShader extends PortalShader {
    public DefaultPortalShader() {
        super(Identifier.of(SeamlessPortalsConstants.MOD_ID, "shaders/default.vert.glsl"), Identifier.of(SeamlessPortalsConstants.MOD_ID, "shaders/portal.frag.glsl"));
    }

    @Override
    public void setUniforms(PortalModelInstance modelInstance, Portal portal) {
        Vector2 screenSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        tmpVec2[0] = screenSize.x;
        tmpVec2[1] = screenSize.y;
        program.setUniform2fv("screenSize", tmpVec2, 0, 2);
        tmpVec4[0] = modelInstance.colorOverlay.r;
        tmpVec4[1] = modelInstance.colorOverlay.g;
        tmpVec4[2] = modelInstance.colorOverlay.b;
        tmpVec4[3] = modelInstance.colorOverlay.a;
        program.setUniform4fv("overlayColor", tmpVec4, 0, 4);
        modelInstance.portalTexture.bind(1);
        program.setUniformi("screenTex", 1);
    }
}
