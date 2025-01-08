package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.items.ItemStack;

public interface ISliceablePuzzleModel {
    void renderAsSlicedEntity(Vector3 position, ItemStack stack, Camera renderCamera, Matrix4 modelMatrix, Portal portal);
}
