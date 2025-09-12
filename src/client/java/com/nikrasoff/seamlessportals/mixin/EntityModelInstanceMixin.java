package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.GameTexture;
import finalforeach.cosmicreach.rendering.entities.Bone;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityAnimation;
import finalforeach.cosmicreach.rendering.entities.animations.BoneAnimation;
import finalforeach.cosmicreach.rendering.entities.animations.EntityAnimation;
import finalforeach.cosmicreach.rendering.entities.animations.TextureAnimation;
import finalforeach.cosmicreach.rendering.entities.instances.EntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(EntityModelInstance.class)
public abstract class EntityModelInstanceMixin implements IModEntityModelInstance {
    @Unique
    Portal cosmicReach_Seamless_Portals$slicingPortal;

    @Unique
    boolean cosmicReach_Seamless_Portals$isEntityDuplicate = false;

    @Shadow
    public abstract void render(Entity entity, Camera worldCamera, Matrix4 modelMat, boolean shouldRender);

    @Shadow
    public GameShader shader;

    @Shadow private FloatArray currentAnimationTimers;

    @Shadow private Array<EntityAnimation> currentAnimations;

    @Shadow public HashMap<String, GameTexture> textureMap;

    @Shadow
    EntityModel entityModel;

    @Shadow public ObjectMap<String, Bone> boneMap;

    @Shadow abstract public void setBoneTransformOverrides(Entity entity);

    @Shadow public Array<Bone> rootBones;

    @Shadow abstract public void setBonePostTransformOverrides(Entity entity);

    @Shadow private static MeshBuilder mb;

    @Shadow abstract void renderBone(Bone bone, Matrix4 modelMat, boolean shouldRender);

    @Shadow
    Mesh mesh;

    @Shadow abstract public Texture getDiffuse();

    @Shadow abstract public Texture getEmission();

    @Shadow
    Color tintColor;

    @Shadow
    float globalAnimTimer;

    @Shadow public boolean animationsShadowed;

    @Shadow private Array<EntityAnimation> animationsToRemove;

    @Shadow abstract public void removeAnimation(IEntityAnimation animation);

