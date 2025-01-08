package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityModelInstance.class)
public abstract class EntityModelInstanceMixin implements IModEntityModelInstance {
    @Unique
    Portal cosmicReach_Seamless_Portals$slicingPortal;

    @Accessor(value = "animTimer")
    abstract float getAnimTimer();

    @Accessor(value = "animTimer")
    abstract void setAnimTimer(float value);

    @Shadow
    public abstract void render(Entity entity, Camera worldCamera, Matrix4 modelMat);

    @Shadow
    GameShader shader;

    @Override
    public void cosmicReach_Seamless_Portals$renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat){
        setAnimTimer(getAnimTimer() - Gdx.graphics.getDeltaTime());
        this.render(entity, worldCamera, modelMat);
    }

    @Override
    public void cosmicReach_Seamless_Portals$updateAnimation() {;
        setAnimTimer(getAnimTimer() + Gdx.graphics.getDeltaTime());
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderDuplicate(Entity entity, Camera renderCamera, Matrix4 modelMatrix, Portal portal) {
        cosmicReach_Seamless_Portals$slicingPortal = portal;
        this.cosmicReach_Seamless_Portals$renderNoAnim(entity, renderCamera, modelMatrix);
        cosmicReach_Seamless_Portals$slicingPortal = null;
        this.shader.shader.setUniformi("u_turnOnSlicing", 0);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTexture(Ljava/lang/String;Lcom/badlogic/gdx/graphics/Texture;I)I"))
    void doPortalStuff(Entity entity, Camera worldCamera, Matrix4 modelMat, CallbackInfo ci){
        if (this.cosmicReach_Seamless_Portals$slicingPortal != null){
            this.shader.shader.setUniformi("u_turnOnSlicing", 1);
            this.shader.bindOptionalUniform3f("u_portalOrigin", this.cosmicReach_Seamless_Portals$slicingPortal.position);
            this.shader.bindOptionalUniform3f("u_portalNormal", this.cosmicReach_Seamless_Portals$slicingPortal.viewDirection);
            this.shader.bindOptionalInt("u_invertPortalNormal", Math.max(cosmicReach_Seamless_Portals$slicingPortal.getPortalSide(entity.position), 0));
        }
    }
}
