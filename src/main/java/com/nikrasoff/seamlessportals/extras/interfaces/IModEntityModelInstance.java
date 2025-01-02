package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import finalforeach.cosmicreach.entities.Entity;

public interface IModEntityModelInstance {
    void cosmicReach_Seamless_Portals$renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat);
    void cosmicReach_Seamless_Portals$nullifyAnimation();
    void cosmicReach_Seamless_Portals$updateAnimation();
}
