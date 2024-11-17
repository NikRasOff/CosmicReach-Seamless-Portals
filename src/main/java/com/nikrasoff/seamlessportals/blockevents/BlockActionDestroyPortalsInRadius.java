package com.nikrasoff.seamlessportals.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.entities.DestabiliserPulseEntity;
import finalforeach.cosmicreach.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

@ActionId(
        id = "seamlessportals:destroy_portals_in_radius"
)
public class BlockActionDestroyPortalsInRadius implements IBlockAction {
    public float radius;

    public BlockActionDestroyPortalsInRadius(){
    }

    public void act(Zone zone, BlockPosition blockPos) {
        Vector3 destroyOrigin = new Vector3(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
        destroyOrigin.add(new Vector3(0.5f, 0.5f, 0.5f));
        DestabiliserPulseEntity pulse = (DestabiliserPulseEntity) EntityCreator.get(DestabiliserPulseEntity.ENTITY_ID.toString());
        pulse.prepareForSpawn(radius, destroyOrigin, zone);
        zone.addEntity(pulse);
    }


    @Override
    public void act(BlockEventArgs blockEventArgs) {
        this.act(blockEventArgs.zone, blockEventArgs.blockPos);
    }
}
