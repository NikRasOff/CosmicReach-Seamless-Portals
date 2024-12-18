package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.world.EntityRegion;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;
import java.util.Map;

public class PortalManager {
    public PortalSpawnBlockInfo portalGenInfo;
    public int maxOmniumFrequency = 0;
    public int maxPortalID = 0;

    public transient HashMap<Integer, Portal> createdPortals = new HashMap<>();
    public ObjectMap<String, Array<PortalSpawnBlockInfo>> spacialAnchors = new ObjectMap<>();

    public PortalManager(){}

    public int getNextPortalID(){
        this.maxPortalID += 1;
        return this.maxPortalID - 1;
    }

    public int getNextOmniumFrequency(){
        this.maxOmniumFrequency += 1;
        return this.maxOmniumFrequency - 1;
    }

    public void registerSpacialAnchor(int frequency, PortalSpawnBlockInfo info){
        if (!this.spacialAnchors.containsKey(String.valueOf(frequency))) this.spacialAnchors.put(String.valueOf(frequency), new Array<>());
        this.spacialAnchors.get(String.valueOf(frequency)).add(info);
    }

    public void deregisterSpacialAnchor(int frequency, IntVector3 position){
        if (!this.spacialAnchors.containsKey(String.valueOf(frequency))) return;
        for (PortalSpawnBlockInfo info : this.spacialAnchors.get(String.valueOf(frequency))){
            if (info.position.equals(position)) this.spacialAnchors.get(String.valueOf(frequency)).removeValue(info, true);
        }
        if (this.spacialAnchors.get(String.valueOf(frequency)).isEmpty()) this.spacialAnchors.remove(String.valueOf(frequency));
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

    public boolean createPortalPair(PortalSpawnBlockInfo gen1, PortalSpawnBlockInfo gen2, BlockEntityPortalGenerator portalGen){
        Portal portal1 = Portal.fromBlockInfo(gen1, portalGen.portalSize);
        Portal portal2 = Portal.fromBlockInfo(gen2, portalGen.portalSize);

        Vector3 nudgeCoords1 = new Vector3(portalGen.entrancePortalOffset.x, portalGen.entrancePortalOffset.y, 0).mul(portal1.getRotationMatrix());
        portal1.position.add(nudgeCoords1);
        if (!portal1.figureOutPlacement(portalGen.zone, portal1.portalSize.x / 2 - portalGen.entrancePortalOffset.x, portal1.portalSize.x / 2 + portalGen.entrancePortalOffset.x, portal1.portalSize.y / 2 - portalGen.entrancePortalOffset.y, portal1.portalSize.y / 2 + portalGen.entrancePortalOffset.y)){
            SeamlessPortals.portalManager.removePortal(portal1);
            return false;
        }
        portalGen.portalId = portal1.getPortalID();

        Vector3 nudgeCoords2 = new Vector3(portalGen.exitPortalOffset.x, portalGen.exitPortalOffset.y, 0).mul(portal2.getRotationMatrix());
        portal2.position.add(nudgeCoords2);
        if (!portal2.figureOutPlacement(portalGen.zone, portal2.portalSize.x / 2 - portalGen.exitPortalOffset.x, portal2.portalSize.x / 2 + portalGen.exitPortalOffset.x, portal2.portalSize.y / 2 - portalGen.exitPortalOffset.y, portal2.portalSize.y / 2 + portalGen.exitPortalOffset.y)){
            portalGen.portalId = -1;
            SeamlessPortals.portalManager.removePortal(portal2);
            SeamlessPortals.portalManager.removePortal(portal1);
            return false;
        }
        portal2.viewDirection.scl(-1);

        portal1.linkPortal(portal2);
        portal2.linkPortal(portal1);
        Zone zone1 = GameSingletons.world.getZoneCreateIfNull(gen1.zoneId);
        Zone zone2 = GameSingletons.world.getZoneCreateIfNull(gen2.zoneId);
        zone1.addEntity(portal1);
        zone2.addEntity(portal2);

        return true;
    }

    public void printExistingIDs(){
        System.out.println("Existing portal IDs: ");
        for (int i : this.createdPortals.keySet()){
            System.out.println(i);
        }
    }
}
