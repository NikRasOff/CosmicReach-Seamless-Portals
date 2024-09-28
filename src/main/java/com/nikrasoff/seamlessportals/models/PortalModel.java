package com.nikrasoff.seamlessportals.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.nikrasoff.seamlessportals.animations.*;
import com.nikrasoff.seamlessportals.config.SeamlessPortalsConfig;
import com.nikrasoff.seamlessportals.extras.FloatContainer;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalIngame;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.world.Sky;

import java.util.HashMap;

public class PortalModel implements IEntityModel, Disposable {
    public static boolean debugReady = false;
    private static ShapeRenderer shapeRenderer;
    FrameBuffer portalFrameBuffer;
    GameMesh mesh;
    GameShader shader;

    GameMesh createModel(){
        MeshData meshData = new MeshData(ChunkShader.DEFAULT_BLOCK_SHADER, RenderOrder.DEFAULT);

        BlockState.getInstance("seamlessportals:ph_portal[default]").addVertices(meshData, 0, 0, 0);
        return meshData.toSharedIndexMesh(true);
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
        if (SeamlessPortalsConfig.INSTANCE.debugOutlines.value()){
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
