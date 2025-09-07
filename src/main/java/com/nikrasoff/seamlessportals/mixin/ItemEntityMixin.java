package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import finalforeach.cosmicreach.singletons.GameSingletons;
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
public abstract class ItemEntityMixin extends Entity{
    @Shadow
    ItemStack itemStack;

    @Shadow
    float renderSize;

    @Shadow
    float randomHoverOffsetTime;

    public ItemEntityMixin(String entityTypeId) {
        super(entityTypeId);
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
