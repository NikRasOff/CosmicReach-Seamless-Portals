package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;

public interface ISPClientConstants {
    IEntityModelInstance getNewPortalModelInstance();
    void animateCameraTurning(Vector3 originalPos, Vector3 newPos, Portal portal);
    void flagEntityModelInstanceForTeleporting(Entity entity, Portal portal);
}
