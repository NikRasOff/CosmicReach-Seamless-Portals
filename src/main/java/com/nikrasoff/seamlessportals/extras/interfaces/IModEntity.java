package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;

public interface IModEntity {
    void cosmicReach_Seamless_Portals$renderNoAnim(Camera renderCamera);

    default void cosmicReach_Seamless_Portals$renderAfterMatrixSetNoAnim(Camera renderCamera, Matrix4 customMatrix){}
}
