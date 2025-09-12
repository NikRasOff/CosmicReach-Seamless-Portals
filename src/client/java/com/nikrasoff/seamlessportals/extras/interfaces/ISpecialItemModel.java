package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

// For models that I want to render differently though portals
// More specifically, the block item model
public interface ISpecialItemModel {
    void renderAsItemEntityThroughPortal(Vector3 worldPosition, Camera worldCamera, Matrix4 modelMat);
}
