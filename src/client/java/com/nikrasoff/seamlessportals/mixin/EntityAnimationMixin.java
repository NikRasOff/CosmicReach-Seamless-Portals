package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalRenderEntityComponent;
import com.nikrasoff.seamlessportals.portals.Portal;
import dev.puzzleshq.puzzleloader.loader.util.ReflectionUtil;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUtils;
import finalforeach.cosmicreach.entities.components.IRenderEntityComponent;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.instances.ItemEntityModelInstance;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.EntityChunk;
import finalforeach.cosmicreach.world.Zone;
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

    @Shadow public transient EntityChunk currentChunk;

    @Shadow public transient Zone zone;

    @Shadow protected transient Vector3 lastRenderPosition;

    @Shadow public Vector3 position;

    @Shadow public transient BoundingBox globalBoundingBox;

    @Shadow private transient Color modelLightColor;

    @Shadow public float age;

    @Shadow protected boolean renderedLastFrame;

    @Shadow public abstract boolean recentlyHit();

    @Unique protected transient boolean cosmicReach_Seamless_Portals$hasBeenRendered = false;

    @Shadow private transient Array<IRenderEntityComponent> renderingComponents;

    @Shadow public abstract void getBoundingBox(BoundingBox boundingBox);

    @Shadow abstract public boolean isSneaking();

    @Shadow public Vector3 velocity;
    @Shadow public Vector3 lastPosition;

    @Override
    public void cosmicReach_Seamless_Portals$renderNoAnim(Camera renderCamera) {
        if (this.modelInstance != null) {
            BlockPosition tbp1 = new BlockPosition(null, 0, 0, 0);
            BlockPosition tbp2 = new BlockPosition(null, 0, 0, 0);

            Vector3 tempRenderPosSP = new Vector3();

            Matrix4 tempRenderMatrixSP = new Matrix4();

            if (this.currentChunk != null) {
                Chunk blockChunk = this.zone.getChunkAtPosition(this.position);
                EntityUtils.setLightingColor(blockChunk, this.position, this.modelInstance.getCurrentAmbientColor(), this.modelLightColor, tbp1, tbp2);
            } else {
                this.modelLightColor.set(Color.WHITE);
            }

            tempRenderPosSP.set(this.lastRenderPosition);
            TickRunner.INSTANCE.partTickLerp(tempRenderPosSP, this.position);
            this.getBoundingBox(this.globalBoundingBox);
            boolean renderThisFrame = renderCamera.frustum.boundsInFrustum(this.globalBoundingBox);
            if (renderThisFrame || this.renderedLastFrame) {
                tempRenderMatrixSP.idt();
                tempRenderMatrixSP.translate(tempRenderPosSP);
                this.cosmicReach_Seamless_Portals$renderAfterMatrixSetNoAnim(renderCamera, tempRenderMatrixSP, renderThisFrame);
                if (this.renderingComponents != null) {
                    int s = this.renderingComponents.size;

                    for(int i = 0; i < s; ++i) {
                        IRenderEntityComponent c = (IRenderEntityComponent)this.renderingComponents.get(i);
                        if (c != null) {
                            if (c instanceof IPortalRenderEntityComponent prec){
                                prec.renderNoAnim((Entity) (Object) this, renderCamera, tempRenderPosSP, tempRenderMatrixSP, renderThisFrame);
                            }
                            else{
                                c.render((Entity)(Object)this, renderCamera, tempRenderPosSP, tempRenderMatrixSP, renderThisFrame);
                            }
                        }
                    }
                }
            }

            this.renderedLastFrame = renderThisFrame || this.renderedLastFrame && !this.position.epsilonEquals(this.lastRenderPosition);
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderSliced(Camera playerCamera, Portal portal) {
        if (this.modelInstance != null) {
            BlockPosition tbp1 = new BlockPosition(null, 0, 0, 0);
            BlockPosition tbp2 = new BlockPosition(null, 0, 0, 0);

            Vector3 tempRenderPosSP = new Vector3();

            Matrix4 tempRenderMatrixSP = new Matrix4();

            if (this.currentChunk != null) {
                Chunk blockChunk = this.zone.getChunkAtPosition(this.position);
                EntityUtils.setLightingColor(blockChunk, this.position, this.modelInstance.getCurrentAmbientColor(), this.modelLightColor, tbp1, tbp2);
            } else {
                this.modelLightColor.set(Color.WHITE);
            }

            tempRenderPosSP.set(this.lastRenderPosition);
            TickRunner.INSTANCE.partTickLerp(tempRenderPosSP, this.position);
            this.getBoundingBox(this.globalBoundingBox);
            boolean renderThisFrame = playerCamera.frustum.boundsInFrustum(this.globalBoundingBox);
            if (renderThisFrame || this.renderedLastFrame) {
                tempRenderMatrixSP.idt();
                tempRenderMatrixSP.translate(tempRenderPosSP);
                this.cosmicReach_Seamless_Portals$renderSlicedAfterMatrixSet(playerCamera, tempRenderMatrixSP, portal, false);
                if (this.renderingComponents != null) {
                    int s = this.renderingComponents.size;

                    for(int i = 0; i < s; ++i) {
                        IRenderEntityComponent c = (IRenderEntityComponent)this.renderingComponents.get(i);
                        if (c != null) {
                            if (c instanceof IPortalRenderEntityComponent prec){
                                prec.renderSliced((Entity) (Object) this, playerCamera, portal, tempRenderPosSP, tempRenderMatrixSP, renderThisFrame);
                            }
                            else{
                                c.render((Entity)(Object)this, playerCamera, tempRenderPosSP, tempRenderMatrixSP, renderThisFrame);
                            }
                        }
                    }
                }
            }

            this.renderedLastFrame = renderThisFrame || this.renderedLastFrame && !this.position.epsilonEquals(this.lastRenderPosition);
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderDuplicate(Camera playerCamera, Portal portal){
        if (this.modelInstance != null) {
            BlockPosition tbp1 = new BlockPosition(null, 0, 0, 0);
            BlockPosition tbp2 = new BlockPosition(null, 0, 0, 0);

            Vector3 tempRenderPosSP = new Vector3();

            Matrix4 tempRenderMatrixSP = new Matrix4();

            if (this.currentChunk != null) {
                Chunk blockChunk = this.zone.getChunkAtPosition(this.position);
                EntityUtils.setLightingColor(blockChunk, this.position, this.modelInstance.getCurrentAmbientColor(), this.modelLightColor, tbp1, tbp2);
            } else {
                this.modelLightColor.set(Color.WHITE);
            }

            tempRenderPosSP.set(this.lastRenderPosition);
            TickRunner.INSTANCE.partTickLerp(tempRenderPosSP, this.position);
            this.getBoundingBox(this.globalBoundingBox);

            BoundingBox tmpBB1 = new BoundingBox(this.globalBoundingBox);
            Vector3 addition = portal.getPortaledPos(this.position).sub(this.position);
            tmpBB1.min.add(addition);
            tmpBB1.max.add(addition);
            tmpBB1.update();

            boolean renderThisFrame = playerCamera.frustum.boundsInFrustum(tmpBB1);
            if (renderThisFrame || this.renderedLastFrame) {
                tempRenderMatrixSP.idt();
                tempRenderMatrixSP.translate(tempRenderPosSP);
                tempRenderMatrixSP = portal.getFullyPortaledTransform(tempRenderMatrixSP);
                this.cosmicReach_Seamless_Portals$renderSlicedAfterMatrixSet(playerCamera, tempRenderMatrixSP, portal, true);
                if (this.renderingComponents != null) {
                    int s = this.renderingComponents.size;

                    for(int i = 0; i < s; ++i) {
                        IRenderEntityComponent c = (IRenderEntityComponent)this.renderingComponents.get(i);
                        if (c != null) {
                            if (c instanceof IPortalRenderEntityComponent prec){
                                prec.renderDuplicate((Entity) (Object) this, playerCamera, portal, tempRenderPosSP, tempRenderMatrixSP, renderThisFrame);
                            }
                            else{
                                c.render((Entity)(Object)this, playerCamera, tempRenderPosSP, tempRenderMatrixSP, renderThisFrame);
                            }
                        }
                    }
                }
            }

            this.renderedLastFrame = renderThisFrame || this.renderedLastFrame && !this.position.epsilonEquals(this.lastRenderPosition);
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderAfterMatrixSetNoAnim(Camera renderCamera, Matrix4 customMatrix, boolean shouldRender) {
        float r = this.modelLightColor.r;
        float g = this.modelLightColor.g;
        float b = this.modelLightColor.b;
        if (this.recentlyHit()) {
            b = 0.0F;
            g = 0.0F;
        }

        this.modelInstance.setTint(r, g, b, 1.0F);
        try {
            if (this.modelInstance instanceof ItemEntityModelInstance && ReflectionUtil.getField(this.modelInstance, "model").get(this.modelInstance) instanceof CosmicItemModelWrapper w){
                w.renderAsEntity(this.position, (ItemStack) ReflectionUtil.getField(this, "itemStack").get(this), renderCamera, customMatrix);
            }
            else {
                ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$renderNoAnim((Entity) (Object) this, renderCamera, customMatrix, shouldRender);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
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
