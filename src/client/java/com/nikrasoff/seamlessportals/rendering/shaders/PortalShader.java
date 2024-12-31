package com.nikrasoff.seamlessportals.rendering.shaders;

import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.models.PortalModelInstance;
import finalforeach.cosmicreach.util.Identifier;

public abstract class PortalShader extends TwoSidedShader {

    protected static final float[] tmpVec2 = new float[2];
    protected static final float[] tmpVec4 = new float[4];
    public PortalShader(Identifier vert, Identifier frag) {
        super(vert, frag);
    }

    public abstract void setUniforms(PortalModelInstance modelInstance, Portal portal);
}
