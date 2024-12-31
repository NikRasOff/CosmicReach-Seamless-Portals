package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.networking.packets.PortalAnimationPacket;
import com.nikrasoff.seamlessportals.networking.packets.PortalDeletePacket;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.sounds.GameSound;
import finalforeach.cosmicreach.util.ArrayUtils;
import finalforeach.cosmicreach.world.EntityRegion;
import finalforeach.cosmicreach.world.Zone;

import java.util.Arrays;

public class Portal extends Entity {
    public static GameSound portalOpenSound = GameSound.of("seamlessportals:sounds/portals/portal_open.ogg");
    public static GameSound portalCloseSound = GameSound.of("seamlessportals:sounds/portals/portal_close.ogg");

    public transient boolean isPortalDestroyed = false;

    public transient Portal pendingLinkedPortal;
    public transient Portal linkedPortal;
    @CRBSerialized
    final Vector3 linkedPortalChunkCoords = new Vector3();
    @CRBSerialized
    EntityUniqueId linkedPortalID = new EntityUniqueId();
    @CRBSerialized
    public Vector3 portalSize = new Vector3();
    @CRBSerialized
    public Vector3 upVector = new Vector3(0, 1,0 );

    @CRBSerialized
    private boolean isEndAnimationPlaying = false;
    @CRBSerialized
    private float endAnimationTimer = 0f;

    private final BoundingBox meshBB = new BoundingBox();
    public static final Object lock = new Object();
    protected static final Vector3 tmpVec3 = new Vector3();
    protected static final Array<BoundingBox> tempBounds = new Array<>();
    protected static final BoundingBox tmpBB = new BoundingBox();

