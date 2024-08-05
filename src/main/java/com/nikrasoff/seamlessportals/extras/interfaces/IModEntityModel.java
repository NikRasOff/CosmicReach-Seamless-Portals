package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import finalforeach.cosmicreach.entities.Entity;

public interface IModEntityModel {
    void renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat);
    void updateAnimation(Entity entity);
}
