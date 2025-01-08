package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public interface IModEntityModelInstance {
    default void cosmicReach_Seamless_Portals$renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat){}
    default void cosmicReach_Seamless_Portals$updateAnimation(){}
    void cosmicReach_Seamless_Portals$renderDuplicate(Entity entity, Camera renderCamera, Matrix4 modelMatrix, Portal portal);
}
