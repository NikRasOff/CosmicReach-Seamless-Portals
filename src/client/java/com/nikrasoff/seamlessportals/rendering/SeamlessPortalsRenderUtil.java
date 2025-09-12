package com.nikrasoff.seamlessportals.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.shaders.ModelShader;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;
import java.util.Map;

public class SeamlessPortalsRenderUtil {
    public static RenderContext renderContext;
    public static Model cubeModel;
    public static ModelInstance cubeModelInstance;
    public static AssetManager assets;
    public static Map<String, Model> modelMap;
    private static ModelShader shader;

    private static final float[] bones = new float[4 * 16];
    private static final Matrix4 idtMatrix = new Matrix4();

    private static final float[] tmpVec4 = new float[4];
    private static final float[] tmpVec3 = new float[3];
    private static final Color tintColor = new Color();
    private static final BlockPosition tmpBlockPos1 = new BlockPosition(null, 0, 0, 0);
    private static final BlockPosition tmpBlockPos2 = new BlockPosition(null, 0, 0, 0);

    public static void initialise(){
        modelMap = new HashMap<>();
        renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.LRU));
        ModelBuilder modelBuilder = new ModelBuilder();
        cubeModel = modelBuilder.createBox(1, 1, 1, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates);
        cubeModelInstance = new ModelInstance(cubeModel);
        assets = new AssetManager();
        shader = new ModelShader();
        shader.init();
    }

    public static void renderModelSliced(ModelInstance instance, Camera camera, Vector3 worldPos, Portal portal, boolean isDuplicate){
        if (portal != null){
            shader.program.bind();
            shader.program.setUniformi("u_turnOnSlicing", 1);
            if (isDuplicate){
                tmpVec3[0] = portal.linkedPortal.position.x;
                tmpVec3[1] = portal.linkedPortal.position.y;
                tmpVec3[2] = portal.linkedPortal.position.z;
                shader.program.setUniform3fv("u_portalOrigin", tmpVec3, 0, 3);
                tmpVec3[0] = portal.linkedPortal.viewDirection.x;
                tmpVec3[1] = portal.linkedPortal.viewDirection.y;
                tmpVec3[2] = portal.linkedPortal.viewDirection.z;
                shader.program.setUniform3fv("u_portalNormal", tmpVec3, 0, 3);
                shader.program.setUniformi("u_invertPortalNormal", Math.max(portal.getPortalSide(worldPos), 0));
            }
            else {
                tmpVec3[0] = portal.position.x;
                tmpVec3[1] = portal.position.y;
                tmpVec3[2] = portal.position.z;
                shader.program.setUniform3fv("u_portalOrigin", tmpVec3, 0, 3);
                tmpVec3[0] = portal.viewDirection.x;
                tmpVec3[1] = portal.viewDirection.y;
                tmpVec3[2] = portal.viewDirection.z;
                shader.program.setUniform3fv("u_portalNormal", tmpVec3, 0, 3);
                shader.program.setUniformi("u_invertPortalNormal", Math.max(-portal.getPortalSide(worldPos), 0));
            }
        }
        renderModel(instance, camera, worldPos, true, true);
        shader.program.setUniformi("u_turnOnSlicing", 0);
    }

    public static void renderModel(ModelInstance instance, Camera camera, Vector3 worldPos, boolean useAmbientLight, boolean applyFog){
        // I'm not using ModelBatch primarily because I need to render the same ModelInstance several times over in a single frame
        Zone z = InGame.getLocalPlayer().getZone();
        Sky s = Sky.getCurrentSky(z);
        shader.begin(camera, renderContext);
        ((TextureAttribute) instance.materials.get(0).get(TextureAttribute.Diffuse)).textureDescription.texture.bind(1);
        shader.program.setUniformi("u_diffuseTex", 1);
        if (!applyFog) {
            shader.program.setUniformf("u_fogDensity", 0f);
        }
        if (useAmbientLight){
            Entity.setLightingColor(z, worldPos, s.currentAmbientColor, tintColor, tmpBlockPos1, tmpBlockPos2);
        }
        else {
            tintColor.set(Color.WHITE);
        }
        tmpVec4[0] = tintColor.r;
        tmpVec4[1] = tintColor.g;
        tmpVec4[2] = tintColor.b;
        tmpVec4[3] = tintColor.a;
        shader.program.setUniform4fv("u_ambientLight", tmpVec4, 0, 4);

        Pool<Renderable> renderablePool = new Pool<>() {
            protected Renderable newObject() {
                return new Renderable();
            }

            public Renderable obtain() {
                Renderable renderable = super.obtain();
                renderable.environment = null;
                renderable.material = null;
                renderable.meshPart.set("", null, 0, 0, 0);
                renderable.shader = null;
                renderable.userData = null;
                return renderable;
            }
        };
        Array<Renderable> renderableArray = new Array<>();
        instance.getRenderables(renderableArray, renderablePool);
        for (Renderable r : renderableArray){
            for (int i = 0; i < bones.length; i += 16){
                int idx = i / 16;
                if (r.bones != null && idx < r.bones.length && r.bones[idx] != null){
                    System.arraycopy(r.bones[idx].val, 0, bones, i, 16);
                }
                else {
                    System.arraycopy(idtMatrix.val, 0, bones, i, 16);
                }
            }
            shader.program.setUniformMatrix4fv("u_bones", bones, 0, bones.length);
            shader.render(r);
        }
        shader.end();
    }

    public static Model loadModel(Identifier modelId){
        Model m = modelMap.get(modelId.toString());
        if (m != null){
            return m;
        }
        String modelPath = "assets/" + modelId.toPath();
        if (Gdx.files.classpath(modelPath).exists()){
            assets.load(modelPath, Model.class);
            assets.finishLoadingAsset(modelPath);
            m = assets.get(modelPath, Model.class);

            for (Material mat : m.materials){
                TextureAttribute ta = (TextureAttribute) mat.get(TextureAttribute.Diffuse);
                ta.textureDescription.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            modelMap.put(modelId.toString(), m);
            return m;
        }
        return null;
    }

    public static ModelInstance getModelInstance(Identifier modelId){
        Model m = loadModel(modelId);
        if (m != null){
            return new ModelInstance(m);
        }
        return null;
    }
}
