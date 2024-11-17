package com.nikrasoff.seamlessportals.blockevents;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

@ActionId(
        id = "seamlessportals:deregister_portal_gen"
)
public class BlockActionDeregisterPortalGen implements IBlockAction {
    public void act(BlockPosition blockPos) {
        if (SeamlessPortals.portalManager.prevPortalGenPos == null) return;
        if (SeamlessPortals.portalManager.getPrevGenBlockPos() == null) return;
        if (blockPos.toString().equals(SeamlessPortals.portalManager.getPrevGenBlockPos().toString())){
            SeamlessPortals.portalManager.prevPortalGenPos = null;
            SeamlessPortals.portalManager.prevPortalGenZone = null;
        }
    }

    @Override
    public void act(BlockEventArgs blockEventArgs) {
        this.act(blockEventArgs.blockPos);
    }
}
