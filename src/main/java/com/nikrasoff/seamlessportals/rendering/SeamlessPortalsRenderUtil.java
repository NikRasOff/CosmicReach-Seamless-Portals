package com.nikrasoff.seamlessportals.rendering;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;

public class SeamlessPortalsRenderUtil {
    public static RenderContext RENDER_CONTEXT;
    public static Model CUBE_MODEL;
    public static ModelInstance CUBE_MODEL_INSTANCE;
    public static void initialise(){
        RENDER_CONTEXT = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.LRU));
        ModelBuilder modelBuilder = new ModelBuilder();
        CUBE_MODEL = modelBuilder.createBox(1, 1, 1, new Material(), VertexAttributes.Usage.Position);
        CUBE_MODEL_INSTANCE = new ModelInstance(CUBE_MODEL);
    }
}
