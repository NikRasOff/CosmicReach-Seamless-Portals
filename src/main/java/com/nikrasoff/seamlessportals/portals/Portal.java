package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.rendering.models.PortalModel;
import com.nikrasoff.seamlessportals.rendering.models.PortalModelInstance;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.world.EntityRegion;
import finalforeach.cosmicreach.world.Zone;

public class Portal extends Entity {
    public transient boolean isPortalDestroyed = false;

    public transient Portal linkedPortal;

    static private final PortalModel portalModel = new PortalModel();

    @CRBSerialized
    private Vector3 linkedPortalChunkCoords = new Vector3();
    @CRBSerialized
    private int linkedPortalID = -1;
    @CRBSerialized
    private int portalID = -1;
    @CRBSerialized
    public Vector3 portalSize = new Vector3();
    @CRBSerialized
    public Vector3 upVector = new Vector3(0, 1,0 );

    @CRBSerialized
    public String zoneID;

    private boolean isEndAnimationPlaying = false;
    public static final Object lock = new Object();

    public static Portal readPortal(CRBinDeserializer deserializer){
        // It took so much time to make this work...
        Portal portal = new Portal();
        if (deserializer != null) {
            portal.read(deserializer);
            SeamlessPortals.portalManager.addPortal(portal);
            Portal lPortal = SeamlessPortals.portalManager.getPortal(portal.linkedPortalID);
            if (lPortal != null){
                portal.linkPortal(lPortal);
                lPortal.linkPortal(portal);
            }
            else {
                Vector3 chunkCoords = portal.linkedPortalChunkCoords;
                EntityRegion.readChunkColumn(InGame.world.getZone(portal.zoneID), (int) chunkCoords.x, (int) chunkCoords.z, Math.floorDiv((int) chunkCoords.x, 16), Math.floorDiv((int) chunkCoords.y, 16), Math.floorDiv((int) chunkCoords.z, 16));
            }
        }
        return portal;
    }

    public Portal(){
        super("seamlessportals:entity_portal");
        this.canDespawn = false;
        this.hasGravity = false;
        this.noClip = true;
        IPortalableEntity.setIgnorePortals((IPortalableEntity) this, true);
        this.zoneID = "base:moon";
        this.modelInstance = portalModel.getNewModelInstance();
    }

    public Portal(Vector2 size, Vector3 viewDir, Vector3 upDir, Vector3 portalPos, Zone zone){
        this();
        this.portalID = SeamlessPortals.portalManager.getNextPortalID();
        SeamlessPortals.portalManager.addPortal(this);

        this.localBoundingBox.min.set(-size.x/2, -size.y / 2, -1F);
        this.localBoundingBox.max.set(size.x/2, size.y / 2, 1F);

        setPosition(portalPos.x, portalPos.y, portalPos.z);

        this.viewDirection = viewDir;
        this.upVector = upDir;

        this.localBoundingBox.update();
        this.zoneID = zone.zoneId;
        this.portalSize = new Vector3(size.x, size.y, 0);
        this.viewPositionOffset = new Vector3(0, 0, 0);
        this.modelInstance.setCurrentAnimation("start");
    }

    public Portal(Vector2 size, String viewDir, Vector3 portalPos, Zone zone){
        this(new Vector2(3, 3), new Vector3(0, 0, 1), new Vector3(0, 1, 0), portalPos, zone);
        switch (viewDir){
            case "negZ":
                this.viewDirection = new Vector3(0, 0, -1);
                this.position.y += size.y / 2;
                break;
            case "posX":
                this.viewDirection = new Vector3(1, 0, 0);
                this.position.y += size.y / 2;
                break;
            case "negX":
                this.viewDirection = new Vector3(-1, 0, 0);
                this.position.y += size.y / 2;
                break;
            case "posYposZ":
                this.viewDirection = new Vector3(0, 1, 0);
                this.upVector = new Vector3(0, 0, -1);
                break;
            case "posYnegX":
                this.viewDirection = new Vector3(0, 1, 0);
                this.upVector = new Vector3(1, 0, 0);
                this.position.y += 0.5F;
                break;
            case "posYnegZ":
                this.viewDirection = new Vector3(0, 1, 0);
                this.upVector = new Vector3(0, 0, 1);
                this.position.y += 0.5F;
                break;
            case "posYposX":
                this.viewDirection = new Vector3(0, 1, 0);
                this.upVector = new Vector3(-1, 0, 0);
                this.position.y += 0.5F;
                break;
            case "negYposZ":
                this.viewDirection = new Vector3(0, -1, 0);
                this.upVector = new Vector3(0, 0, 1);
                this.position.y += 0.5F;
                break;
            case "negYnegX":
                this.viewDirection = new Vector3(0, -1, 0);
                this.upVector = new Vector3(-1, 0, 0);
                this.position.y += 0.5F;
                break;
            case "negYnegZ":
                this.viewDirection = new Vector3(0, -1, 0);
                this.upVector = new Vector3(0, 0, -1);
                this.position.y += 0.5F;
                break;
            case "negYposX":
                this.viewDirection = new Vector3(0, -1, 0);
                this.upVector = new Vector3(1, 0, 0);
                this.position.y += 0.5F;
                break;
            default:
                this.viewDirection = new Vector3(0, 0, 1);
                this.position.y += size.y / 2;
                break;
        }
    }