    @Override
    public void cosmicReach_Seamless_Portals$renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat, boolean shouldRender){
        Array<EntityAnimation> curAnimations = this.currentAnimations;
        FloatArray curAnimationTimers = this.currentAnimationTimers;

        for(int i = 0; i < curAnimations.size && i < curAnimationTimers.size; ++i) {
            EntityAnimation currentAnimation = ((EntityAnimation[])curAnimations.items)[i];
            float animTimer = curAnimationTimers.items[i];

            ObjectMap.Values boneName = currentAnimation.getTextureAnimations().iterator();

            while(boneName.hasNext()) {
                TextureAnimation texAnim = (TextureAnimation)boneName.next();
                this.textureMap.put(texAnim.name, texAnim.getTexture(animTimer));
            }
        }

        for(String boneName : this.entityModel.boneNames) {
            Bone bone = (Bone)this.boneMap.get(boneName);
            bone.lastRotation.set(bone.currentRotation);
            bone.currentPosition.set(0.0F, 0.0F, 0.0F);
            bone.currentRotation.set(0.0F, 0.0F, 0.0F);
            bone.currentScale.set(1.0F, 1.0F, 1.0F);

            for(int i = 0; i < curAnimations.size && i < curAnimationTimers.size; ++i) {
                EntityAnimation currentAnimation = ((EntityAnimation[])curAnimations.items)[i];
                float animTimer = curAnimationTimers.items[i];
                BoneAnimation boneAnim = currentAnimation.getBoneAnimation(boneName);
                if (boneAnim != null) {
                    boneAnim.addPosition(bone.currentPosition, animTimer);
                    boneAnim.addRotation(bone.currentRotation, animTimer);
                    boneAnim.addScale(bone.currentScale, animTimer);
                }
            }
        }

        this.setBoneTransformOverrides(entity);

        for(String boneName : this.entityModel.boneNames) {
            Bone bone = (Bone)this.boneMap.get(boneName);
            bone.calculateLocalTransform();
        }

        Array.ArrayIterator var21 = this.rootBones.iterator();

        while(var21.hasNext()) {
            Bone bone = (Bone)var21.next();
            bone.propagateTransform();
        }

        this.setBonePostTransformOverrides(entity);
        if (shouldRender) {
            mb.begin(this.shader.allVertexAttributesObj, 4);
        }

        var21 = this.rootBones.iterator();

        while(var21.hasNext()) {
            Bone rootBone = (Bone)var21.next();
            this.renderBone(rootBone, modelMat, shouldRender);
        }

        if (shouldRender) {
            if (this.mesh == null) {
                this.mesh = mb.end();
            } else {
                mb.end(this.mesh);
            }
        }

        if (shouldRender && this.textureMap.size() > 0) {
            this.shader.bind(worldCamera);

            if (cosmicReach_Seamless_Portals$slicingPortal != null){
                this.shader.bindOptionalBool("u_turnOnSlicing", true);
                if (cosmicReach_Seamless_Portals$isEntityDuplicate){
                    this.shader.bindOptionalUniform3f("u_portalOrigin", this.cosmicReach_Seamless_Portals$slicingPortal.linkedPortal.position);
                    this.shader.bindOptionalUniform3f("u_portalNormal", this.cosmicReach_Seamless_Portals$slicingPortal.linkedPortal.viewDirection);
                    this.shader.bindOptionalInt("u_invertPortalNormal", Math.max(cosmicReach_Seamless_Portals$slicingPortal.getPortalSide(entity.position), 0));
                }
                else {
                    this.shader.bindOptionalUniform3f("u_portalOrigin", this.cosmicReach_Seamless_Portals$slicingPortal.position);
                    this.shader.bindOptionalUniform3f("u_portalNormal", this.cosmicReach_Seamless_Portals$slicingPortal.viewDirection);
                    this.shader.bindOptionalInt("u_invertPortalNormal", Math.max(-cosmicReach_Seamless_Portals$slicingPortal.getPortalSide(entity.position), 0));
                }
            }

            this.shader.bindOptionalUniform3f("trueCameraPosition", worldCamera.position);

            this.shader.bindOptionalTexture("texDiffuse", this.getDiffuse(), 0);
            this.shader.bindOptionalTexture("texEmission", this.getEmission(), 1);
            this.shader.bindOptionalUniform4f("tintColor", this.tintColor);
            this.mesh.render(this.shader.shader, 4);

            // Apparently this uniform is here just to look pretty and not much else
            // So I'm setting it back to zero after using
            this.shader.bindOptionalUniform3f("trueCameraPosition", Vector3.Zero);
            this.shader.bindOptionalBool("u_turnOnSlicing", false);
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$updateAnimation() {
        float dt = Gdx.graphics.getDeltaTime();
        this.globalAnimTimer += dt;
        if (!this.animationsShadowed) {
            Array.ArrayIterator var6 = this.animationsToRemove.iterator();

            while(var6.hasNext()) {
                EntityAnimation a = (EntityAnimation)var6.next();
                this.removeAnimation((IEntityAnimation)a);
            }

            this.animationsToRemove.clear();
        }

        Array<EntityAnimation> curAnimations = this.currentAnimations;
        FloatArray curAnimationTimers = this.currentAnimationTimers;

        for(int i = 0; i < curAnimations.size && i < curAnimationTimers.size; ++i) {
            EntityAnimation currentAnimation = ((EntityAnimation[])curAnimations.items)[i];
            float animTimer = curAnimationTimers.items[i];
            if (!this.animationsShadowed) {
                animTimer += dt;
                float durationSeconds = currentAnimation.durationSeconds;
                if (currentAnimation.loop) {
                    animTimer %= durationSeconds;
                } else {
                    if (animTimer >= durationSeconds) {
                        this.animationsToRemove.add(currentAnimation);
                    }

                    animTimer = Math.min(animTimer, durationSeconds);
                }

                curAnimationTimers.items[i] = animTimer;
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderSliced(Entity entity, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
        this.cosmicReach_Seamless_Portals$slicingPortal = portal;
        this.cosmicReach_Seamless_Portals$isEntityDuplicate = isDuplicate;
        this.cosmicReach_Seamless_Portals$renderNoAnim(entity, renderCamera, modelMatrix, true);
        cosmicReach_Seamless_Portals$slicingPortal = null;
    }
}
