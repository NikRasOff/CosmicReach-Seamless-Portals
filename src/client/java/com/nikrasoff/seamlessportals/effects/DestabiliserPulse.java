package com.nikrasoff.seamlessportals.effects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.OrderedMap;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public class DestabiliserPulse extends PulseEffect{
    public float destroyRadius;

    @Override
    public void setupEffect(float lifetime, Vector3 position, Zone zone, Map<String, Object> argMap) {
        super.setupEffect(lifetime, position, zone, argMap);
        this.destroyRadius = (float) argMap.get("radius");
        this.setupPulseEffect(new Vector3(0, 0, 0), new Vector3(destroyRadius, destroyRadius, destroyRadius).scl(2), new Color(1, 0, 0, 0),
                new Color(1, 0, 0, 0.5f), 1, new Vector3(destroyRadius, destroyRadius, destroyRadius).scl(2), new Color(1, 0, 0, 0), 0.5f);
    }
}