    public void playAnimation(String animName){
        if (this.isEndAnimationPlaying) return;
        this.modelInstance.setCurrentAnimation(animName);
    }

    public int getPortalID(){
        return this.portalID;
    }

    @Override
    public void hit(float amount) {}

    public static Portal fromBlockPos(Vector2 size, BlockPosition blPos, Zone zone){
        String[] strId = blPos.getBlockState().stringId.split(",");
        String dirString = "";
        for (String id : strId){
            String[] i = id.split("=");
            if (i.length > 1){
                if (i[0].equals("facing")){
                    dirString = i[1];
                }
            }
        }

        return new Portal(size, dirString, new Vector3(blPos.getGlobalX() + 0.5F, blPos.getGlobalY(), blPos.getGlobalZ() + 0.5F), zone);
    }

    public void linkPortal(Portal to){
        linkedPortal = to;
        this.linkedPortalID = to.getPortalID();
        this.linkedPortalChunkCoords.x = Math.floorDiv((int) linkedPortal.position.x, 16);
        this.linkedPortalChunkCoords.y = Math.floorDiv((int) linkedPortal.position.y, 16);
        this.linkedPortalChunkCoords.z = Math.floorDiv((int) linkedPortal.position.z, 16);
    }

    public OrientedBoundingBox getGlobalBoundingBox(){
        BoundingBox globalBB = new BoundingBox();
        globalBB.set(this.localBoundingBox);
        globalBB.update();

        return new OrientedBoundingBox(globalBB, this.getPortalMatrix().inv());
    }

    public OrientedBoundingBox getMeshBoundingBox(){
        BoundingBox meshBB = new BoundingBox();
        meshBB.set(this.localBoundingBox);
        meshBB.min.z = -0.025F;
        meshBB.max.z = 0.025F;
        meshBB.update();

        return new OrientedBoundingBox(meshBB, this.getPortalMatrix().inv());
    }

    public Matrix4 getPortaledTransform(Matrix4 transform){
        Matrix4 newTransform = transform.cpy();
        Matrix4 thisPort = this.getPortalMatrix();
        Matrix4 linkedPort = this.linkedPortal.getPortalMatrix();
        thisPort.setTranslation(0, 0, 0);
        linkedPort.setTranslation(0, 0, 0);
        thisPort.inv();
        newTransform.mul(thisPort);
        newTransform.mul(linkedPort);
        return newTransform;
    }

    public float getDistanceToPortalPlane(Vector3 pos){
        Plane portalPlane = new Plane(this.viewDirection, this.position);
        return Math.abs(portalPlane.distance(pos));
    }

    public Vector3 getPortaledPos(Vector3 pos){
        Vector3 newPos = pos.cpy();
        Matrix4 thisPort = this.getPortalMatrix();
        Matrix4 linkedPort = this.linkedPortal.getPortalMatrix().inv();
        newPos.mul(thisPort);
        newPos.mul(linkedPort);
        return newPos;
    }

    public Matrix4 getPortalMatrix(){
        // Synchronized to get rid of weird flickering when teleporting
        Matrix4 m = new Matrix4();
        synchronized (lock){
            m.setToLookAt(this.position, this.position.cpy().add(this.viewDirection), this.upVector);
        }
        return m;
    }

    public Vector3 getPortaledVector(Vector3 vector3){
        Vector3 from = this.getPortaledPos(Vector3.Zero);
        Vector3 to = this.getPortaledPos(vector3);

        return to.sub(from);
    }

    public int getPortalSide(Vector3 pos){
        Vector3 localOffset = pos.cpy().sub(this.position);
        return getPortalSideLocal(localOffset);
    }

    public int getPortalSideLocal(Vector3 offset){
        return (int) Math.signum(offset.dot(this.viewDirection));
    }

    public boolean isOnSameSideOfPortal(Vector3 pos1, Vector3 pos2){
        return getPortalSide(pos1) == getPortalSide(pos2);
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        super.update(zone, deltaTime);
        if (isEndAnimationPlaying){
            PortalModelInstance pm = (PortalModelInstance) this.modelInstance;
            if (pm.isAnimationOver()){
                this.isPortalDestroyed = true;
                this.onDeath(zone);
            }
        }
    }

    public void render(Camera worldCamera) {
        if (this.modelInstance != null) {
            tmpModelMatrix.setToLookAt(this.position, this.position.cpy().add(this.viewDirection), this.upVector);
            this.renderModelAfterMatrixSet(worldCamera);
        }
    }

    public void startDestruction(){
        if (isEndAnimationPlaying) return;
        this.isEndAnimationPlaying = true;
        this.modelInstance.setCurrentAnimation("end");
    }

    @Override
    protected void onDeath(Zone zone) {
        SeamlessPortals.portalManager.removePortal(this);
        super.onDeath(zone);
    }

    public boolean isPortalInRange(boolean byProxy){
        int renderDistance = GraphicsSettings.renderDistanceInChunks.getValue();
        if (this.position.dst2(InGame.getLocalPlayer().getPosition()) <= renderDistance * renderDistance * 256){
            return true;
        }
        if (byProxy && this.linkedPortal != null){
            return this.linkedPortal.isPortalInRange(false);
        }
        return false;
    }
}
