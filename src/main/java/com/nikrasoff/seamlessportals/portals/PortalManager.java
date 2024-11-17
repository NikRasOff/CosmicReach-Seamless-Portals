package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.ZoneLoaders;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.networking.server.ServerZoneLoader;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.EntityRegion;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;

public class PortalManager {
    public int saveDataVersion = 1;
    public String prevPortalGenZone;
    public Vector3 prevPortalGenPos;
    public int maxOmniumFrequency = 0;
    public int maxPortalID = 0;

    public transient HashMap<Integer, Portal> createdPortals = new HashMap<>();
    public HashMap<Integer, Array<IntVector3>> spacialAnchors = new HashMap<>();

    public PortalManager(){}
    
    public BlockPosition getPrevGenBlockPos(){
        if (this.prevPortalGenPos == null) return null;
        Zone curZone = GameSingletons.world.getZoneCreateIfNull(this.prevPortalGenZone);
        Chunk c = curZone.getChunkAtBlock((int) this.prevPortalGenPos.x, (int) this.prevPortalGenPos.y, (int) this.prevPortalGenPos.z);
        if (c == null){

//            WorldLoader.INSTANCE.getChunkColumn(curZone, Math.floorDiv((int) this.prevPortalGenPos.x, 16), Math.floorDiv((int) this.prevPortalGenPos.y, 256) * 16, Math.floorDiv((int) this.prevPortalGenPos.z, 16), true);
            c = curZone.getChunkAtBlock((int) this.prevPortalGenPos.x, (int) this.prevPortalGenPos.y, (int) this.prevPortalGenPos.z);
            if (c == null){
                SeamlessPortals.LOGGER.warn("Couldn't get previous portal generator location");
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

    public void registerSpacialAnchor(IntVector3 position, int frequency){
        if (!this.spacialAnchors.containsKey(frequency)) this.spacialAnchors.put(frequency, new Array<>());
        this.spacialAnchors.get(frequency).add(position);
    }

    public void deregisterSpacialAnchor(IntVector3 position, int frequency){
        if (!this.spacialAnchors.containsKey(frequency)) return;
        this.spacialAnchors.get(frequency).removeValue(position, false);
        if (this.spacialAnchors.get(frequency).isEmpty()) this.spacialAnchors.remove(frequency);
    }

    public Portal getPortalWithGen(int portalID, Vector3 chunkCoords, String zoneID){
        Portal result = getPortal(portalID);
        if (result != null){
            return result;
        }
        EntityRegion.readChunkColumn(GameSingletons.world.getZoneIfExists(zoneID), (int) chunkCoords.x, (int) chunkCoords.z, Math.floorDiv((int) chunkCoords.x, 16), Math.floorDiv((int) chunkCoords.y, 16), Math.floorDiv((int) chunkCoords.z, 16));
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
        zone1.addEntity(portal1);
        zone2.addEntity(portal2);
    }

    public void printExistingIDs(){
        System.out.println("Existing portal IDs: ");
        for (int i : this.createdPortals.keySet()){
            System.out.println(i);
        }
    }
}
