package com.nikrasoff.seamlessportals.blocks.placement_rules;

import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;

import java.util.HashMap;

public class PortalGenPlacementRules extends CustomPlacementRules {

    public PortalGenPlacementRules() {
    }

    public BlockState adjustBlockState(BlockState targetBlockState, Vector3 camDirection, BlockPosition blockPos, Vector3 intersection) {
        HashMap<String, String> m = new HashMap<>();
        float xDiff = camDirection.x;
        float zDiff = camDirection.z;
        String vertical = "false";
        if (camDirection.y < -0.8F) {
            vertical = "up";
        }

        if (camDirection.y > 0.8F) {
            vertical = "down";
        }

        String direction;
        if (Math.abs(xDiff) > Math.abs(zDiff)) {
            if (xDiff < 0.0F) {
                direction = "PosX";
            } else {
                direction = "NegX";
            }
        } else if (zDiff < 0.0F) {
            direction = "PosZ";
        } else {
            direction = "NegZ";
        }

        m.put("vertical", vertical);
        m.put("direction", direction);
        targetBlockState = targetBlockState.getVariantWithParams(m);
        return targetBlockState;
    }
}
