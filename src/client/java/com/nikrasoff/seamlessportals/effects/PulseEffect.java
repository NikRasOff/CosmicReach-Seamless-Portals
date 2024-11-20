package com.nikrasoff.seamlessportals.effects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.animations.ColorAnimation;
import com.nikrasoff.seamlessportals.animations.SPAnimationSequence;
import com.nikrasoff.seamlessportals.animations.Vector3Animation;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.rendering.shaders.TwoSidedShader;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;

import java.util.Map;

public abstract class PulseEffect implements IEffect {
    public SPAnimationSequence animationSequence = new SPAnimationSequence(false);

    public Vector3 position;
    public Zone zone;
    public Vector3 modelScale = new Vector3();
    public Color modelColor = new Color();
    public static Renderable renderable;
    public static TwoSidedShader shader;

    private static Matrix4 tmpMat = new Matrix4();
    private static float[] tmpVec4 = new float[4];

    public PulseEffect(){}

    public static void create(){
        renderable = new Renderable();
        SeamlessPortalsRenderUtil.cubeModelInstance.getRenderable(renderable);
        shader = new TwoSidedShader(Identifier.of(SeamlessPortalsConstants.MOD_ID, "shaders/default.vert.glsl"), Identifier.of(SeamlessPortalsConstants.MOD_ID, "shaders/effect_pulse.frag.glsl"));
        shader.init();
    }

    protected void setupPulseEffect(Vector3 startingModelScale, Vector3 finalModelScale, Color startingColor, Color finalColor, float changeTime, Vector3 fadeoutScale, Color fadeoutColor, float fadeoutTime){
        SPAnimationSequence startingAnimation = new SPAnimationSequence(true);
        startingAnimation.add(new Vector3Animation(startingModelScale, finalModelScale, changeTime, this.modelScale));
        startingAnimation.add(new ColorAnimation(startingColor, finalColor, changeTime, this.modelColor));
        animationSequence.add(startingAnimation);

        animationSequence.add(new ColorAnimation(finalColor, fadeoutColor, fadeoutTime, this.modelColor));
    }

    @Override
    public void setupEffect(float lifetime, Vector3 position, Zone zone, Map<String, Object> argMap) {
        this.animationSequence.restart();
        this.animationSequence.update(lifetime);
        this.position = position;
        this.zone = zone;
    }

    @Override
    public void render(float delta, Camera playerCamera) {
//        SeamlessPortals.LOGGER.info("Pulse effect scale = " + modelScale);
        this.animationSequence.update(delta);
        if (this.animationSequence.isFinished()) {
            SeamlessPortals.effectManager.removeEffect(this);
            SeamlessPortals.LOGGER.info("Pulse effect finished rendering");
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

    @Override
    public boolean isInZone(Zone zone) {
        return zone == this.zone;
    }
}
