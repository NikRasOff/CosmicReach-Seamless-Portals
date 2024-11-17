package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;

public interface IPortalablePlayerController {
    void cosmicReach_Seamless_Portals$portalCurrentCameraTransform(Portal portal, Vector3 offset);

    void cosmicReach_Seamless_Portals$resetPlayerCameraUp();
}
