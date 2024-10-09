package com.nikrasoff.seamlessportals.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.animations.ColorAnimation;
import com.nikrasoff.seamlessportals.animations.SPAnimationSequence;
import com.nikrasoff.seamlessportals.animations.Vector3Animation;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.rendering.shaders.TwoSidedShader;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;

public class PulseEffect {
    public static Array<PulseEffect> allPulseEffects = new Array<>();

    public SPAnimationSequence animationSequence = new SPAnimationSequence(false);

    public Vector3 position;
    public String zoneID;
    public Vector3 modelScale = new Vector3();
    public Color modelColor = new Color();
    public static Renderable renderable;
    public static TwoSidedShader shader;

    private static Matrix4 tmpMat = new Matrix4();
    private static float[] tmpVec4 = new float[4];

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

    public static void create(){
        renderable = new Renderable();
        SeamlessPortalsRenderUtil.cubeModelInstance.getRenderable(renderable);
        shader = new TwoSidedShader(Identifier.of(SeamlessPortals.MOD_ID, "shaders/default.vert.glsl"), Identifier.of(SeamlessPortals.MOD_ID, "shaders/effect_pulse.frag.glsl"));
        shader.init();
    }

    public static void renderPulseEffects(Camera playerCamera){
        Player player = InGame.getLocalPlayer();
        for (PulseEffect pulseEffect : allPulseEffects){
            if (pulseEffect.zoneID.equals(player.zoneId)){
                pulseEffect.render(playerCamera);
            }
        }
    }

    public void render(Camera playerCamera) {
        this.animationSequence.update(Gdx.graphics.getDeltaTime());
        if (this.animationSequence.isFinished()) {
            allPulseEffects.removeValue(this, true);
            return;
        }

        tmpMat.idt();
        tmpMat.translate(position);
        tmpMat.scale(modelScale.x, modelScale.y, modelScale.z);

        renderable.worldTransform.set(tmpMat);
        shader.begin(playerCamera, SeamlessPortalsRenderUtil.renderContext);

        tmpVec4[0] = modelColor.r;
        tmpVec4[1] = modelColor.g;
        tmpVec4[2] = modelColor.b;
        tmpVec4[3] = modelColor.a;

        shader.program.setUniform4fv("u_modelColor", tmpVec4, 0, 4);
        shader.render(renderable);
        shader.end();
    }
}
