package com.nikrasoff.seamlessportals.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.entities.DestabiliserPulseEntity;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;
import java.util.function.Supplier;

public class EffectManager implements IEffectManager {
    public Array<IEffect> effectArray = new Array<>();
    public ObjectMap<Identifier, Supplier<IEffect>> effectMap = new ObjectMap<>();
    public void createEffect(Identifier effectId, float startingTime, Vector3 position, Zone zone, Map<String, Object> argMap){
        if (GameSingletons.client().getLocalPlayer().getZone() != zone) return;
        if (!effectMap.containsKey(effectId)) return;
        IEffect newEffect = effectMap.get(effectId).get();
        newEffect.setupEffect(startingTime, position, zone, argMap);
        effectArray.add(newEffect);
        SeamlessPortals.LOGGER.info("Effect created: " + effectId.toString());
    }
    public void render(Camera renderFromCamera){
        float delta = Gdx.graphics.getDeltaTime();
        Zone playerZone = GameSingletons.client().getLocalPlayer().getZone();
        effectArray.forEach((e) -> {
            if (!e.isInZone(playerZone)) {
                this.removeEffect(e);
                return;
            }
            e.render(delta, renderFromCamera);
        });
    }
    public void removeEffect(IEffect e){
        effectArray.removeValue(e, true);
    }
    public void cleanUpEffects(){
        effectArray.clear();
    }

    @Override
    public void registerEffectCreator(Identifier effectId, Supplier<IEffect> effectSupplier) {
        this.effectMap.put(effectId, effectSupplier);
    }

    public void registerEffects(){
        this.registerEffectCreator(DestabiliserPulseEntity.ENTITY_ID, DestabiliserPulse::new);
    }
}
