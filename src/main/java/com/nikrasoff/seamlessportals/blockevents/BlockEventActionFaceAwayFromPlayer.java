package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blockevents.IBlockEventAction;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public class BlockEventActionFaceAwayFromPlayer implements IBlockEventAction {
    @Override
    public String getActionId() {
        return "seamlessportals:face_away_from_player";
    }

    @Override
    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> map) {
        this.act(blockState, zone, (BlockPosition) map.get("blockPos"));
    }

    public void act(BlockState blockState, Zone zone, BlockPosition targetBlockPosition) {
        Entity playerEntity = InGame.getLocalPlayer().getEntity();
        Vector3 playerEntityViewDir = playerEntity.viewDirection;
        String directionString = "";
        float highestDot = -90;

        float curDitDot = playerEntityViewDir.dot(new Vector3(1, 0, 0));
        if (curDitDot > highestDot) {
            highestDot = curDitDot;
            directionString = "posX";
        }
        curDitDot = playerEntityViewDir.dot(new Vector3(-1, 0, 0));
        if (curDitDot > highestDot) {
            highestDot = curDitDot;
            directionString = "negX";
        }
        curDitDot = playerEntityViewDir.dot(new Vector3(0, 0, 1));
        if (curDitDot > highestDot) {
            highestDot = curDitDot;
            directionString = "posZ";
        }
        curDitDot = playerEntityViewDir.dot(new Vector3(0, 0, -1));
        if (curDitDot > highestDot) {
            directionString = "negZ";
        }

        String[] blockStateID = blockState.stringId.split(",");
        StringBuilder newBlockStateID = new StringBuilder();
        for (String idString : blockStateID){
            if (!newBlockStateID.isEmpty()){
                newBlockStateID.append(",");
            }
            String[] idSplit = idString.split("=");

            newBlockStateID.append(idSplit[0]);
            if (idSplit.length > 1) {
                newBlockStateID.append("=");
                if (idSplit[0].equals("facing")) {
                    newBlockStateID.append(directionString);
                } else {
                    newBlockStateID.append(idSplit[1]);
                }
            }
        }

        String newSaveKey = blockState.getBlockId() + "[" + newBlockStateID + "]";
        BlockState newBlockState = BlockState.getInstance(newSaveKey);
        BlockSetter.replaceBlock(zone, newBlockState, targetBlockPosition, new Queue<>());
    }
}
