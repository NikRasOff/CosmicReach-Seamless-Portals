package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

@ActionId(
        id = "seamlessportals:register_portal_gen"
)
public class BlockActionRegisterPortalGen implements IBlockAction {
    public void act(Zone zone, BlockPosition blockPos) {
        if (SeamlessPortals.portalManager.prevPortalGenPos == null || SeamlessPortals.portalManager.getPrevGenBlockPos() == null) {
            SeamlessPortals.portalManager.prevPortalGenPos = new Vector3(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
            SeamlessPortals.portalManager.prevPortalGenZone = zone.zoneId;
        }
        else {
            if (blockPos.toString().equals(SeamlessPortals.portalManager.getPrevGenBlockPos().toString()) && SeamlessPortals.portalManager.prevPortalGenZone.equals(zone.zoneId)){
                SeamlessPortals.portalManager.prevPortalGenPos = null;
                SeamlessPortals.portalManager.prevPortalGenZone = null;
                return;
            }
            SeamlessPortals.portalManager.createPortalPair(SeamlessPortals.portalManager.getPrevGenBlockPos(), blockPos, GameSingletons.world.getZoneCreateIfNull(SeamlessPortals.portalManager.prevPortalGenZone), zone);
            BlockSetter.get().replaceBlock(zone, BlockState.getInstance("base:air[default]"), SeamlessPortals.portalManager.getPrevGenBlockPos());
            BlockSetter.get().replaceBlock(zone, BlockState.getInstance("base:air[default]"), blockPos);
            SeamlessPortals.portalManager.prevPortalGenPos = null;
            SeamlessPortals.portalManager.prevPortalGenZone = null;
        }
    }

    @Override
    public void act(BlockEventArgs blockEventArgs) {
        this.act(blockEventArgs.zone, blockEventArgs.blockPos);
    }
}
