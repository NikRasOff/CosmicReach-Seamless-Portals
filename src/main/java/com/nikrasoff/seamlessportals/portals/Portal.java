package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ScreenUtils;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.animations.ColorAnimation;
import com.nikrasoff.seamlessportals.animations.FloatAnimation;
import com.nikrasoff.seamlessportals.animations.SPAnimationSequence;
import com.nikrasoff.seamlessportals.extras.FloatContainer;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.gamestates.InGame;
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

public class Portal extends Entity {
    public transient boolean isPortalDestroyed = false;
    public transient boolean isPortalBeingUsed = false;
    public transient Vector3 portalEndPosition = new Vector3();
    private transient final boolean ignorePortals;

    public transient Portal linkedPortal;
    private Vector2 portalSize;

    private transient Vector3 portalMeshScale = new Vector3();
    private transient Vector3 portalMeshLocalOffset = new Vector3(0, 0, 0);

    static GameMesh mesh = createModel();
    static GameShader shader = new GameShader("seamlessportals:portal.vert.glsl", "seamlessportals:portal.frag.glsl");

    public boolean isDestroyAnimationPlaying = false;

    public String zoneID;

    private transient FrameBuffer portalFrameBuffer;

    private transient final PerspectiveCamera portalCamera;

    private transient final SPAnimationSequence startingAnimationSequence = new SPAnimationSequence(false);
    private transient final SPAnimationSequence endingAnimationSequence = new SPAnimationSequence(true);

    private transient final FloatContainer animModelScale = new FloatContainer(1);
    private transient final Color colorOverlay = Color.CLEAR.cpy();

    public Portal(){
        this.ignorePortals = true;
        this.portalCamera = new PerspectiveCamera(GraphicsSettings.fieldOfView.getValue(), (float)Gdx.graphics.getWidth(), (float)Gdx.graphics.getHeight());
        endingAnimationSequence.add(new FloatAnimation(1, 0, 0.5F, this.animModelScale));
        endingAnimationSequence.add(new ColorAnimation(new Color(1, 0, 0, 0), new Color(1, 0, 0, 1), 0.25F, this.colorOverlay));
        startingAnimationSequence.finish();
        this.zoneID = "base:moon";
    }

