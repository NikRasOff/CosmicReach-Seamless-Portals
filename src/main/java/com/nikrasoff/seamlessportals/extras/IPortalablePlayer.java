package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;

public interface IPortalablePlayer {
    public void portalCurrentCameraTransform(Portal portal, Vector3 offset);
}
