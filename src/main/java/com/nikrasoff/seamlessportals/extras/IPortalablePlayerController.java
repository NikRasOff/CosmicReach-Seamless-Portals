package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;

public interface IPortalablePlayerController {
    public void portalCurrentCameraTransform(Portal portal, Vector3 offset);
}