    public Portal(Vector2 size, String viewDir, Vector3 portalPos, Zone zone){
        this.hasGravity = false;
        this.ignorePortals = true;


        switch (viewDir){
            case "negZ":
                viewDirection = new Vector3(0, 0, -1);
                this.localBoundingBox.min.set(-size.x/2, -size.y / 2, -1F);
                this.localBoundingBox.max.set(size.x/2, size.y / 2, 1F);
                break;
            case "posX":
                viewDirection = new Vector3(1, 0, 0);
                this.localBoundingBox.min.set(-1F, -size.y / 2, -size.x/2);
                this.localBoundingBox.max.set(1F, size.y / 2, size.x/2);
                break;
            case "negX":
                viewDirection = new Vector3(-1, 0, 0);
                this.localBoundingBox.min.set(-1F, -size.y / 2, -size.x/2);
                this.localBoundingBox.max.set(1F, size.y / 2, size.x/2);
                break;
            default:
                viewDirection = new Vector3(0, 0, 1);
                this.localBoundingBox.min.set(-size.x/2, -size.y / 2, -1F);
                this.localBoundingBox.max.set(size.x/2, size.y / 2, 1F);
                break;
        }
        setPosition(portalPos.x + 0.5F, portalPos.y + size.y / 2, portalPos.z + 0.5F);
        this.zoneID = zone.zoneId;
        this.portalSize = size;
        this.viewPositionOffset = new Vector3(0, 0, 0);
        this.portalCamera = new PerspectiveCamera(GraphicsSettings.fieldOfView.getValue(), (float)Gdx.graphics.getWidth(), (float)Gdx.graphics.getHeight());
        portalFrameBuffer = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        startingAnimationSequence.add(new FloatAnimation(0, 1, 0.5F, this.animModelScale));
        startingAnimationSequence.add(new ColorAnimation(new Color(0, 0, 1, 1), new Color(0, 0, 1,0), 0.5F, this.colorOverlay));
        this.colorOverlay.set(0, 0, 1, 1);

        endingAnimationSequence.add(new FloatAnimation(1, 0, 0.5F, this.animModelScale));
        endingAnimationSequence.add(new ColorAnimation(new Color(1, 0, 0, 0), new Color(1, 0, 0, 1), 0.25F, this.colorOverlay));
    }

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
    }

    public BoundingBox getGlobalBoundingBox(){
        BoundingBox globalBB = new BoundingBox();
        globalBB.set(localBoundingBox);
        globalBB.min.add(this.position);
        globalBB.max.add(this.position);
        globalBB.update();
        return globalBB;
    }

    public BoundingBox getMeshBoundingBox(){
        BoundingBox meshBB = new BoundingBox();
        meshBB.min.set(this.portalMeshScale).scl(-0.5F).add(this.position);
        meshBB.max.set(this.portalMeshScale).scl(0.5F).add(this.position);
        return meshBB;
    }

    public float getPortalYaw(){
        return (float) Math.toDegrees(Math.acos(viewDirection.z)) + (viewDirection.x < 0 ? 180 : 0);
    }

    private Matrix4 calculateObliqueMatrix(PerspectiveCamera playerCamera, Vector4 newNearPlane){
        // It fucking works!
        float[] res = playerCamera.projection.getValues().clone();

        Vector4 q = new Vector4();
        q.x = (Math.signum(newNearPlane.x) + res[8]) / res[0];
        q.y = (Math.signum(newNearPlane.y) + res[9]) / res[5];
        q.z = -1.0F;
        q.w = (1 + res[10]) / res[14];

        Vector4 m3 = newNearPlane.cpy().scl(2.0F / newNearPlane.dot(q));
        res[2] = m3.x;
        res[6] = m3.y;
        res[10] = m3.z + 1.0F;
        res[14] = m3.w;

        return new Matrix4(res);
    }

    private void setCameraNearClipPlane(Camera playerCamera){
        Matrix4 clipPlane = new Matrix4();
        clipPlane.setToLookAt(this.position, this.position.cpy().add(this.viewDirection), new Vector3(0, 1, 0));

        int dot = (int) Math.signum(this.viewDirection.dot(this.position.cpy().sub(playerCamera.position)));

        Vector3 viewSpacePos = this.linkedPortal.position.cpy().mul(this.portalCamera.view);
        Vector3 viewSpaceNormal = this.linkedPortal.position.cpy().add(this.linkedPortal.viewDirection).mul(this.portalCamera.view);
        viewSpaceNormal.sub(viewSpacePos).nor().scl(dot);
        float viewSpaceDist = -viewSpacePos.dot(viewSpaceNormal);

        float nearClipLimit = 0.2f;
        if (Math.abs(viewSpaceDist) > nearClipLimit){
            Vector4 clipPlaneCameraSpace = new Vector4(-viewSpaceNormal.x, -viewSpaceNormal.y, -viewSpaceNormal.z, -viewSpaceDist);
            if (viewSpaceDist < 0){
                clipPlaneCameraSpace.scl(-1);
            }

            this.portalCamera.projection.set(this.calculateObliqueMatrix(this.portalCamera, clipPlaneCameraSpace));
        }
        this.portalCamera.combined.set(this.portalCamera.projection);
        Matrix4.mul(this.portalCamera.combined.val, this.portalCamera.view.val);
        // Not updating the frustum because that causes a bunch of artifacts
//        this.portalCamera.invProjectionView.set(this.portalCamera.combined);
//        Matrix4.inv(this.portalCamera.invProjectionView.val);
//        this.portalCamera.frustum.update(this.portalCamera.invProjectionView);
    }

    private void updatePortalCamera(Camera playerCamera){
        this.portalCamera.viewportHeight = playerCamera.viewportHeight;
        this.portalCamera.viewportWidth = playerCamera.viewportWidth;
        this.portalCamera.fieldOfView = GraphicsSettings.fieldOfView.getValue();
        this.portalCamera.near = playerCamera.near;
        this.portalCamera.far = playerCamera.far;

        Vector3 newPortalCamPos = getPortaledPos(playerCamera.position);
        Vector3 newPortalCamDir = getPortaledVector(playerCamera.direction);

        this.portalCamera.position.x = newPortalCamPos.x;
        this.portalCamera.position.y = newPortalCamPos.y;
        this.portalCamera.position.z = newPortalCamPos.z;
        this.portalCamera.position.add(this.viewPositionOffset);
        this.portalCamera.direction.set(newPortalCamDir);
        this.portalCamera.up.set(0.0F, 1.0F, 0.0F);
        this.portalCamera.update();

        if (!this.isPortalBeingUsed) setCameraNearClipPlane(playerCamera);
    }

    private Texture createPortalTexture(Camera playerCamera){
        updatePortalCamera(playerCamera);
        if (this.portalFrameBuffer == null){
            this.portalFrameBuffer = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }
        else if (Gdx.graphics.getWidth() != portalFrameBuffer.getWidth() || Gdx.graphics.getHeight() != portalFrameBuffer.getHeight()){
            if (Gdx.graphics.getWidth() > 0 && Gdx.graphics.getHeight() > 0){
                this.portalFrameBuffer.dispose();
                this.portalFrameBuffer = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            }
        }
        this.portalFrameBuffer.begin();
        ScreenUtils.clear(Sky.skyColor, true);
        Sky.drawStars(this.portalCamera);
        GameSingletons.zoneRenderer.render(InGame.world.getZone(this.zoneID), this.portalCamera);
        if (this.isPortalBeingUsed && !this.isOnSameSideOfPortal(playerCamera.position, this.portalEndPosition)) {
            this.linkedPortal.renderToFrameBuffer(this.portalCamera, portalFrameBuffer);
            portalFrameBuffer.bind();
        }
        portalFrameBuffer.end();

        return this.portalFrameBuffer.getColorBufferTexture();
    }

    public void updatePortalMeshScale(PerspectiveCamera playerCamera){
        float thisPortalYaw = this.getPortalYaw();

        float halfHeight = (float) (playerCamera.near * Math.tan(Math.toRadians(playerCamera.fieldOfView * 0.5)));
        float halfWidth = halfHeight * (playerCamera.viewportWidth / playerCamera.viewportHeight);

        float portalThickness = (new Vector3(halfWidth, halfHeight, playerCamera.near)).len();

        Plane portalPlane = new Plane(this.viewDirection, this.position);
        float camDistToPortalPlane = Math.abs(portalPlane.distance(playerCamera.position));

        if (this.isPortalBeingUsed){
            portalThickness += camDistToPortalPlane;
        }
        else{
            if ((camDistToPortalPlane > portalThickness) || (this.position.dst(playerCamera.position) > (this.portalSize.len() / 2))){
                portalThickness = 0;
            }
        }

        this.portalMeshScale = new Vector3(this.portalSize.x, this.portalSize.y, portalThickness);
        this.portalMeshScale.rotate(thisPortalYaw, 0, 1, 0);

        if (this.isPortalBeingUsed) this.setPortalMeshLocalOffset(this.portalEndPosition, portalThickness);
        else this.setPortalMeshLocalOffset(playerCamera.position, portalThickness);

        this.portalMeshScale.scl(this.animModelScale.getValue());
        this.portalMeshLocalOffset.scl(this.animModelScale.getValue());
    }

    public void setPortalMeshLocalOffset(Vector3 pos, float width){
        boolean camFacingSameDirAsPortal = this.viewDirection.dot(pos.cpy().sub(this.position)) < 0;
        this.portalMeshLocalOffset = new Vector3(0, 0, width * (camFacingSameDirAsPortal ? 0.5f : -0.5f));
        this.portalMeshLocalOffset.rotate(this.getPortalYaw(), 0, 1, 0);
    }

    public Vector3 getPortaledPos(Vector3 pos){
        Vector3 newPos = pos.cpy();
        Matrix4 thisPort = new Matrix4();
        thisPort.setToLookAt(this.position, this.position.cpy().add(this.viewDirection), new Vector3(0, 1, 0));
        Matrix4 linkedPort = new Matrix4();
        linkedPort.setToLookAt(this.linkedPortal.position, this.linkedPortal.position.cpy().add(this.linkedPortal.viewDirection.cpy()), new Vector3(0, 1, 0));
        linkedPort.inv();
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

    public void updateAnimations(float deltaTime){
        if (!this.startingAnimationSequence.isFinished()){
            this.startingAnimationSequence.update(deltaTime);
        }
        if (this.isDestroyAnimationPlaying){
            if (!this.startingAnimationSequence.isFinished() || this.endingAnimationSequence.isFinished()){
                this.destroyPortal();
                return;
            }
            this.endingAnimationSequence.update(deltaTime);
        }
    }

    public void render (Camera playerCamera){
        if (mesh != null) {
            Texture portalTexture = this.createPortalTexture(playerCamera);
            this.updatePortalMeshScale((PerspectiveCamera) playerCamera);


            SharedQuadIndexData.bind();
            shader.bind(playerCamera);

            Vector2 screenSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            float[] tmpVec = new float[2];
            tmpVec[0] = screenSize.x;
            tmpVec[1] = screenSize.y;
            shader.shader.setUniform2fv("screenSize", tmpVec, 0, 2);

            float[] tmpVec4 = new float[4];
            tmpVec4[0] = this.colorOverlay.r;
            tmpVec4[1] = this.colorOverlay.g;
            tmpVec4[2] = this.colorOverlay.b;
            tmpVec4[3] = this.colorOverlay.a;

            shader.shader.setUniform4fv("overlayColor", tmpVec4, 0, 4);

            shader.bindOptionalTexture("screenTex", portalTexture, 1);
            shader.bindOptionalUniform3f("posOffset", this.position);
            shader.bindOptionalUniform3f("localOffset", this.portalMeshLocalOffset);
            shader.bindOptionalUniform3f("portScale", this.portalMeshScale);
            shader.shader.setUniformMatrix("u_projViewTrans", playerCamera.combined);
            mesh.bind(shader.shader);
            mesh.render(shader.shader, 4);
            mesh.unbind(shader.shader);
            SharedQuadIndexData.unbind();
        }
    }

    public void renderToFrameBuffer(Camera playerCamera, FrameBuffer frameBuffer){
        Texture portalTexture = createPortalTexture(playerCamera);
        updatePortalMeshScale((PerspectiveCamera) playerCamera);

        SharedQuadIndexData.bind();
        shader.bind(playerCamera);

        Vector2 screenSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float[] tmpVec = new float[2];
        tmpVec[0] = screenSize.x;
        tmpVec[1] = screenSize.y;
        shader.shader.setUniform2fv("screenSize", tmpVec, 0, 2);

        shader.bindOptionalTexture("screenTex", portalTexture, 1);
        shader.bindOptionalUniform3f("posOffset", this.position);
        shader.bindOptionalUniform3f("localOffset", this.portalMeshLocalOffset);
        shader.bindOptionalUniform3f("portScale", this.portalMeshScale);
        shader.shader.setUniformMatrix("u_projViewTrans", playerCamera.combined);
        mesh.bind(shader.shader);
        frameBuffer.bind();
        mesh.render(shader.shader, 4);
        mesh.unbind(shader.shader);
        FrameBuffer.unbind();
        SharedQuadIndexData.unbind();
    }

    public void startDestruction(){
        this.isDestroyAnimationPlaying = true;
        this.linkedPortal.isDestroyAnimationPlaying = true;
    }

    public void destroyPortal(){
        if (this.portalFrameBuffer != null){
            this.portalFrameBuffer.dispose();
        }
        isPortalDestroyed = true;
        SeamlessPortals.portalManager.shouldUpdatePortalArray = true;
    }

    public boolean isPortalStable(){
        return this.startingAnimationSequence.isFinished() && !this.isDestroyAnimationPlaying;
    }
}
