package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import com.nikrasoff.seamlessportals.networking.packets.PortalAnimationPacket;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.util.ArrayUtils;
import finalforeach.cosmicreach.world.EntityRegion;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;

public class PortalManager {
    public PortalSpawnBlockInfo portalGenInfo;
    public int maxOmniumFrequency = 0;

    public transient HashMap<EntityUniqueId, Portal> createdPortals = new HashMap<>();
    public ObjectMap<String, Array<PortalSpawnBlockInfo>> spacialAnchors = new ObjectMap<>();

    public PortalManager(){}

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

    public Portal getPortalWithGen(EntityUniqueId portalID, Vector3 chunkCoords, String zoneID){
        Portal result = getPortal(portalID);
        if (result != null){
            return result;
        }
        if (GameSingletons.isClient && !GameSingletons.isHost) {
            throw new RuntimeException("Use simple getPortal() on client instead of getPortalWithGen()");
        }

        EntityRegion.readChunkColumn(GameSingletons.world.getZoneCreateIfNull(zoneID), (int) chunkCoords.x, (int) chunkCoords.z, Math.floorDiv((int) chunkCoords.x, 16), Math.floorDiv((int) chunkCoords.y, 16), Math.floorDiv((int) chunkCoords.z, 16));
        result = getPortal(portalID);
        return result;
    }

    public Portal getPortal(EntityUniqueId portalID){
        return this.createdPortals.get(portalID);
    }

    public void addPortal(Portal portal){
        this.createdPortals.put(portal.uniqueId, portal);
    }

    public void removePortal(Portal portal){
        if (portal == null) return;
        this.createdPortals.remove(portal.uniqueId);
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

    public boolean createPortalPairFromGenAndAnchor(PortalSpawnBlockInfo gen, PortalSpawnBlockInfo anchor){
        BlockEntity be = GameSingletons.world.getZoneCreateIfNull(gen.zoneId).getBlockEntity(gen.position.x, gen.position.y, gen.position.z);
        if (be instanceof BlockEntityPortalGenerator portalGen){
            PortalGenPortal portal1 = PortalGenPortal.fromBlockInfo(gen, portalGen, false);
            PortalGenPortal portal2 = PortalGenPortal.fromBlockInfo(anchor, portalGen, true);
            if (portal1 == null || portal2 == null){
                this.removePortal(portal1);
                this.removePortal(portal2);
                return false;
            }
            portalGen.portalId.set(portal1.uniqueId);

            portal1.linkPortal(portal2);
            portal2.linkPortal(portal1);
            Zone zone1 = GameSingletons.world.getZoneCreateIfNull(gen.zoneId);
            Zone zone2 = GameSingletons.world.getZoneCreateIfNull(anchor.zoneId);
            Portal.portalOpenSound.playGlobalSound3D(zone1, portal1.position);
            Portal.portalOpenSound.playGlobalSound3D(zone2, portal2.position);
            zone1.addEntity(portal1);
            if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                ServerSingletons.SERVER.broadcast(zone1, new PortalAnimationPacket(portal1.uniqueId, "start"));
            }
            zone2.addEntity(portal2);
            if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                ServerSingletons.SERVER.broadcast(zone2, new PortalAnimationPacket(portal2.uniqueId, "start"));
            }
            return true;
        }

        return false;
    }
}
