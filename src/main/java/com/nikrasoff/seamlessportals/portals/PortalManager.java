package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.WorldLoader;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.EntityRegion;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;
import java.util.WeakHashMap;

public class PortalManager {
    public int saveDataVersion = 1;
    public String prevPortalGenZone;
    public Vector3 prevPortalGenPos;
    public int maxOmniumFrequency = 0;
    public int maxPortalID = 0;

    public transient HashMap<Integer, Portal> createdPortals = new HashMap<>();

    public PortalManager(){}
    
    public BlockPosition getPrevGenBlockPos(){
        if (this.prevPortalGenPos == null) return null;
        Zone cur_zone = InGame.world.getZone(this.prevPortalGenZone);
        Chunk c = cur_zone.getChunkAtBlock((int) this.prevPortalGenPos.x, (int) this.prevPortalGenPos.y, (int) this.prevPortalGenPos.z);
        if (c == null){
            WorldLoader.INSTANCE.getChunkColumn(cur_zone, Math.floorDiv((int) this.prevPortalGenPos.x, 16), Math.floorDiv((int) this.prevPortalGenPos.y, 256) * 16, Math.floorDiv((int) this.prevPortalGenPos.z, 16), true);
            c = cur_zone.getChunkAtBlock((int) this.prevPortalGenPos.x, (int) this.prevPortalGenPos.y, (int) this.prevPortalGenPos.z);
            if (c == null){
                SeamlessPortals.LOGGER.warn("Couldn't gen previous portal generator location");
                return null;
            }
        }
        return new BlockPosition(c, (int) (this.prevPortalGenPos.x - c.blockX), (int) (this.prevPortalGenPos.y - c.blockY), (int) (this.prevPortalGenPos.z - c.blockZ));
    }

    public int getNextPortalID(){
        this.maxPortalID += 1;
        return this.maxPortalID - 1;
    }

    public int getNextOmniumFrequency(){
        this.maxOmniumFrequency += 1;
        return this.maxOmniumFrequency - 1;
    }

    public Portal getPortalWithGen(int portalID, Vector3 chunkCoords, String zoneID){
        Portal result = getPortal(portalID);
        if (result != null){
            return result;
        }
        EntityRegion.readChunkColumn(InGame.world.getZone(zoneID), (int) chunkCoords.x, (int) chunkCoords.z, Math.floorDiv((int) chunkCoords.x, 16), Math.floorDiv((int) chunkCoords.y, 16), Math.floorDiv((int) chunkCoords.z, 16));
        result = getPortal(portalID);
        return result;
    }

    public Portal getPortal(int portalID){
        return this.createdPortals.get(portalID);
    }

    public void addPortal(Portal portal){
        this.createdPortals.put(portal.getPortalID(), portal);
    }

    public void removePortal(Portal portal){
        this.createdPortals.remove(portal.getPortalID());
    }

    public void createPortalPair(BlockPosition portalPos1, BlockPosition portalPos2, Zone zone1, Zone zone2){
        Portal portal1 = Portal.fromBlockPos(new Vector2(3, 3), portalPos1, zone1);
        Portal portal2 = Portal.fromBlockPos(new Vector2(3, 3), portalPos2, zone2);
        portal1.linkPortal(portal2);
        portal2.linkPortal(portal1);
        zone1.allEntities.add(portal1);
        zone2.allEntities.add(portal2);
    }

    public void printExistingIDs(){
        System.out.println("Existing portal IDs: ");
        for (int i : this.createdPortals.keySet()){
            System.out.println(i);
        }
    }
}
