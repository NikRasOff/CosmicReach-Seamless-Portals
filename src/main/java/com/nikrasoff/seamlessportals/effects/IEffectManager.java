package com.nikrasoff.seamlessportals.effects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;
import java.util.function.Supplier;

public interface IEffectManager {
    void createEffect(Identifier effectId, float startingTime, Vector3 position, Zone zone, Map<String, Object> argMap);
    void createEffect(Identifier effectId, float startingTime, Vector3 position, Zone zone, CRBinDeserializer deserial);
    void removeEffect(IEffect effect);
    void cleanUpEffects();
    void registerEffectCreator(Identifier effectId, Supplier<IEffect> effectSupplier);
    void registerEffects();
    void render(Camera renderFromCamera);
}
