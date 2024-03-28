package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import finalforeach.cosmicreach.world.BlockPosition;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.world.blockevents.IBlockEventAction;
import finalforeach.cosmicreach.world.blocks.BlockState;

import java.util.Map;

public class BlockEventActionDestroyPortalsInRadius implements IBlockEventAction {
    @Override
    public String getActionId() {
        return "seamlessportals:destroy_portals_in_radius";
    }

    @Override
    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, World world, Map<String, Object> map) {
        this.act(blockState, blockEventTrigger, world, (BlockPosition) map.get("blockPos"));
    }

    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, World world, BlockPosition blockPos) {
        OrderedMap<String, Object> p = blockEventTrigger.getParams();
        float destroyRadius = (float) p.get("radius");
        Vector3 destroyOrigin = new Vector3(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
        destroyOrigin.add(new Vector3(0.5f, 0.5f, 0.5f));
        new DestabiliserPulse(destroyOrigin,destroyRadius);
    }
}
