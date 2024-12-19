package com.nikrasoff.seamlessportals.rendering.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Sky;

public class ModelShader implements Shader {
    public ShaderProgram program;
    Camera camera;
    RenderContext context;

    private static final Vector3 tmpVec3 = new Vector3();
    private static final float[] tv3 = new float[3];

    @Override
    public void init () {
        String vert = GameAssetLoader.loadAsset(Identifier.of("seamlessportals", "shaders/model.vert.glsl")).readString();
        String frag = GameAssetLoader.loadAsset(Identifier.of("seamlessportals", "shaders/model.frag.glsl")).readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) throw new GdxRuntimeException(program.getLog());
    }
    @Override
    public void dispose () {
        program.dispose();
    }
    @Override
    public void begin (Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.bind();
        program.setUniformMatrix("u_projTrans", camera.combined);
        Sky sky = Sky.currentSky;
        program.setUniformf("u_fogDensity", sky.fogDensity);
        sky.getSunDirection(tmpVec3);
        tv3[0] = tmpVec3.x;
        tv3[1] = tmpVec3.y;
        tv3[2] = tmpVec3.z;
        program.setUniform3fv("u_sunDirection", tv3, 0, 3);
        tv3[0] = camera.position.x;
        tv3[1] = camera.position.y;
        tv3[2] = camera.position.z;
        program.setUniform3fv("u_cameraPosition", tv3, 0, 3);
        tv3[0] = sky.currentAmbientColor.r;
        tv3[1] = sky.currentSkyColor.g;
        tv3[2] = sky.currentAmbientColor.b;
        program.setUniform3fv("u_skyAmbientColor", tv3, 0, 3);
        context.setDepthTest(GL20.GL_LESS);
    }
    @Override
    public void render (Renderable renderable) {
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        context.setCullFace(GL20.GL_BACK);
        renderable.meshPart.render(program);
    }
    @Override
    public void end () {}
    @Override
    public int compareTo (Shader other) {
        return 0;
    }
    @Override
    public boolean canRender (Renderable instance) {
        return true;
    }
}
