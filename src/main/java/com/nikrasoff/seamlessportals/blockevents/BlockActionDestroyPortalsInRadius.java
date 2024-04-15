package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.nikrasoff.seamlessportals.effects.DestabiliserPulse;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

@ActionId(
        id = "seamlessportals:destroy_portals_in_radius"
)
public class BlockActionDestroyPortalsInRadius implements IBlockAction {
    public float radius;

    public BlockActionDestroyPortalsInRadius(){
    }

    @Override
    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> map) {
        this.act(zone, (BlockPosition) map.get("blockPos"));
    }

    public void act(Zone zone, BlockPosition blockPos) {
        Vector3 destroyOrigin = new Vector3(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
        destroyOrigin.add(new Vector3(0.5f, 0.5f, 0.5f));
        new DestabiliserPulse(destroyOrigin, radius, zone);
    }


}
