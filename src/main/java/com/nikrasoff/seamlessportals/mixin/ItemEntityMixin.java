package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.items.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements IModEntity {
    @Shadow
    ItemStack itemStack;

    @Shadow
    float renderSize;

    @Shadow
    float randomHoverOffsetTime;

    public ItemEntityMixin(String entityTypeId) {
        super(entityTypeId);
    }

    @Override
    public void renderNoAnim(Camera renderCamera){
        if (renderCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            if (this.model == null && this.itemStack != null) {
                this.model = GameSingletons.itemEntityModelLoader.load(this.itemStack);
            }

            if (this.model != null) {
                Matrix4 tmpMatrix = new Matrix4();
                Vector3 tmpPos = new Vector3();
                tmpMatrix.idt();
                tmpPos.set(this.lastRenderPosition);
                TickRunner.INSTANCE.partTickSlerp(tmpPos, this.position);
                tmpMatrix.translate(tmpPos);
                float partTick = TickRunner.INSTANCE.getPartTick();
                float a = this.age + partTick * 0.05F + this.randomHoverOffsetTime;
                float hover = MathUtils.sin(a);
                float spin = a * 60.0F;
                tmpMatrix.scl(this.renderSize);
                tmpMatrix.rotate(Vector3.Y, spin);
                tmpMatrix.translate(-0.5F, -0.5F, -0.5F);
                tmpMatrix.translate(0.0F, this.renderSize / 2.0F + this.renderSize * hover / 2.0F, 0.0F);
                this.renderAfterMatrixSetNoAnim(renderCamera, tmpMatrix);
            }

        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/math/Vector3;set(Lcom/badlogic/gdx/math/Vector3;)Lcom/badlogic/gdx/math/Vector3;", ordinal = 1))
    public Vector3 nullifyAnimations(Vector3 instance, Vector3 vector, Operation<Vector3> original){
        return null;
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void advanceAnimations(Camera worldCamera, CallbackInfo ci){
        Vector3 tmpPos = new Vector3();
        tmpPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpPos, this.position);
        this.lastRenderPosition.set(tmpPos);
    }
}
