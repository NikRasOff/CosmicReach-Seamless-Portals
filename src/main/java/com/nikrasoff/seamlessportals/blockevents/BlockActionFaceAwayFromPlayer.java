package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.extras.DirectionVector;
import finalforeach.cosmicreach.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

@ActionId(
        id = "seamlessportals:face_away_from_player"
)
public class BlockActionFaceAwayFromPlayer implements IBlockAction {

    public boolean vertical = false;
    public boolean invert = false;

    public void act(BlockState blockState, Zone zone, BlockPosition targetBlockPosition, Player player) {
        Entity playerEntity = player.getEntity();
        Vector3 playerEntityViewDir = playerEntity.viewDirection.cpy();
        if (invert) playerEntityViewDir.scl(-1);
        String directionString = "";
        String horDirString;

        horDirString = DirectionVector.getClosestHorizontalDirection(playerEntityViewDir).getName();

        if (this.vertical && !DirectionVector.getClosestDirection(playerEntityViewDir).getName().equals(horDirString)){
            directionString = DirectionVector.getClosestVerticalDirection(playerEntityViewDir).getName();
        }

        directionString += horDirString;

        String newSaveKey = getNewSaveKey(blockState, directionString);
        BlockState newBlockState = BlockState.getInstance(newSaveKey, MissingBlockStateResult.MISSING_OBJECT);
        BlockSetter.get().replaceBlock(zone, newBlockState, targetBlockPosition);
    }

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

    @Override
    public void act(BlockEventArgs blockEventArgs) {
        this.act(blockEventArgs.srcBlockState, blockEventArgs.zone, blockEventArgs.blockPos, blockEventArgs.srcPlayer);
    }
}
