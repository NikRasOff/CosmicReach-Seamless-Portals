package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;
import com.nikrasoff.seamlessportals.extras.DirectionVector;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.world.Zone;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@ActionId(
        id = "seamlessportals:face_away_from_player"
)
public class BlockActionFaceAwayFromPlayer implements IBlockAction {
    @Override
    public void act(BlockState blockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> map) {
        this.act(blockState, zone, (BlockPosition) map.get("blockPos"));
    }

    public void act(BlockState blockState, Zone zone, BlockPosition targetBlockPosition) {
        Entity playerEntity = InGame.getLocalPlayer().getEntity();
        Vector3 playerEntityViewDir = playerEntity.viewDirection;
        String directionString = "";
        String horDirString;

        horDirString = new String(DirectionVector.getClosestHorizontalDirection(playerEntityViewDir).getName());

        if (!DirectionVector.getClosestDirection(playerEntityViewDir).getName().equals(horDirString)){
            directionString = DirectionVector.getClosestVerticalDirection(playerEntityViewDir).getName();
        }

        directionString += horDirString;

        String newSaveKey = getNewSaveKey(blockState, directionString);
        BlockState newBlockState = BlockState.getInstance(newSaveKey);
        BlockSetter.replaceBlock(zone, newBlockState, targetBlockPosition, new Queue<>());
    }

    @NotNull
    private static String getNewSaveKey(BlockState blockState, String directionString) {
        if (!blockState.stringId.contains("facing=")){
            return blockState.getBlockId() + "[" + blockState.stringId + ",facing=" + directionString + "]";
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

        return blockState.getBlockId() + "[" + newBlockStateID + "]";
    }
}
