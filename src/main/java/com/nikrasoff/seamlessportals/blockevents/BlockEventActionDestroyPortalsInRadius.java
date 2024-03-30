package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.OrderedMap;
import com.nikrasoff.seamlessportals.effects.DestabiliserPulse;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blockevents.IBlockEventAction;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public class BlockEventActionDestroyPortalsInRadius implements IBlockEventAction {
    @Override
    public String getActionId() {
        return "seamlessportals:destroy_portals_in_radius";
    }

    @Override
    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> map) {
        this.act(blockEventTrigger, zone, (BlockPosition) map.get("blockPos"));
    }

    public void act(BlockEventTrigger blockEventTrigger, Zone zone, BlockPosition blockPos) {
        OrderedMap<String, Object> p = blockEventTrigger.getParams();
        float destroyRadius = (float) p.get("radius");
        Vector3 destroyOrigin = new Vector3(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
        destroyOrigin.add(new Vector3(0.5f, 0.5f, 0.5f));
        new DestabiliserPulse(destroyOrigin, destroyRadius, zone);
    }
}
