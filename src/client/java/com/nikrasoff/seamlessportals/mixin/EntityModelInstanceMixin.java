package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.nikrasoff.seamlessportals.extras.ClientPortalExtras;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.particles.GameParticleSystem;
import finalforeach.cosmicreach.rendering.GameTexture;
import finalforeach.cosmicreach.rendering.entities.*;
import finalforeach.cosmicreach.rendering.entities.animations.BoneAnimation;
import finalforeach.cosmicreach.rendering.entities.animations.EntityAnimation;
import finalforeach.cosmicreach.rendering.entities.animations.TextureAnimation;
import finalforeach.cosmicreach.rendering.entities.instances.EntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

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

    @Final
    @Shadow private static Matrix4 scaleMat;

    @Shadow
    EntityModel entityModel;

    @Shadow public ObjectMap<String, Bone> boneMap;

    @Shadow abstract public void setBoneTransformOverrides(Entity entity);

    @Shadow public Array<Bone> rootBones;

    @Shadow abstract public void setBonePostTransformOverrides(Entity entity);

    @Final
    @Shadow private static MeshBuilder mb;

    @Shadow
    protected abstract void renderBone(Bone bone, Matrix4 modelMat, boolean shouldRender);

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

    @Shadow
    protected Bone rootBone;

    @Shadow
    public abstract void setBoneToDirection(Bone bone, Vector3 direction);

    @Shadow
    protected Bone headBone;

    @Unique
    protected void cosmicReach_Seamless_Portals$customBoneTransformOverrides(Entity entity){
        this.setBoneTransformOverrides(entity);
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat, boolean shouldRender){
        Array<EntityAnimation> curAnimations = this.currentAnimations;
        FloatArray curAnimationTimers = this.currentAnimationTimers;

        for(int i = 0; i < curAnimations.size && i < curAnimationTimers.size; ++i) {
            EntityAnimation currentAnimation = ((EntityAnimation[])curAnimations.items)[i];
            float animTimer = curAnimationTimers.items[i];

            for (TextureAnimation texAnim : currentAnimation.getTextureAnimations()) {
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

        this.cosmicReach_Seamless_Portals$customBoneTransformOverrides(entity);

        for(String boneName : this.entityModel.boneNames) {
            Bone bone = (Bone)this.boneMap.get(boneName);
            bone.calculateLocalTransform();
        }

        Array.ArrayIterator<Bone> var21 = this.rootBones.iterator();

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
            this.cosmicReach_Seamless_Portals$renderBoneNoAnim(rootBone, modelMat, shouldRender);
        }

        if (shouldRender) {
            if (this.mesh == null) {
                this.mesh = mb.end();
            } else {
                mb.end(this.mesh);
            }
        }

        if (shouldRender && !this.textureMap.isEmpty()) {
            this.shader.bind(worldCamera);

            if (cosmicReach_Seamless_Portals$slicingPortal != null){
                this.shader.bindOptionalBool("u_turnOnSlicing", true);
                if (cosmicReach_Seamless_Portals$isEntityDuplicate){
                    this.shader.bindOptionalUniform3f("u_portalOrigin", ClientPortalExtras.getOriginPosForSlicing(this.cosmicReach_Seamless_Portals$slicingPortal, worldCamera, entity.position, false));
                    this.shader.bindOptionalUniform3f("u_portalNormal", this.cosmicReach_Seamless_Portals$slicingPortal.linkedPortal.viewDirection);
                    this.shader.bindOptionalInt("u_invertPortalNormal", Math.max(cosmicReach_Seamless_Portals$slicingPortal.getPortalSide(entity.position), 0));
                }
                else {
                    this.shader.bindOptionalUniform3f("u_portalOrigin", ClientPortalExtras.getOriginPosForSlicing(this.cosmicReach_Seamless_Portals$slicingPortal, worldCamera, entity.position, false));
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

    @Unique
    private void cosmicReach_Seamless_Portals$renderBoneNoAnim(Bone bone, Matrix4 modelMat, boolean shouldRender){
        Matrix4 tempCubeMat = new Matrix4();
        Matrix4 tempRotMat = new Matrix4();
        Quaternion tempQuat = new Quaternion();

        if (shouldRender && bone.cubes != null) {
            for(EntityModelCube c : bone.cubes) {
                Vector3 size = c.size;
                float inflate = c.inflate;
                Vector3 pivot = c.pivot;
                tempCubeMat.idt();
                tempCubeMat.translate(c.origin.x, c.origin.y, c.origin.z);
                tempCubeMat.translate(size.x / 2.0F, size.y / 2.0F, size.z / 2.0F);
                tempCubeMat.scl(size.x + inflate, size.y + inflate, size.z + inflate);
                tempRotMat.idt();
                tempQuat.setEulerAngles(c.rotation.y, c.rotation.x, c.rotation.z);
                tempRotMat.translate(pivot.x, pivot.y, pivot.z);
                tempRotMat.rotate(tempQuat);
                tempRotMat.translate(-pivot.x, -pivot.y, -pivot.z);
                tempCubeMat.mulLeft(tempRotMat);
                tempCubeMat.mulLeft(bone.currentTransform);
                tempCubeMat.mulLeft(scaleMat);
                if (modelMat != null) {
                    tempCubeMat.mulLeft(modelMat);
                }

                EntityShapeBuilder.build(this.entityModel.texWidth, this.entityModel.texHeight, c, mb, tempCubeMat);
            }
        }

        Array.ArrayIterator var12 = bone.childBones.iterator();

        while(var12.hasNext()) {
            Bone childBone = (Bone)var12.next();
            this.cosmicReach_Seamless_Portals$renderBoneNoAnim(childBone, modelMat, shouldRender);
        }
    }

    @Unique
    private void cosmicReach_Seamless_Portals$updateParticlesForBone(Bone bone, Matrix4 modelMat){
        Matrix4 tempCubeMat = new Matrix4();
        if (bone.particleSystems != null) {
//            SeamlessPortals.LOGGER.info("Tried updating particles");
            for(GameParticleSystem system : bone.particleSystems) {
                tempCubeMat.idt();
                tempCubeMat.mulLeft(bone.currentTransform);
                tempCubeMat.mulLeft(scaleMat);
                if (modelMat != null) {
                    tempCubeMat.mulLeft(modelMat);
                    tempCubeMat.trn(InGame.IN_GAME.getWorldCamera().position);
                }

                system.setTransform(tempCubeMat);
                InGame.IN_GAME.gameParticles.add(system);
            }
        }

        for (Bone childBone : bone.childBones) {
            this.cosmicReach_Seamless_Portals$updateParticlesForBone(childBone, modelMat);
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$updateAnimation(Entity entity, Vector3 renderPos) {
        float dt = Gdx.graphics.getDeltaTime();
        this.globalAnimTimer += dt;
        if (!this.animationsShadowed) {

            for (EntityAnimation a : this.animationsToRemove) {
                this.removeAnimation((IEntityAnimation) a);
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

        Matrix4 tempMat = new Matrix4();
        tempMat.translate(renderPos);
        Vector3 tempCamPos = InGame.IN_GAME.getWorldCamera().position.cpy().scl(-1);
        tempMat.translate(tempCamPos);

        for (Bone bone : this.rootBones) {
//            bone.calculateLocalTransform();
            this.cosmicReach_Seamless_Portals$updateParticlesForBone(bone, tempMat);
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
