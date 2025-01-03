package com.nikrasoff.seamlessportals.rendering.models;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.rendering.shaders.*;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;

public class PortalModel implements IEntityModel, Disposable {
    public static boolean debugReady = false;
    private static ShapeRenderer shapeRenderer;
    FrameBuffer portalFrameBuffer;
    public static Renderable renderable;
    public static DefaultPortalShader portalShader;
    public static NullPortalShader nullPortalShader;
    public static HPGPortalShader hpgPortalShader;
    public static HPGNullPortalShader hpgNullPortalShader;
    public static Texture noiseTexture;
    public static Array<Texture> convEventTextures = new Array<>();
    public static PortalModel model;

    public static void create(){
        renderable = new Renderable();
        SeamlessPortalsRenderUtil.cubeModelInstance.getRenderable(renderable);
        portalShader = new DefaultPortalShader();
        portalShader.init();
        nullPortalShader = new NullPortalShader();
        nullPortalShader.init();
        hpgPortalShader = new HPGPortalShader();
        hpgPortalShader.init();
        hpgNullPortalShader = new HPGNullPortalShader();
        hpgNullPortalShader.init();
        model = new PortalModel();
        noiseTexture = GameAssetLoader.getTexture(Identifier.of(SeamlessPortalsConstants.MOD_ID, "textures/special/funky_noise.png"));
        noiseTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        noiseTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        convEventTextures.add(GameAssetLoader.getTexture(Identifier.of(SeamlessPortalsConstants.MOD_ID, "textures/special/convergence_event1.png")));
        convEventTextures.add(GameAssetLoader.getTexture(Identifier.of(SeamlessPortalsConstants.MOD_ID, "textures/special/convergence_event2.png")));
        convEventTextures.add(GameAssetLoader.getTexture(Identifier.of(SeamlessPortalsConstants.MOD_ID, "textures/special/convergence_event3.png")));
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
        if (UI.renderDebugInfo){
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
