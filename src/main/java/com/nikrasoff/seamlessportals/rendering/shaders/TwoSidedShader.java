package com.nikrasoff.seamlessportals.rendering.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.util.Identifier;

public class TwoSidedShader  implements Shader {
    public ShaderProgram program;
    Camera camera;
    RenderContext context;

    Identifier vertIdentifier;
    Identifier fragIdentifier;

    public TwoSidedShader(Identifier vert, Identifier frag){
        this.vertIdentifier = vert;
        this.fragIdentifier = frag;
    }

    @Override
    public void init () {
        String vert = GameAssetLoader.loadAsset(vertIdentifier).readString();
        String frag = GameAssetLoader.loadAsset(fragIdentifier).readString();
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
        program.setUniformMatrix("u_projViewTrans", camera.combined);
        context.setDepthTest(GL20.GL_LESS);
    }
    @Override
    public void render (Renderable renderable) {
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        context.setCullFace(GL20.GL_FRONT);
        renderable.meshPart.render(program);
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
