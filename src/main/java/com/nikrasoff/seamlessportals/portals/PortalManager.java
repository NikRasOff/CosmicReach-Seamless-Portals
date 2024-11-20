package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.world.EntityRegion;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;

public class PortalManager {
    public PortalSpawnBlockInfo portalGenInfo;
    public int maxOmniumFrequency = 0;
    public int maxPortalID = 0;

    public transient HashMap<Integer, Portal> createdPortals = new HashMap<>();
    public HashMap<Integer, Array<IntVector3>> spacialAnchors = new HashMap<>();

    public PortalManager(){}

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
        if (GameSingletons.isClient && !GameSingletons.isHost) {
            throw new RuntimeException("Don't try to just grab a portal on client!");
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

    public void createPortalPair(PortalSpawnBlockInfo gen1, PortalSpawnBlockInfo gen2){
        Portal portal1 = Portal.fromBlockInfo(gen1, new Vector2(3, 3));
        Portal portal2 = Portal.fromBlockInfo(gen2, new Vector2(3, 3));
        portal1.linkPortal(portal2);
        portal2.linkPortal(portal1);
        Zone zone1 = GameSingletons.world.getZoneCreateIfNull(gen1.zoneId);
        Zone zone2 = GameSingletons.world.getZoneCreateIfNull(gen2.zoneId);
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
