package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockPosition;

public record RaycastOutput(Vector3 hitPos, DirectionVector hitNormal, BlockPosition hitBlock) {
}
