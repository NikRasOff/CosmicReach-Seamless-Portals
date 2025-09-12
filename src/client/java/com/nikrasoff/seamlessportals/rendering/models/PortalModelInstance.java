package com.nikrasoff.seamlessportals.rendering.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.nikrasoff.seamlessportals.SPClientConstants;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.animations.*;
import com.nikrasoff.seamlessportals.extras.ClientPortalEntityTools;
import com.nikrasoff.seamlessportals.extras.FloatContainer;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalIngame;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalZoneRenderer;
import com.nikrasoff.seamlessportals.portals.HPGPortal;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.api.IPortalEntityRenderer;
import com.nikrasoff.seamlessportals.rendering.shaders.PortalShader;
import finalforeach.cosmicreach.rendering.IRenderable;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.entities.IEntityAnimation;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.world.Sky;

import java.util.HashMap;

public class PortalModelInstance implements IEntityModelInstance {
    private PortalModel portalModel;

    HashMap<String, ISPAnimation> allAnimations = new HashMap<>();
    ISPAnimation currentAnimation;

    public boolean isPortalMeshGenerated = false;
    public Vector3 portalMeshScale = new Vector3();
    private Vector3 portalMeshLocalOffset = new Vector3(0, 0, 0);
    private final FloatContainer animModelScale = new FloatContainer(1);
    public final Color colorOverlay = Color.CLEAR.cpy();
    private final PerspectiveCamera portalCamera;
    public Texture portalTexture;
    private boolean portalCloseToCamera = false;

    private static final float[] tmpVec2 = new float[2];
    private static final float[] tmpVec4 = new float[4];

