package com.nikrasoff.seamlessportals.rendering.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.portals.HPGPortal;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.models.PortalModel;
import com.nikrasoff.seamlessportals.rendering.models.PortalModelInstance;
import finalforeach.cosmicreach.util.Identifier;

import java.util.Arrays;

public class HPGPortalShader extends PortalShader {
    public HPGPortalShader() {
        super(Identifier.of(SeamlessPortalsConstants.MOD_ID, "shaders/textured_portal.vert.glsl"), Identifier.of(SeamlessPortalsConstants.MOD_ID, "shaders/hpg_portal.frag.glsl"));
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
        tmpVec2[0] = modelInstance.portalMeshScale.x;
        tmpVec2[1] = modelInstance.portalMeshScale.y;
        program.setUniform2fv("u_portalSize", tmpVec2, 0, 2);
        PortalModel.noiseTexture.bind(2);
        program.setUniformi("u_noiseTex", 2);
        Color c = ((HPGPortal) portal).getOutlineColor();
        tmpVec4[0] = c.r;
        tmpVec4[1] = c.g;
        tmpVec4[2] = c.b;
        tmpVec4[3] = c.a;
        program.setUniform4fv("u_outlineColor", tmpVec4, 0, 4);
        program.setUniformi("u_time", ((int) TimeUtils.millis()) / 500);
    }
}
