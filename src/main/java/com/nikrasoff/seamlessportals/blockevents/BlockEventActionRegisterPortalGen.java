package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blockevents.IBlockEventAction;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public class BlockEventActionRegisterPortalGen implements IBlockEventAction {
    @Override
    public String getActionId() {
        return "seamlessportals:register_portal_gen";
    }

    @Override
    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> map) {
        this.act(zone, (BlockPosition) map.get("blockPos"));
    }

    public void act(Zone zone, BlockPosition blockPos) {
        if (SeamlessPortals.portalManager.prevPortalGenPos == null) {
            SeamlessPortals.portalManager.prevPortalGenPos = new Vector3(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
            SeamlessPortals.portalManager.prevPortalGenZone = zone.zoneId;
        }
        else {
            if (blockPos.toString().equals(SeamlessPortals.portalManager.getPrevGenBlockPos().toString()) && SeamlessPortals.portalManager.prevPortalGenZone.equals(zone.zoneId)){
                SeamlessPortals.portalManager.prevPortalGenPos = null;
                SeamlessPortals.portalManager.prevPortalGenZone = null;
                return;
            }
            SeamlessPortals.portalManager.createPortalPair(SeamlessPortals.portalManager.getPrevGenBlockPos(), blockPos, InGame.world.getZone(SeamlessPortals.portalManager.prevPortalGenZone), zone);
            BlockSetter.replaceBlock(zone, BlockState.getInstance("base:air[default]"), SeamlessPortals.portalManager.getPrevGenBlockPos(), new Queue<>());
            BlockSetter.replaceBlock(zone, BlockState.getInstance("base:air[default]"), blockPos, new Queue<>());
            SeamlessPortals.portalManager.prevPortalGenPos = null;
            SeamlessPortals.portalManager.prevPortalGenZone = null;
        }
    }
}
