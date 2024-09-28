package com.nikrasoff.seamlessportals.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.ScreenUtils;
import com.nikrasoff.seamlessportals.animations.*;
import com.nikrasoff.seamlessportals.extras.FloatContainer;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalIngame;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Sky;

import java.util.HashMap;

public class PortalModelInstance implements IEntityModelInstance {
    private final PortalModel portalModel;

    HashMap<String, ISPAnimation> allAnimations = new HashMap<>();
    ISPAnimation currentAnimation;

    public boolean isPortalMeshGenerated = false;
    private Vector3 portalMeshScale = new Vector3();
    private Vector3 portalMeshLocalOffset = new Vector3(0, 0, 0);
    private final FloatContainer animModelScale = new FloatContainer(1);
    private final Color colorOverlay = Color.CLEAR.cpy();
    private final PerspectiveCamera portalCamera;

    protected PortalModelInstance(PortalModel m){
        this.portalModel = m;
        this.portalCamera = new PerspectiveCamera(GraphicsSettings.fieldOfView.getValue(), (float)Gdx.graphics.getWidth(), (float)Gdx.graphics.getHeight());
        SPAnimationSequence startingAnimationSequence = new SPAnimationSequence(false);
        startingAnimationSequence.add(new DoThingAnimation<>(() -> {
            colorOverlay.set(Color.BLUE);
            animModelScale.set(0);
        }));
        startingAnimationSequence.add(new FloatAnimation(0, 1, 0.5F, this.animModelScale));
        startingAnimationSequence.add(new ColorAnimation(new Color(0, 0, 1, 1), new Color(0, 0, 1,0), 0.5F, this.colorOverlay));
        this.allAnimations.put("start", startingAnimationSequence);
        SPAnimationSequence endingAnimationSequence = new SPAnimationSequence(true);
        endingAnimationSequence.add(new FloatAnimation(1, 0, 0.5F, this.animModelScale));
        endingAnimationSequence.add(new ColorAnimation(new Color(1, 0, 0, 0), new Color(1, 0, 0, 1), 0.25F, this.colorOverlay));
        this.allAnimations.put("end", endingAnimationSequence);
        SPAnimationSequence idleAnimSeq = new SPAnimationSequence(true);
        idleAnimSeq.add(new DoThingAnimation<>(() -> colorOverlay.set(Color.CLEAR)));
        this.allAnimations.put("idle", idleAnimSeq);
    }
    @Override
    public IEntityModel getModel() {
        return this.portalModel;
    }

    @Override
    public void setTint(float v, float v1, float v2, float v3) {
    }

