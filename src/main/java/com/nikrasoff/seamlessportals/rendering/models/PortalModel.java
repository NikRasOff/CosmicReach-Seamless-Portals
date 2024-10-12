package com.nikrasoff.seamlessportals.rendering.models;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.rendering.shaders.TwoSidedShader;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.util.Identifier;

public class PortalModel implements IEntityModel, Disposable {
    public static boolean debugReady = false;
    private static ShapeRenderer shapeRenderer;
    FrameBuffer portalFrameBuffer;
    public static Renderable renderable;
    public static TwoSidedShader shader;

    public static void create(){
        renderable = new Renderable();
        SeamlessPortalsRenderUtil.cubeModelInstance.getRenderable(renderable);
        shader = new TwoSidedShader(Identifier.of(SeamlessPortals.MOD_ID, "shaders/default.vert.glsl"), Identifier.of(SeamlessPortals.MOD_ID, "shaders/portal.frag.glsl"));
        shader.init();
    }

    public PortalModel(){
    }



    private void initialiseDebug(){
        shapeRenderer = new ShapeRenderer();
        debugReady = true;
    }

    private void disableDebug(){
        if (shapeRenderer != null){
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        debugReady = false;
    }

    void renderDebug(Portal portal, Camera camera){
        if (SeamlessPortals.debugOutlines){
            if (!debugReady) initialiseDebug();
        }
        else if (debugReady){
            disableDebug();
        }
        if (debugReady){
            shapeRenderer.setProjectionMatrix(camera.combined);
        }

        OrientedBoundingBox portalBigBB = portal.getMeshBoundingBox();
        if (debugReady){
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setTransformMatrix(portalBigBB.transform);
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.box(portalBigBB.getBounds().min.x, portalBigBB.getBounds().min.y, portalBigBB.getBounds().min.z, portalBigBB.getBounds().getWidth(), portalBigBB.getBounds().getHeight(), -portalBigBB.getBounds().getDepth());
            shapeRenderer.setColor(0, 0, 1, 1);
            shapeRenderer.line(Vector3.Zero, new Vector3(0, 0, -1));
            shapeRenderer.end();
        }
    }

    @Override
    public void dispose() {
        if (this.portalFrameBuffer != null){
            this.portalFrameBuffer.dispose();
        }
    }

    @Override
    public IEntityModelInstance getNewModelInstance() {
        return new PortalModelInstance(this);
    }
}