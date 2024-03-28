package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.blocks.BlockState;

public class PulseEffect {
    public static Array<PulseEffect> allPulseEffects = new Array<>();

    public Vector3 position;
    public Vector3 startModelScale;
    public Vector3 modelScale;
    public Vector3 targetModelScale;
    public Color startModelColor;
    public Color modelColor;
    public Color targetModelColor;

    public Vector3 fadeModelScale;
    public Color fadeModelColor;

    public float fadeTime;
    public float lifetime;
    public float aliveFor;

    static GameMesh mesh = createModel();
    static GameShader shader = new GameShader("effect_pulse.vert.glsl", "effect_pulse.frag.glsl");

    public boolean fading = false;

    public PulseEffect(Vector3 pos, Vector3 startingModelScale, Vector3 finalModelScale, Color startingColor, Color finalColor, float changeTime, Vector3 fadeoutScale, Color fadeoutColor, float fadeoutTime){
        this.aliveFor = 0;
        this.lifetime = changeTime;
        this.position = pos;
        this.startModelColor = startingColor;
        this.startModelScale = startingModelScale;
        this.modelColor = startingColor;
        this.modelScale = startingModelScale;
        this.targetModelScale = finalModelScale;
        this.targetModelColor = finalColor;
        this.fadeModelColor = fadeoutColor;
        this.fadeModelScale = fadeoutScale;
        this.fadeTime = fadeoutTime;

        allPulseEffects.add(this);
    }

    private static GameMesh createModel(){
        MeshData meshData = new MeshData(ChunkShader.DEFAULT_BLOCK_SHADER, RenderOrder.TRANSPARENT);

        BlockState.getInstance("seamlessportals:ph_destabiliser_pulse[default]").addVertices(meshData, 0, 0, 0);
        return meshData.toSharedIndexMesh(true);
    }

    private void updateModelScaleAndColorAlive(){
        float progress = this.aliveFor / this.lifetime;

        this.modelColor.set(this.startModelColor.cpy().lerp(this.targetModelColor, progress));
        this.modelScale.set(this.startModelScale.cpy().lerp(this.targetModelScale, progress));
    }

    private void updateModelScaleAndColorFading(){
        float progress = (this.aliveFor - this.lifetime) / this.fadeTime;

        this.modelColor.set(this.targetModelColor.cpy().lerp(this.fadeModelColor, progress));
        this.modelScale.set(this.targetModelScale.cpy().lerp(this.fadeModelScale, progress));
    }

    public static void renderPulseEffects(Camera playerCamera){
        for (PulseEffect pulseEffect : allPulseEffects){
            pulseEffect.render(playerCamera);
        }
    }

    public void setToFading(){
        this.fading = true;
    }

    public void render(Camera playerCamera){
        if (fading) this.updateModelScaleAndColorFading();
        else this.updateModelScaleAndColorAlive();

        this.aliveFor += Gdx.graphics.getDeltaTime();

        SharedQuadIndexData.bind();
        shader.bind(playerCamera);

        float[] tmpVec4 = new float[4];
        tmpVec4[0] = this.modelColor.r;
        tmpVec4[1] = this.modelColor.g;
        tmpVec4[2] = this.modelColor.b;
        tmpVec4[3] = this.modelColor.a;

        shader.shader.setUniformMatrix("u_projViewTrans", playerCamera.combined);
        shader.bindOptionalUniform3f("posOffset", this.position);
        shader.bindOptionalUniform3f("modelScale", this.modelScale);
        shader.shader.setUniform4fv("modelColor", tmpVec4, 0, 4);

        mesh.bind(shader.shader);
        mesh.render(shader.shader, 4);
        mesh.unbind(shader.shader);
        SharedQuadIndexData.unbind();
        if (this.aliveFor > this.lifetime && !this.fading){
            this.setToFading();
        }
        if (this.aliveFor > this.lifetime + this.fadeTime){
            System.out.println("ye");
            allPulseEffects.removeValue(this, true);
        }
    }

}
