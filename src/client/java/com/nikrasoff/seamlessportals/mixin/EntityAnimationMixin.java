package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.core.loader.util.Reflection;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.instances.ItemEntityModelInstance;
import finalforeach.cosmicreach.rendering.items.ItemThingModel;
import io.github.puzzle.cosmic.impl.client.item.CosmicItemModelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityAnimationMixin implements IModEntity {
    @Shadow public transient IEntityModelInstance modelInstance;

    @Shadow protected transient Vector3 lastRenderPosition;

    @Shadow public Vector3 position;

    @Shadow public transient BoundingBox globalBoundingBox;

    @Shadow private transient Color modelLightColor;

    @Shadow public float age;

    @Shadow public abstract boolean recentlyHit();

    @Unique protected transient boolean cosmicReach_Seamless_Portals$hasBeenRendered = false;

    @Override
    public void cosmicReach_Seamless_Portals$renderNoAnim(Camera renderCamera) {
        if (this.modelInstance != null) {
            Vector3 tmpPos = new Vector3();
            tmpPos.set(this.lastRenderPosition);
            TickRunner.INSTANCE.partTickLerp(tmpPos, this.position);
            if (renderCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
                Matrix4 tmpMatrix = new Matrix4();
                tmpMatrix.idt();
                tmpMatrix.translate(tmpPos);
                float r = this.modelLightColor.r;
                float g = this.modelLightColor.g;
                float b = this.modelLightColor.b;
                if (this.recentlyHit()) {
                    b = 0.0F;
                    g = 0.0F;
                }

                this.modelInstance.setTint(r, g, b, 1.0F);
                ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$renderNoAnim((Entity)(Object) this, renderCamera, tmpMatrix);
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderSliced(Camera playerCamera, Portal portal) {
        if (this.modelInstance != null) {
            Vector3 tmpPos = new Vector3();
            tmpPos.set(this.lastRenderPosition);
            TickRunner.INSTANCE.partTickLerp(tmpPos, this.position);
            if (playerCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
                Matrix4 tmpMatrix = new Matrix4();
                tmpMatrix.idt();
                tmpMatrix.translate(tmpPos);
                float r = this.modelLightColor.r;
                float g = this.modelLightColor.g;
                float b = this.modelLightColor.b;
                if (this.recentlyHit()) {
                    b = 0.0F;
                    g = 0.0F;
                }

                this.modelInstance.setTint(r, g, b, 1.0F);
                ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$renderSliced((Entity)(Object) this, playerCamera, tmpMatrix, portal, false);
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderDuplicate(Camera playerCamera, Portal portal){
        BoundingBox tmpBB1 = new BoundingBox(this.globalBoundingBox);
        Vector3 addition = portal.getPortaledPos(this.position).sub(this.position);
        tmpBB1.min.add(addition);
        tmpBB1.max.add(addition);
        tmpBB1.update();
        if (this.modelInstance != null) {
            Vector3 tmpPos = new Vector3();
            tmpPos.set(this.lastRenderPosition);
            TickRunner.INSTANCE.partTickLerp(tmpPos, this.position);
            if (playerCamera.frustum.boundsInFrustum(tmpBB1)) {
                Matrix4 tmpMatrix = new Matrix4();
                tmpMatrix.translate(tmpPos);
                tmpMatrix = portal.getFullyPortaledTransform(tmpMatrix);
                float r = this.modelLightColor.r;
                float g = this.modelLightColor.g;
                float b = this.modelLightColor.b;
                if (this.recentlyHit()) {
                    b = 0.0F;
                    g = 0.0F;
                }

                this.modelInstance.setTint(r, g, b, 1.0F);
                ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$renderSliced((Entity)(Object) this, playerCamera, tmpMatrix, portal, true);
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderAfterMatrixSetNoAnim(Camera renderCamera, Matrix4 customMatrix) {
        float r = this.modelLightColor.r;
        float g = this.modelLightColor.g;
        float b = this.modelLightColor.b;
        if (this.recentlyHit()) {
            b = 0.0F;
            g = 0.0F;
        }

        this.modelInstance.setTint(r, g, b, 1.0F);
        if (this.modelInstance instanceof ItemEntityModelInstance && Reflection.getFieldContents(this.modelInstance, "model") instanceof CosmicItemModelWrapper w){
            w.renderAsEntity(this.position, Reflection.getFieldContents(this, "itemStack"), renderCamera, customMatrix);
        }
        else {
            this.modelInstance.render((Entity) (Object) this, renderCamera, customMatrix, true);
        }
    }



    @Unique
    public void cosmicReach_Seamless_Portals$renderSlicedAfterMatrixSet(Camera renderCamera, Matrix4 renderMatrix, Portal portal, boolean isDuplicate){
        float r = this.modelLightColor.r;
        float g = this.modelLightColor.g;
        float b = this.modelLightColor.b;
        if (this.recentlyHit()) {
            b = 0.0F;
            g = 0.0F;
        }

        this.modelInstance.setTint(r, g, b, 1.0F);

        ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$renderSliced((Entity) (Object) this, renderCamera, renderMatrix, portal, isDuplicate);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/entities/Entity;renderModelAfterMatrixSet(Lcom/badlogic/gdx/graphics/Camera;Z)V"))
    public void checkIfRendered(Camera worldCamera, CallbackInfo ci){
        this.cosmicReach_Seamless_Portals$hasBeenRendered = true;
    }

    @Override
    public boolean cosmicReach_Seamless_Portals$checkIfHasBeenRendered() {
        return this.cosmicReach_Seamless_Portals$hasBeenRendered;
    }

    @Override
    public void cosmicReach_Seamless_Portals$resetRender() {
        this.cosmicReach_Seamless_Portals$hasBeenRendered = false;
    }

    public void cosmicReach_Seamless_Portals$advanceAnimations(){
        if (this.modelInstance == null) return;
        Vector3 tmpPos = new Vector3();
        tmpPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpPos, this.position);
        this.lastRenderPosition.set(tmpPos);
        if (!(this.modelInstance instanceof IModEntityModelInstance)) return;
        ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$updateAnimation();
    }
}
