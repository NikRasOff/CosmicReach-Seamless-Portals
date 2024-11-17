package com.nikrasoff.seamlessportals.effects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public interface IEffect {
    void setupEffect(float lifetime, Vector3 position, Zone zone, Map<String, Object> argMap);
    void render(float delta, Camera playerCamera);
    boolean isInZone(Zone zone);
}
