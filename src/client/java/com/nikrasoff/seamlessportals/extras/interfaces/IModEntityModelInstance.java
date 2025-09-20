package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public interface IModEntityModelInstance {
    default void cosmicReach_Seamless_Portals$renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat, boolean shouldRender){}
    default void cosmicReach_Seamless_Portals$updateAnimation(Entity entity, Vector3 renderPos){}
    void cosmicReach_Seamless_Portals$renderSliced(Entity entity, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate);
    default void cosmicReach_Seamless_Portals$flagForTeleporting(Portal portal) {}
}
