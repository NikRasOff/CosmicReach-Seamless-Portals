package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.world.BlockPosition;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.world.blockevents.IBlockEventAction;
import finalforeach.cosmicreach.world.blocks.BlockState;

import java.util.Map;

public class BlockEventActionRegisterPortalGen implements IBlockEventAction {
    @Override
    public String getActionId() {
        return "seamlessportals:register_portal_gen";
    }

    @Override
    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, World world, Map<String, Object> map) {
        this.act(blockState, blockEventTrigger, world, (BlockPosition) map.get("blockPos"));
    }

    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, World world, BlockPosition blockPos) {
        if (SeamlessPortals.portalManager.prevPortalGen == null) {
            SeamlessPortals.portalManager.prevPortalGen = new Vector3(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
        }
        else {
            if (blockPos.toString().equals(SeamlessPortals.portalManager.getPrevGenBlockPos().toString())){
                SeamlessPortals.portalManager.prevPortalGen = null;
                return;
            }
            SeamlessPortals.portalManager.createPortalPair(SeamlessPortals.portalManager.getPrevGenBlockPos(), blockPos);
            BlockSetter.replaceBlock(world, BlockState.getInstance("base:air[default]"), SeamlessPortals.portalManager.getPrevGenBlockPos(), new Queue<>());
            BlockSetter.replaceBlock(world, BlockState.getInstance("base:air[default]"), blockPos, new Queue<>());
            SeamlessPortals.portalManager.prevPortalGen = null;
        }
    }
}