    public void updatePortalMeshScale(PerspectiveCamera playerCamera, Portal portal){
        this.isPortalMeshGenerated = true;
        float halfHeight = (float) (playerCamera.near * Math.tan(Math.toRadians(playerCamera.fieldOfView * 0.5)));
        float halfWidth = halfHeight * (playerCamera.viewportWidth / playerCamera.viewportHeight);

        float portalThickness = (new Vector3(halfWidth, halfHeight, playerCamera.near)).len();

        float camDistToPortalPlane = portal.getDistanceToPortalPlane(playerCamera.position);

        if ((camDistToPortalPlane > portalThickness) || (!portal.getGlobalBoundingBox().contains(playerCamera.position))){
            portalThickness = 0;
        }

        this.portalMeshScale = new Vector3(portal.portalSize.x, portal.portalSize.y, portalThickness);

        boolean camFacingSameDirAsPortal = portal.viewDirection.dot(playerCamera.position.cpy().sub(portal.position)) > 0;
        this.portalMeshLocalOffset = new Vector3(0, 0, portalThickness * (camFacingSameDirAsPortal ? 0.5f : -0.5f));

        this.portalMeshScale.scl(this.animModelScale.getValue());
        this.portalMeshLocalOffset.scl(this.animModelScale.getValue());
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

    private void setCameraNearClipPlane(Camera playerCamera, Portal portal){
        Matrix4 clipPlane = new Matrix4();
        clipPlane.setToLookAt(portal.position, portal.position.cpy().add(portal.viewDirection), new Vector3(0, 1, 0));

        int dot = (int) Math.signum(portal.viewDirection.dot(portal.position.cpy().sub(playerCamera.position)));

        Vector3 viewSpacePos = portal.linkedPortal.position.cpy().mul(this.portalCamera.view);
        Vector3 viewSpaceNormal = portal.linkedPortal.position.cpy().add(portal.linkedPortal.viewDirection).mul(this.portalCamera.view);
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

    private void updatePortalCamera(Camera playerCamera, Portal portal){
        this.portalCamera.viewportHeight = playerCamera.viewportHeight;
        this.portalCamera.viewportWidth = playerCamera.viewportWidth;
        this.portalCamera.fieldOfView = ((IPortalIngame) GameState.IN_GAME).getTempFovForPortals();
        this.portalCamera.near = playerCamera.near;
        this.portalCamera.far = playerCamera.far;

        this.portalCamera.position.set(portal.getPortaledPos(playerCamera.position));
        this.portalCamera.position.add(portal.viewPositionOffset);
        this.portalCamera.direction.set(portal.getPortaledVector(playerCamera.direction));
        this.portalCamera.up.set(portal.getPortaledVector(playerCamera.up));
        this.portalCamera.update();

        if (portal.getDistanceToPortalPlane(playerCamera.position) > 0.02F) setCameraNearClipPlane(playerCamera, portal);
    }

    private Texture createPortalTexture(Camera playerCamera, Portal portal){
        updatePortalCamera(playerCamera, portal);
        if (portalModel.portalFrameBuffer == null){
            portalModel.portalFrameBuffer = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }
        else if (Gdx.graphics.getWidth() != portalModel.portalFrameBuffer.getWidth() || Gdx.graphics.getHeight() != portalModel.portalFrameBuffer.getHeight()){
            if (Gdx.graphics.getWidth() > 0 && Gdx.graphics.getHeight() > 0){
                portalModel.portalFrameBuffer.dispose();
                portalModel.portalFrameBuffer = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            }
        }
        portalModel.portalFrameBuffer.begin();
        ScreenUtils.clear(Sky.currentSky.currentSkyColor, true);
        Sky.currentSky.drawSky(this.portalCamera);
        GameSingletons.zoneRenderer.render(InGame.world.getZone(portal.zoneID), this.portalCamera);
        Gdx.gl.glDepthMask(true);
        InGame.world.getZone(portal.linkedPortal.zoneID).forEachEntity((e) -> {
            if (e instanceof Portal){
                return;
            }
            ((IModEntity) e).renderNoAnim(portalCamera);
        });

        portalModel.portalFrameBuffer.end();

        return portalModel.portalFrameBuffer.getColorBufferTexture();
    }

    public OrientedBoundingBox getMeshBoundingBox(Matrix4 transformMatrix){
        BoundingBox meshBB = new BoundingBox();
        Vector3 tmpScale = this.portalMeshScale.cpy();
        tmpScale.z = 0;

        meshBB.min.set(tmpScale).scl(-0.5F);
        meshBB.max.set(tmpScale).scl(0.5F);
        meshBB.update();

        return new OrientedBoundingBox(meshBB, transformMatrix.cpy().inv());
    }

    public boolean isAnimationOver(){
        return currentAnimation.isFinished();
    }

    @Override
    public void render(Entity entity, Camera camera, Matrix4 matrix4) {
//        System.out.println("Rendered: " + ((Portal) entity).getPortalID());
        if (!this.isPortalMeshGenerated){
            this.updatePortalMeshScale((PerspectiveCamera) camera, (Portal) entity);
        }
        portalModel.renderDebug((Portal) entity, camera);
        if (((Portal) entity).linkedPortal == null || ((Portal) entity).isPortalDestroyed){
            return;
        }
        if (this.currentAnimation != null){
            this.currentAnimation.update(Gdx.graphics.getDeltaTime());
        }
        if (!((Portal) entity).zoneID.equals(InGame.getLocalPlayer().zoneId) || ((Portal) entity).isPortalDestroyed || ((Portal) entity).position.dst(camera.position) > 50){
            return;
        }
        if (!camera.frustum.boundsInFrustum(this.getMeshBoundingBox(matrix4)) && ((Portal) entity).getDistanceToPortalPlane(camera.position) > camera.near){
            return;
        }

        if (portalModel.mesh == null){
            portalModel.mesh = portalModel.createModel();
        }
        if (portalModel.shader == null){
            portalModel.shader = new GameShader(Identifier.of("seamlessportals", "shaders/portal.vert.glsl"), Identifier.of("seamlessportals", "shaders/portal.frag.glsl"));
            portalModel.shader.allVertexAttributesObj = new VertexAttributes(VertexAttribute.Position());
        }

        Texture portalTexture = this.createPortalTexture(camera, (Portal) entity);
        this.updatePortalMeshScale((PerspectiveCamera) camera, (Portal) entity);


        SharedQuadIndexData.bind();
        portalModel.shader.bind(camera);

        Vector2 screenSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float[] tmpVec = new float[2];
        tmpVec[0] = screenSize.x;
        tmpVec[1] = screenSize.y;
        portalModel.shader.shader.setUniform2fv("screenSize", tmpVec, 0, 2);
        portalModel.shader.bindOptionalUniform4f("overlayColor", this.colorOverlay);
        portalModel.shader.bindOptionalTexture("screenTex", portalTexture, 1);
        portalModel.shader.bindOptionalMatrix4("transMatrix", matrix4.cpy().inv());
        portalModel.shader.bindOptionalUniform3f("localOffset", this.portalMeshLocalOffset);
        portalModel.shader.bindOptionalUniform3f("portScale", this.portalMeshScale);
        portalModel.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
        portalModel.mesh.render(portalModel.shader.shader, 4);
        SharedQuadIndexData.unbind();
    }

    @Override
    public Color getCurrentAmbientColor() {
        return new Color();
    }

    @Override
    public void setCurrentAnimation(String s) {
        this.currentAnimation = this.allAnimations.get(s);
        this.currentAnimation.restart();
    }

    @Override
    public void setEntityModel(IEntityModel iEntityModel) {

    }
}