package com.nikrasoff.seamlessportals;

import finalforeach.cosmicreach.world.BlockPosition;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.world.blockevents.IBlockEventAction;
import finalforeach.cosmicreach.world.blocks.BlockState;

import java.util.Map;

public class BlockEventActionDeregisterPortalGen implements IBlockEventAction {
    @Override
    public String getActionId() {
        return "seamlessportals:deregister_portal_gen";
    }

    @Override
    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, World world, Map<String, Object> map) {
        this.act((BlockPosition) map.get("blockPos"));
    }
    public void act(BlockPosition blockPos) {
        if (SeamlessPortals.portalManager.prevPortalGen == null) return;
        if (blockPos.toString().equals(SeamlessPortals.portalManager.getPrevGenBlockPos().toString())){
            SeamlessPortals.portalManager.prevPortalGen = null;
        }
    }
}
