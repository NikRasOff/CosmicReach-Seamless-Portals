package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.networking.packets.PortalAnimationPacket;
import com.nikrasoff.seamlessportals.networking.packets.UpdatePortalPacket;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.world.Zone;

import java.util.Arrays;

public class HPGPortal extends Portal {
    public static final String[] defaultBlacklist = {
            "base:air",
            "base:water"
    };
    public static final Color primaryPortalColor = Color.CYAN;
    public static final Color secondaryPortalColor = Color.ORANGE;
    @CRBSerialized
    private boolean isSecond = false;
    @CRBSerialized
    private boolean isUnstable = false;

    public static HPGPortal readPortal(CRBinDeserializer deserializer){
        HPGPortal portal = new HPGPortal();
        if (deserializer != null) {
            portal.read(deserializer);
            String zoneId = deserializer.readString("zoneId");
            if (zoneId == null){
                zoneId = GameSingletons.world.defaultZoneId;
            }
            SeamlessPortals.portalManager.addPortal(portal);
            Portal lPortal;
            if (GameSingletons.isHost){
                lPortal = SeamlessPortals.portalManager.getPortalWithGen(portal.linkedPortalID, portal.linkedPortalChunkCoords, zoneId);
            }
            else {
                lPortal = SeamlessPortals.portalManager.getPortal(portal.linkedPortalID);
            }
            if (lPortal != null){
                portal.linkPortal(lPortal);
                lPortal.linkPortal(portal);
            }
            portal.calculateLocalBB();
            portal.calculateMeshBB();
        }
        return portal;
    }

    public HPGPortal(){
        super("seamlessportals:entity_hpg_portal");
        this.canDespawn = false;
        this.hasGravity = false;
        this.noClip = true;
        IPortalableEntity.setIgnorePortals((IPortalableEntity) this, true);
        if (GameSingletons.isClient){
            this.modelInstance = SeamlessPortals.clientConstants.getNewPortalModelInstance();
        }
    }

    private HPGPortal(Vector2 size, Vector3 viewDir, Vector3 upDir, Vector3 portalPos){
        this();
        SeamlessPortals.portalManager.addPortal(this);

        setPosition(portalPos.x, portalPos.y, portalPos.z);

        this.viewDirection.set(viewDir);
        this.upVector.set(upDir);

        this.portalSize.set(size.x, size.y, 0);
        this.viewPositionOffset.set(0, 0, 0);
        this.calculateLocalBB();
        this.calculateMeshBB();
        if (GameSingletons.isClient){
            this.modelInstance.setCurrentAnimation("start");
        }
    }

    public Color getOutlineColor(){
        return this.isSecond ? secondaryPortalColor : primaryPortalColor;
    }

    public static HPGPortal createNewPortal(Vector2 size, Vector3 viewDir, Vector3 upDir, Vector3 portalPos, boolean isSecond, boolean isUnstable, Zone z){
        HPGPortal newPortal = new HPGPortal(size, viewDir, upDir, portalPos);
        newPortal.isSecond = isSecond;
        newPortal.isUnstable = isUnstable;
        if (!newPortal.figureOutPlacement(z, 0.5f, 0.5f, 1, 1)){
            SeamlessPortals.portalManager.removePortal(newPortal);
            return null;
        }
        return newPortal;
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        super.update(zone, deltaTime);
        if (this.isPortalInAWall(zone) || !this.isPortalOnValidSurface(zone)){
            if (this.linkedPortal != null){
                this.linkedPortal.linkedPortal = null;
                this.linkedPortal.pendingLinkedPortal = null;
                if (GameSingletons.isClient) {
                    this.linkedPortal.playAnimation("rebind");
                }
                if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                    ServerSingletons.SERVER.broadcast(this.linkedPortal.zone, new UpdatePortalPacket(this.linkedPortal));
                    ServerSingletons.SERVER.broadcast(this.linkedPortal.zone, new PortalAnimationPacket(this.linkedPortalID, "rebind"));
                }
            }
            this.startDestruction();
        }
    }

    public boolean figureOutPlacement(Zone z, float maxBumpPosX, float maxBumpNegX, float maxBumpPosY, float maxBumpNegY){
        if (!isPortalInAWall(z) && isPortalOnValidSurface(z)) return true;
        Vector3 originalPos = this.position.cpy();

        if (tryBumping(new Vector3(1, 0, 0), maxBumpPosX, z)) return true;
        this.position.set(originalPos);
        if (tryBumping(new Vector3(-1, 0, 0), maxBumpNegX, z)) return true;
        this.position.set(originalPos);
        if (tryBumping(new Vector3(0, 1, 0), maxBumpPosY, z)) return true;
        this.position.set(originalPos);
        if (tryBumping(new Vector3(0, -1, 0), maxBumpNegY, z)) return true;
        this.position.set(originalPos);

        return false;
    }

    private boolean tryBumping(Vector3 dir, float maxAmount, Zone z){
        // Tries placing the portal along a direction, but with a blacklist
        float bumpAmount = 0.01f;
        float bumpCount = 0;
        Matrix4 portalMatrix = this.getRotationMatrix();
        Vector3 bumpDir = new Vector3();
        bumpDir.set(dir).scl(bumpAmount).mul(portalMatrix);
        while (bumpCount < maxAmount){
            bumpCount += bumpAmount;
            this.position.add(bumpDir);
            if (!isPortalInAWall(z) && isPortalOnValidSurface(z)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPortalOnValidSurface(Zone z){
        float blockCheckBump = 0.1f;
        tmpVec3.set(this.viewDirection).scl(this.isSecond ? -blockCheckBump : blockCheckBump);
        this.position.add(tmpVec3);
//        SeamlessPortals.LOGGER.info("\nTesting at pos " + this.position);
        OrientedBoundingBox bb = this.getMeshBoundingBox();
        this.position.sub(tmpVec3);
        Vector3[] vertices = bb.getVertices();

        IntVector3 min = IntVector3.leastVector(vertices);
        IntVector3 max = IntVector3.greatestVector(vertices);

        for (int bx = min.x; bx <= max.x; ++bx){
            for (int by = min.y; by <= max.y; ++by){
                for (int bz = min.z; bz <= max.z; ++bz){
                    BlockState checkBlock = z.getBlockState(bx, by, bz);
                    if (checkBlock != null && !checkBlock.walkThrough){
                        checkBlock.getBoundingBox(tmpBB, bx, by, bz);
                        if (bb.intersects(tmpBB)){
                            if (this.isUnstable){
                                return checkBlock.hasTag("portal_whitelisted");
                            }
                            else{
                                if (Arrays.asList(defaultBlacklist).contains(checkBlock.getBlockId())){
                                    return false;
                                }
                                if (checkBlock.hasTag("portal_blacklisted")) return false;
                            }
                        }
                    }
                    else return false;
                }
            }
        }
        return true;
    }
}
