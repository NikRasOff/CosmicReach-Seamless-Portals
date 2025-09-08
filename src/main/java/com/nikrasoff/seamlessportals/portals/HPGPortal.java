package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.entities.components.PortalCheckComponent;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.networking.packets.ConvergenceEventPacket;
import com.nikrasoff.seamlessportals.networking.packets.PortalAnimationPacket;
import com.nikrasoff.seamlessportals.networking.packets.UpdatePortalPacket;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.CommonEntityTags;
import finalforeach.cosmicreach.entities.components.GravityComponent;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.sounds.GameSound;
import finalforeach.cosmicreach.world.Zone;

import java.util.Arrays;

public class HPGPortal extends Portal {
    public static final String[] defaultBlacklist = {
            "base:air",
            "base:water"
    };
    public static final Color primaryPortalColor = Color.CYAN;
    public static final Color secondaryPortalColor = Color.ORANGE;
    private static final int convergenceEventCooldown = 1200;
    private static final GameSound[] convEventSounds = new GameSound[]{
            GameSound.of("seamlessportals:sounds/portals/conv_event1.ogg"),
            GameSound.of("seamlessportals:sounds/portals/conv_event2.ogg"),
            GameSound.of("seamlessportals:sounds/portals/conv_event3.ogg"),
            GameSound.of("seamlessportals:sounds/portals/conv_event4.ogg")
    };
    @CRBSerialized
    private boolean isSecond = false;
    @CRBSerialized
    private boolean isUnstable = false;
    @CRBSerialized
    private int convergenceEventTimer = 0;

    public boolean convEventHappening = false;
    private int convEventLifetime = 0;

    private int convEventSmallTimer = 0;
    public int convEventTexture = 0;

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
                //TODO: Fix when more zones get added
                lPortal = SeamlessPortals.portalManager.getPortalWithGen(portal.linkedPortalID, portal.linkedPortalChunkCoords, zoneId);
            } else {
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
        this.addTag(CommonEntityTags.NO_DESPAWN);
        this.addTag(CommonEntityTags.NOCLIP);
        this.addTag(CommonEntityTags.PROJECTILE_IMMUNE);
        this.addTag(CommonEntityTags.NO_ENTITY_PUSH);
        this.addTag(CommonEntityTags.NO_BUOYANCY);
        this.removeUpdatingComponent(GravityComponent.INSTANCE);
        this.removeUpdatingComponent(PortalCheckComponent.INSTANCE);
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
            this.modelInstance.addAnimation("start");
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
    public void update(Zone zone, float deltaTime) {
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
        if (this.linkedPortal == null){
            if (this.convEventHappening){
                this.convEventLifetime += 1;
                if (this.convEventLifetime >= 100){
                    this.convEventHappening = false;
                    this.convEventLifetime = 0;
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                        ServerSingletons.SERVER.broadcast(this.zone, new ConvergenceEventPacket(this, -1));
                    }
                }
                return;
            }
            this.convergenceEventTimer += 1;
            if (this.convergenceEventTimer >= convergenceEventCooldown){
                this.convEventSmallTimer += 1;
                if (this.convEventSmallTimer >= 20){
                    this.convEventSmallTimer = 0;
                    if (MathUtils.random(1000) == 0){
                        this.convergenceEventTimer = 0;
                        this.convEventHappening = true;
                        this.convEventLifetime = 0;
                        this.convEventTexture = MathUtils.random(2);
                        convEventSounds[MathUtils.random(3)].playGlobalSound3D(this.zone, this.position);
                        if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                            ServerSingletons.SERVER.broadcast(this.zone, new ConvergenceEventPacket(this));
                        }
                    }
                }
            }
        }
        else {
            this.convEventHappening = false;
            this.convergenceEventTimer = 0;
            this.convEventSmallTimer = 0;
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
        Vector3 tvec = this.viewDirection.cpy().scl(this.isSecond ? -blockCheckBump : blockCheckBump);
        this.position.add(tvec);
//        SeamlessPortals.LOGGER.info("\nTesting at pos " + this.position);
        OrientedBoundingBox bb = this.getMeshBoundingBox();
        this.position.sub(tvec);
        Vector3[] vertices = bb.getVertices();

        IntVector3 min = IntVector3.leastVector(vertices);
        IntVector3 max = IntVector3.greatestVector(vertices);

        BoundingBox tBB = new BoundingBox();

        for (int bx = min.x; bx <= max.x; ++bx){
            for (int by = min.y; by <= max.y; ++by){
                for (int bz = min.z; bz <= max.z; ++bz){
                    BlockState checkBlock = z.getBlockState(bx, by, bz);
                    if (checkBlock != null && !checkBlock.walkThrough){
                        checkBlock.getBoundingBox(tBB, bx, by, bz);
                        if (bb.intersects(tBB)){
                            if (this.isUnstable){
                                return checkBlock.hasTag(SeamlessPortalsConstants.PORTAL_WHITELISTED);
                            }
                            else{
                                if (Arrays.asList(defaultBlacklist).contains(checkBlock.getBlockId())){
                                    return false;
                                }
                                if (checkBlock.hasTag(SeamlessPortalsConstants.PORTAL_BLACKLISTED)) {
                                    return false;
                                }
                            }
                        }
                    }
                    else{
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
