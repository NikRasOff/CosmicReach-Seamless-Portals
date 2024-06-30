package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.ScreenUtils;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.animations.ColorAnimation;
import com.nikrasoff.seamlessportals.animations.FloatAnimation;
import com.nikrasoff.seamlessportals.animations.SPAnimationSequence;
import com.nikrasoff.seamlessportals.extras.FloatContainer;
import com.nikrasoff.seamlessportals.extras.IPortalableEntity;
import com.nikrasoff.seamlessportals.models.PortalModel;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.WorldLoader;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.io.CRBSerialized;
import finalforeach.cosmicreach.io.CosmicReachBinaryDeserializer;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.ChunkColumn;

public class Portal extends Entity {
    public transient boolean isPortalDestroyed = false;

    public transient Portal linkedPortal;

    @CRBSerialized
    private Vector3 linkedPortalChunkCoords = new Vector3();
    @CRBSerialized
    private int linkedPortalID = 0;
    @CRBSerialized
    private int portalID = -1;
    @CRBSerialized
    public Vector3 portalSize = new Vector3();
    @CRBSerialized
    public Vector3 upVector = new Vector3(0, 1,0 );

    public boolean isDestroyAnimationPlaying = false;

    @CRBSerialized
    public String zoneID;

    public static Portal readPortal(CosmicReachBinaryDeserializer deserializer){
        Portal portal = new Portal();
        if (deserializer != null) {
            portal.read(deserializer);
            Portal lPortal = SeamlessPortals.portalManager.getPortal(portal.linkedPortalID);
            if (lPortal != null){
                portal.linkPortal(lPortal);
                lPortal.linkPortal(portal);
            }
            else {
                ChunkColumn cc = WorldLoader.INSTANCE.getChunkColumn(InGame.world.getZone(portal.zoneID), (int) portal.linkedPortalChunkCoords.x, (int) portal.linkedPortalChunkCoords.y, (int) portal.linkedPortalChunkCoords.z, false);
                if (cc != null){
                    cc.readyToUnload = true;
                }
                else {
                    return null;
                }
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
        this.model = new PortalModel();
    }

    public Portal(Vector2 size, Vector3 viewDir, Vector3 upDir, Vector3 portalPos, Zone zone){
        this();
        this.portalID = SeamlessPortals.portalManager.getNextPortalID();
        SeamlessPortals.portalManager.addPortal(this);

        this.localBoundingBox.min.set(-size.x/2, -size.y / 2, -1F);
        this.localBoundingBox.max.set(size.x/2, size.y / 2, 1F);

        setPosition(portalPos.x + 0.5F, portalPos.y, portalPos.z + 0.5F);

        this.viewDirection = viewDir;
        this.upVector = upDir;

        this.localBoundingBox.update();
        this.zoneID = zone.zoneId;
        this.portalSize = new Vector3(size.x, size.y, 0);
        this.viewPositionOffset = new Vector3(0, 0, 0);
        this.model.setCurrentAnimation(this, "start");
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

        return new Portal(size, dirString, new Vector3(blPos.getGlobalX(), blPos.getGlobalY(), blPos.getGlobalZ()), zone);
    }

    private static GameMesh createModel(){
        MeshData meshData = new MeshData(ChunkShader.DEFAULT_BLOCK_SHADER, RenderOrder.DEFAULT);

        BlockState.getInstance("seamlessportals:ph_portal[default]").addVertices(meshData, 0, 0, 0);
        return meshData.toSharedIndexMesh(true);
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

        return new OrientedBoundingBox(globalBB, this.modelMatrix.cpy().inv());
    }

    public OrientedBoundingBox getMeshBoundingBox(){
        OrientedBoundingBox globalBB = this.getGlobalBoundingBox();
        globalBB.getBounds().min.z = 0;
        globalBB.getBounds().max.z = 0;
        globalBB.getBounds().update();

        return globalBB;
    }

    public Matrix4 getPortaledTransform(Matrix4 transform){
        Matrix4 newTransform = transform.cpy();
        Matrix4 thisPort = this.modelMatrix;
        Matrix4 linkedPort = this.linkedPortal.modelMatrix;
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
        Matrix4 thisPort = this.modelMatrix.cpy();
        Matrix4 linkedPort = this.linkedPortal.modelMatrix.cpy().inv();
//        linkedPort.inv();
        newPos.mul(thisPort);
        newPos.mul(linkedPort);
        return newPos;
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

    public void render(Camera worldCamera) {
        if (this.model != null) {
            this.modelMatrix.setToLookAt(this.position, this.position.cpy().add(this.viewDirection), this.upVector);
            this.renderModelAfterMatrixSet(worldCamera);
        }
    }

    public void startDestruction(){
        this.model.setCurrentAnimation(this, "end");
    }
}
