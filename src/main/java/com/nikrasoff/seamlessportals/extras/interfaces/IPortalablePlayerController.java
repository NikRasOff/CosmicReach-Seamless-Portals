package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;

public interface IPortalablePlayerController {
    void portalCurrentCameraTransform(Portal portal, Vector3 offset);

    void resetPlayerCameraUp();
}
