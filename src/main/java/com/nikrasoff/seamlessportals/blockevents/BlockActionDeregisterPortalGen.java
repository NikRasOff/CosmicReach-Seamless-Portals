package com.nikrasoff.seamlessportals.blockevents;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IntVector3;
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
        if (SeamlessPortals.portalManager.portalGenInfo == null) return;
        if (SeamlessPortals.portalManager.portalGenInfo.position.equals(new IntVector3(blockPos))){
            SeamlessPortals.portalManager.portalGenInfo = null;
        }
    }

    @Override
    public void act(BlockEventArgs blockEventArgs) {
        this.act(blockEventArgs.blockPos);
    }
}
