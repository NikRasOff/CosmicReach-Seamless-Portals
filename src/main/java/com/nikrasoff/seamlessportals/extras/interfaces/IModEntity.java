package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;

public interface IModEntity {
    void renderNoAnim(Camera renderCamera);

    default void renderAfterMatrixSetNoAnim(Camera renderCamera, Matrix4 customMatrix){};
}
