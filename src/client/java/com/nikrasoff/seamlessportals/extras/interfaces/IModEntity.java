package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.nikrasoff.seamlessportals.portals.Portal;

public interface IModEntity {
    void cosmicReach_Seamless_Portals$renderDuplicate(Camera playerCamera, Portal portal);
    void cosmicReach_Seamless_Portals$renderSliced(Camera playerCamera, Portal portal);
    void cosmicReach_Seamless_Portals$renderNoAnim(Camera renderCamera);
    void cosmicReach_Seamless_Portals$renderDuplicateSliced(Camera playerCamera, Portal portal);

    default void cosmicReach_Seamless_Portals$renderAfterMatrixSetNoAnim(Camera renderCamera, Matrix4 customMatrix, boolean shouldRender){}
    void cosmicReach_Seamless_Portals$advanceAnimations();
    boolean cosmicReach_Seamless_Portals$checkIfHasBeenRendered();
    void cosmicReach_Seamless_Portals$resetRender();
}
