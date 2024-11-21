package com.nikrasoff.seamlessportals.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public class DestabiliserPulse extends PulseEffect{
    public float destroyRadius;

    @Override
    public void setupEffect(float lifetime, Vector3 position, Zone zone) {
        this.setupPulseEffect(new Vector3(0, 0, 0), new Vector3(destroyRadius, destroyRadius, destroyRadius).scl(2), new Color(1, 0, 0, 0),
                new Color(1, 0, 0, 0.5f), 1, new Vector3(destroyRadius, destroyRadius, destroyRadius).scl(2), new Color(1, 0, 0, 0), 0.5f);
        super.setupEffect(lifetime, position, zone);
    }

    @Override
    public void read(CRBinDeserializer deserial) {
        super.read(deserial);
        this.destroyRadius = deserial.readFloat("radius", 5);
    }

    @Override
    public void applyArguments(Map<String, Object> argMap) {
        super.applyArguments(argMap);
        this.destroyRadius = (float) argMap.get("radius");
    }
}
