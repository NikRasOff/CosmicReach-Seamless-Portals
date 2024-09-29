package com.nikrasoff.seamlessportals.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.animations.ColorAnimation;
import com.nikrasoff.seamlessportals.animations.SPAnimationSequence;
import com.nikrasoff.seamlessportals.animations.Vector3Animation;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.meshes.IntIndexData;
import finalforeach.cosmicreach.rendering.meshes.IntIndexedMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;

public class PulseEffect {
    public static Array<PulseEffect> allPulseEffects = new Array<>();

    public SPAnimationSequence animationSequence = new SPAnimationSequence(false);

    public Vector3 position;
    public String zoneID;
    public Vector3 modelScale = new Vector3();
    public Color modelColor = new Color();

    public static GameMesh mesh;
    public static GameShader shader;

    public PulseEffect(Vector3 pos, Zone zone, Vector3 startingModelScale, Vector3 finalModelScale, Color startingColor, Color finalColor, float changeTime, Vector3 fadeoutScale, Color fadeoutColor, float fadeoutTime){
        this.position = pos;

        SPAnimationSequence startingAnimation = new SPAnimationSequence(true);
        startingAnimation.add(new Vector3Animation(startingModelScale, finalModelScale, changeTime, this.modelScale));
        startingAnimation.add(new ColorAnimation(startingColor, finalColor, changeTime, this.modelColor));
        animationSequence.add(startingAnimation);

        animationSequence.add(new ColorAnimation(finalColor, fadeoutColor, fadeoutTime, this.modelColor));

        allPulseEffects.add(this);
        this.zoneID = zone.zoneId;
    }

    private static GameMesh createModel(){
        MeshData meshData = new MeshData(shader, RenderOrder.DEFAULT);

        BlockState.getInstance("seamlessportals:ph_destabiliser_pulse[default]").addVertices(meshData, 0, 0, 0);
        return meshData.toSharedIndexMesh(true);
    }

    public static void renderPulseEffects(Camera playerCamera){
        Player player = InGame.getLocalPlayer();
        for (PulseEffect pulseEffect : allPulseEffects){
            if (pulseEffect.zoneID.equals(player.zoneId)){
                pulseEffect.render(playerCamera);
            }
        }
    }

    public void render(Camera playerCamera){
        this.animationSequence.update(Gdx.graphics.getDeltaTime());
        if (this.animationSequence.isFinished()){
            allPulseEffects.removeValue(this, true);
            return;
        }
        SharedQuadIndexData.bind();

        shader.bind(playerCamera);
        shader.bindOptionalMatrix4("u_projViewTrans", playerCamera.combined);
        shader.bindOptionalUniform3f("posOffset", this.position);
        shader.bindOptionalUniform3f("modelScale", this.modelScale);
        shader.bindOptionalUniform4f("modelColor", this.modelColor);

        mesh.bind(shader.shader);
        mesh.render(shader.shader, 4);
        mesh.unbind(shader.shader);
        shader.unbind();
        SharedQuadIndexData.unbind();
    }

    static {
        shader = new GameShader(Identifier.of("seamlessportals", "shaders/effect_pulse.vert.glsl"), Identifier.of("seamlessportals", "shaders/effect_pulse.frag.glsl"));
        shader.allVertexAttributesObj = new VertexAttributes(VertexAttribute.Position());
        mesh = createModel();
    }
}
