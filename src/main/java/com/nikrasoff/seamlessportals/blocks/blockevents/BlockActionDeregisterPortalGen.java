package com.nikrasoff.seamlessportals.blocks.blockevents;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.gameevents.ActionId;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.actions.IBlockAction;

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