    public static Portal readPortal(CRBinDeserializer deserializer){
        // It took so much time to make this work... and yet it's still somehow broken
        Portal portal = new Portal();
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

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);
        if (this.zone == null){
            serial.writeString("zoneId", GameSingletons.world.defaultZoneId);
        }
        else{
            serial.writeString("zoneId", this.zone.zoneId);
        }
    }

    protected Portal(String id){
        super(id);
    }

    public Portal(){
        super("seamlessportals:entity_portal");
        this.canDespawn = false;
        this.hasGravity = false;
        this.noClip = true;
        IPortalableEntity.setIgnorePortals((IPortalableEntity) this, true);
        if (GameSingletons.isClient){
            this.modelInstance = SeamlessPortals.clientConstants.getNewPortalModelInstance();
        }
    }

    public Portal(Vector2 size, Vector3 viewDir, Vector3 upDir, Vector3 portalPos){
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

    public Portal(Vector2 size, String viewDir, Vector3 portalPos){
        this(new Vector2(size), new Vector3(0, 0, 1), new Vector3(0, 1, 0), portalPos);
        switch (viewDir){
            case "negZ":
                this.viewDirection.set(0, 0, -1);
                this.position.z -= 1;
                break;
            case "posX":
                this.viewDirection.set(1, 0, 0);
                this.position.x += 1;
                break;
            case "negX":
                this.viewDirection.set(-1, 0, 0);
                this.position.x -= 1;
                break;
            case "posYposZ":
                this.viewDirection.set(0, 1, 0);
                this.upVector.set(0, 0, -1);
                this.position.y += 1;
                break;
            case "posYnegX":
                this.viewDirection.set(0, 1, 0);
                this.upVector.set(1, 0, 0);
                this.position.y += 1;
                break;
            case "posYnegZ":
                this.viewDirection.set(0, 1, 0);
                this.upVector.set(0, 0, 1);
                this.position.y += 1;
                break;
            case "posYposX":
                this.viewDirection.set(0, 1, 0);
                this.upVector.set(-1, 0, 0);
                this.position.y += 1;
                break;
            case "negYposZ":
                this.viewDirection.set(0, -1, 0);
                this.upVector.set(0, 0, 1);
                this.position.y -= 1;
                break;
            case "negYnegX":
                this.viewDirection.set(0, -1, 0);
                this.upVector.set(-1, 0, 0);
                this.position.y -= 1;
                break;
            case "negYnegZ":
                this.viewDirection.set(0, -1, 0);
                this.upVector.set(0, 0, -1);
                this.position.y -= 1;
                break;
            case "negYposX":
                this.viewDirection.set(0, -1, 0);
                this.upVector.set(1, 0, 0);
                this.position.y -= 1;
                break;
            default:
                this.viewDirection.set(0, 0, 1);
                this.position.z += 1;
                break;
        }
    }

    public void calculateLocalBB(){
        float diag = (float) Math.sqrt((this.portalSize.x / 2) * (this.portalSize.x / 2) + (this.portalSize.y / 2) * (this.portalSize.y / 2));
        this.localBoundingBox.max.set(diag, diag, diag);
        this.localBoundingBox.min.set(-diag, -diag, -diag);
        this.localBoundingBox.update();
    }

    public void calculateMeshBB(){
        this.meshBB.min.set(-this.portalSize.x / 2 + 0.001f, -this.portalSize.y / 2 + 0.001f, -0.01f);
        this.meshBB.max.set(this.portalSize.x / 2 - 0.001f, this.portalSize.y / 2 - 0.001f, 0.01f);
        this.meshBB.update();
    }

    public void updateSize(Vector2 newSize){
        this.portalSize.x = newSize.x;
        this.portalSize.y = newSize.y;
        this.calculateLocalBB();
        this.calculateMeshBB();
    }

    public void playAnimation(String animName){
        if (this.isEndAnimationPlaying) return;
        this.modelInstance.setCurrentAnimation(animName);
    }

    @Override
    public void hit(float amount) {}

    public static Portal fromBlockInfo(PortalSpawnBlockInfo info, Vector2 size){
        String[] strId = info.blockState.split(",");
        String dirString = "";
        for (String id : strId){
            String[] i = id.split("=");
            if (i.length > 1){
                if (i[0].equals("facing")){
                    dirString = i[1];
                }
            }
        }

        return new Portal(size, dirString, info.position.toVector3().add(new Vector3(0.5f, 0.5f, 0.5f)));
    }

    public void linkPortal(Portal to){
        linkedPortal = to;
        pendingLinkedPortal = to;
        this.linkedPortalID = to.uniqueId;
        this.linkedPortalChunkCoords.x = Math.floorDiv((int) linkedPortal.position.x, 16);
        this.linkedPortalChunkCoords.y = Math.floorDiv((int) linkedPortal.position.y, 16);
        this.linkedPortalChunkCoords.z = Math.floorDiv((int) linkedPortal.position.z, 16);
    }

    public OrientedBoundingBox getFatBoundingBox(){
        BoundingBox globalBB = new BoundingBox();
        globalBB.set(this.meshBB);
        globalBB.min.z = -1;
        globalBB.max.z = 1;
        globalBB.update();

        return new OrientedBoundingBox(globalBB, this.getPortalMatrix().inv());
    }

    public OrientedBoundingBox getMeshBoundingBox(){
        Matrix4 pm = this.getPortalMatrix();
        return new OrientedBoundingBox(meshBB, pm.cpy().inv());
    }

    public Matrix4 getPortaledTransform(Matrix4 transform){
        if (linkedPortal == null) return transform.cpy();
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
        if (linkedPortal == null) return pos.cpy();
        Vector3 newPos = pos.cpy();
        Matrix4 thisPort = this.getPortalMatrix();
        Matrix4 linkedPort = this.linkedPortal.getPortalMatrix().inv();
        newPos.mul(thisPort);
        newPos.mul(linkedPort);
        return newPos;
    }

    public Matrix4 getRotationMatrix(){
        Matrix4 m = new Matrix4();
        synchronized (lock){
            m.setToLookAt(Vector3.Zero, this.viewDirection, this.upVector);
        }
        return m;
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

    public boolean isNotOnSameSideOfPortal(Vector3 pos1, Vector3 pos2){
        return getPortalSide(pos1) != getPortalSide(pos2);
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        super.update(zone, deltaTime);
        if (this.linkedPortal.zone == null){
            EntityRegion.readChunkColumn(GameSingletons.world.getZoneCreateIfNull(this.zone.zoneId), (int) this.linkedPortalChunkCoords.x, (int) this.linkedPortalChunkCoords.z, Math.floorDiv((int) this.linkedPortalChunkCoords.x, 16), Math.floorDiv((int) this.linkedPortalChunkCoords.y, 16), Math.floorDiv((int) this.linkedPortalChunkCoords.z, 16));
        }
        if (isEndAnimationPlaying){
            this.endAnimationTimer += (float) deltaTime;
            if (this.endAnimationTimer >= 0.75f){
                this.isPortalDestroyed = true;
                this.onDeath();
            }
        }
    }

    public void render(Camera worldCamera) {
        if (this.linkedPortal != this.pendingLinkedPortal) this.linkedPortal = this.pendingLinkedPortal;
        if (this.modelInstance != null) {
            tmpModelMatrix.setToLookAt(this.position, this.position.cpy().add(this.viewDirection), this.upVector);
            this.renderModelAfterMatrixSet(worldCamera);
        }
    }

    public void startDestruction(){
        if (isEndAnimationPlaying) return;
        portalCloseSound.playGlobalSound3D(this.zone, this.position);
        this.endAnimationTimer = 0;
        if (GameSingletons.isClient){
            playAnimation("end");
        }
        if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
            ServerSingletons.SERVER.broadcast(this.zone, new PortalAnimationPacket(this.uniqueId, "end"));
        }
        this.isEndAnimationPlaying = true;
    }

    void destroySelfAndLinkedPortal(){
        this.startDestruction();
        if (this.linkedPortal != null) this.linkedPortal.startDestruction();
    }

    @Override
    protected void onDeath() {
        if (GameSingletons.isHost && ServerSingletons.SERVER != null){
            ServerSingletons.SERVER.broadcast(this.zone, new PortalDeletePacket(this.uniqueId));
        }
        SeamlessPortals.portalManager.removePortal(this);
        super.onDeath();
    }

    public boolean isPortalInRange(boolean byProxy, int renderDistance){
        if (this.isEndAnimationPlaying) return true;
        if (!ArrayUtils.any(GameSingletons.world.players, (p) -> this.position.dst2(p.getPosition()) >= renderDistance * renderDistance * 256)){
            return false;
        }
        if (byProxy && this.linkedPortal != null){
            return this.linkedPortal.isPortalInRange(false, renderDistance);
        }
        return false;
    }

    public boolean isPortalInAWall(Zone z) {
        // Does what it says on the tin - figures out if the portal is intersecting something
        OrientedBoundingBox bb = this.getMeshBoundingBox();
        Vector3[] vertices = bb.getVertices();

        IntVector3 min = IntVector3.leastVector(vertices);
        IntVector3 max = IntVector3.greatestVector(vertices);

        for (int bx = min.x; bx <= max.x; ++bx) {
            for (int by = min.y; by <= max.y; ++by) {
                for (int bz = min.z; bz <= max.z; ++bz) {
                    BlockState checkBlock = z.getBlockState(bx, by, bz);
                    if (checkBlock != null && !checkBlock.walkThrough) {
                        checkBlock.getAllBoundingBoxes(tempBounds, bx, by, bz);
                        for (BoundingBox bb1 : tempBounds) {
                            if (bb.intersects(bb1)) return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
