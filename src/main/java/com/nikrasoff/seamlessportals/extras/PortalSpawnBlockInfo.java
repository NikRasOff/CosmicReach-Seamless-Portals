package com.nikrasoff.seamlessportals.extras;

import finalforeach.cosmicreach.world.Zone;

public class PortalSpawnBlockInfo {
    public String zoneId;
    public IntVector3 position = new IntVector3();
    public String blockState;
    public PortalSpawnBlockInfo(){
    }
    public PortalSpawnBlockInfo(String zoneId, IntVector3 position, String blockState){
        this.zoneId = zoneId;
        this.position = position;
        this.blockState = blockState;
    }
}
