package com.nikrasoff.seamlessportals.blocks.blockevents;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.gameevents.ActionId;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.world.Zone;

@ActionId(
        id = "seamlessportals:register_portal_gen"
)
public class BlockActionRegisterPortalGen implements IBlockAction {
    public void act(Zone zone, BlockPosition blockPos) {
        if (SeamlessPortals.portalManager.portalGenInfo == null) {
            SeamlessPortals.portalManager.portalGenInfo = new PortalSpawnBlockInfo(zone.zoneId, new IntVector3(blockPos), blockPos.getBlockState().getSaveKey());
        }
        else {
            if (SeamlessPortals.portalManager.portalGenInfo.position.equals(new IntVector3(blockPos)) && SeamlessPortals.portalManager.portalGenInfo.zoneId.equals(zone.zoneId)){
                SeamlessPortals.portalManager.portalGenInfo = null;
                return;
            }
            SeamlessPortals.portalManager.createPortalPair(SeamlessPortals.portalManager.portalGenInfo, new PortalSpawnBlockInfo(zone.zoneId, new IntVector3(blockPos), blockPos.getBlockState().stringId));
            SeamlessPortals.portalManager.portalGenInfo = null;
        }
    }

    @Override
    public void act(BlockEventArgs blockEventArgs) {
        this.act(blockEventArgs.zone, blockEventArgs.blockPos);
    }
}