    protected PortalModelInstance(PortalModel m){
        this.portalModel = m;
        this.portalCamera = new PerspectiveCamera(GraphicsSettings.fieldOfView.getValue(), (float)Gdx.graphics.getWidth(), (float)Gdx.graphics.getHeight());
        SPAnimationSequence startingAnimationSequence = new SPAnimationSequence(false);
        startingAnimationSequence.add(new DoThingAnimation<>(() -> {
            colorOverlay.set(Color.BLUE);
            animModelScale.set(0);
        }));
        startingAnimationSequence.add(new FloatAnimation(0, 1, 0.25F, this.animModelScale));
        startingAnimationSequence.add(new ColorAnimation(new Color(0, 0, 1, 1), new Color(0, 0, 1,0), 0.25F, this.colorOverlay));
        this.allAnimations.put("start", startingAnimationSequence);
        SPAnimationSequence rebindAnimation = new SPAnimationSequence(false);
        rebindAnimation.add(new DoThingAnimation<>(() -> {
            colorOverlay.set(Color.BLUE);
            animModelScale.set(1);
        }));
        rebindAnimation.add(new WaitAnimation(0.25f));
        rebindAnimation.add(new ColorAnimation(new Color(0, 0, 1, 1), new Color(0, 0, 0, 0), 0.25f, this.colorOverlay));
        this.allAnimations.put("rebind", rebindAnimation);
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

        if ((camDistToPortalPlane > portalThickness) || (!portal.getFatBoundingBox().contains(playerCamera.position))){
            portalThickness = 0.01f;
            portalCloseToCamera = false;
        }
        else{
            portalCloseToCamera = true;
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
        int dot = (int) Math.signum(portal.viewDirection.dot(portal.position.cpy().sub(playerCamera.position)));

        Vector3 tmpPortalPos = portal.linkedPortal.position.cpy();
        tmpPortalPos.add(portal.linkedPortal.viewDirection.cpy().scl(portal.getPortalSide(playerCamera.position) * 0.01f));

        Vector3 viewSpacePos = tmpPortalPos.cpy().mul(this.portalCamera.view);
        Vector3 viewSpaceNormal = tmpPortalPos.cpy().add(portal.linkedPortal.viewDirection).mul(this.portalCamera.view);
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
    }

    private void updatePortalCamera(Camera playerCamera, Portal portal){
        this.portalCamera.viewportHeight = playerCamera.viewportHeight;
        this.portalCamera.viewportWidth = playerCamera.viewportWidth;
        this.portalCamera.fieldOfView = ((IPortalIngame) GameState.IN_GAME).cosmicReach_Seamless_Portals$getTempFovForPortals();
        this.portalCamera.near = playerCamera.near;
        this.portalCamera.far = playerCamera.far;

        this.portalCamera.position.set(portal.getPortaledPos(playerCamera.position));
        this.portalCamera.position.add(portal.viewPositionOffset);
        this.portalCamera.direction.set(portal.getPortaledVector(playerCamera.direction));
        this.portalCamera.up.set(portal.getPortaledVector(playerCamera.up));
        this.portalCamera.update();

        if (portal.getDistanceToPortalPlane(playerCamera.position) > 0.02F) setCameraNearClipPlane(playerCamera, portal);
    }

    public Texture createPortalTexture(Camera playerCamera, Portal portal){
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
        // TODO: figure out how to optimise this
        portalModel.portalFrameBuffer.begin();
        ScreenUtils.clear(Sky.currentSky.currentSkyColor, true);
        Sky.currentSky.drawSky(this.portalCamera);
        if (GameSingletons.zoneRenderer instanceof IPortalZoneRenderer zr) zr.cosmicReach_Seamless_Portals$renderThroughPortal(portal.zone, this.portalCamera);
        else GameSingletons.zoneRenderer.render(portal.zone, this.portalCamera);
        Gdx.gl.glDepthMask(true);
        if (portal.linkedPortal != null){
            for (IRenderable renderable : portal.linkedPortal.zone.allRenderableBlockEntities){
                if (renderable != null){
                    renderable.onRender(portalCamera);
                }
            }

            portal.linkedPortal.zone.forEachEntity((e) -> {
                IPortalEntityRenderer r = SPClientConstants.getPortalEntityRenderer(e.getClass());
                if (r != null){
                    if (r.isCloseToPortal(e, portal)){
                        if (!(e == InGame.getLocalPlayer().getEntity() && GameSingletons.client().isFirstPerson()) && !portal.linkedPortal.isNotOnSameSideOfPortal(portalCamera.position, portal.linkedPortal.getPortaledPos(e.position))){
                            if (ClientPortalEntityTools.isJustTeleported(e)){
                                r.renderSliced(e, portalCamera, portal);
                            }
                            else {
                                r.renderDuplicate(e, portalCamera, portal);
                            }
                        }
                    }
                    if (r.isCloseToPortal(e, portal.linkedPortal)){
                        if (portal.linkedPortal.isNotOnSameSideOfPortal(portalCamera.position, e.position)){
                            if (ClientPortalEntityTools.isJustTeleported(e)){
                                r.renderDuplicate(e, portalCamera, portal.linkedPortal);
                            }
                            else {
                                r.renderSliced(e, portalCamera, portal.linkedPortal);
                            }
                        }
                    }
                    else {
                        r.render(e, portalCamera);
                    }
                }
            });

            GameState.IN_GAME.gameParticles.render(portalCamera, 0);
            portalModel.portalFrameBuffer.end();
        }

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
    public void render(Entity entity, Camera camera, Matrix4 matrix4, boolean shouldRender) {
        if (!this.isPortalMeshGenerated){
            this.updatePortalMeshScale((PerspectiveCamera) camera, (Portal) entity);
        }
        portalModel.renderDebug((Portal) entity, camera);
        if (((Portal) entity).isPortalDestroyed){
            return;
        }
        if (this.currentAnimation != null){
            this.currentAnimation.update(Gdx.graphics.getDeltaTime());
        }
        if (!shouldRender) return;
        if (entity.zone != GameSingletons.client().getLocalPlayer().getZone() || ((Portal) entity).isPortalDestroyed || (entity).position.dst(camera.position) > 50){
            return;
        }
        if (!camera.frustum.boundsInFrustum(this.getMeshBoundingBox(matrix4)) && ((Portal) entity).getDistanceToPortalPlane(camera.position) > camera.near){
            return;
        }

        Matrix4 renderMatrix = matrix4.cpy();

        PortalShader currentShader;
        if (((Portal) entity).linkedPortal == null){
            if (entity instanceof HPGPortal) currentShader = PortalModel.hpgNullPortalShader;
            else currentShader = PortalModel.nullPortalShader;
        }
        else {
            if (entity instanceof HPGPortal) currentShader = PortalModel.hpgPortalShader;
            else currentShader = PortalModel.portalShader;

            portalTexture = this.createPortalTexture(camera, (Portal) entity);
        }
        this.updatePortalMeshScale((PerspectiveCamera) camera, (Portal) entity);

        currentShader.begin(camera, SeamlessPortalsRenderUtil.renderContext);

        currentShader.setUniforms(this, (Portal) entity);

        PortalModel.renderable.worldTransform.set(renderMatrix).inv().translate(this.portalMeshLocalOffset).scale(this.portalMeshScale.x, this.portalMeshScale.y, this.portalMeshScale.z);
        if (portalCloseToCamera){
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        }
        currentShader.render(PortalModel.renderable);
        if (portalCloseToCamera){
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        }
        currentShader.end();
    }

    @Override
    public Color getCurrentAmbientColor() {
        return new Color();
    }

    @Override
    public void addAnimation(String s) {
        this.currentAnimation = this.allAnimations.get(s);
        if (this.currentAnimation != null){
            this.currentAnimation.restart();
        }
        else {
            SeamlessPortals.LOGGER.warn("Couldn't find portal animation: {}", s);
        }
    }

    @Override
    public void removeAnimation(String s) {
        throw new RuntimeException("Removing portal animations not implemented");
    }

    @Override
    public void removeAnimation(IEntityAnimation iEntityAnimation) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void setEntityModel(IEntityModel iEntityModel) {
        this.portalModel = (PortalModel) iEntityModel;
    }

    @Override
    public Array<? extends IEntityAnimation> getAnimations() {
        return null;
    }

    @Override
    public void shadowAnimations(Array<? extends IEntityAnimation> array) {

    }
}
