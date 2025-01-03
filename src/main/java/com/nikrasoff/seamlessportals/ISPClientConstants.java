package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;

public interface ISPClientConstants {
    IEntityModelInstance getNewPortalModelInstance();
    IEntityModelInstance getNewPulseModelInstance();
    void animateCameraTurning(Vector3 originalPos, Vector3 newPos, Portal portal);
}
