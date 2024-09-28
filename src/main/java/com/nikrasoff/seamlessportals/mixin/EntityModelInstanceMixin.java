package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.WeakHashMap;

@Mixin(EntityModelInstance.class)
public abstract class EntityModelInstanceMixin implements IModEntityModelInstance {
    @Accessor(value = "animTimer")
    abstract float getAnimTimer();

    @Accessor(value = "animTimer")
    abstract void setAnimTimer(float value);

    @Shadow
    public abstract void render(Entity entity, Camera worldCamera, Matrix4 modelMat);

    @Override
    public void renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat){
        setAnimTimer(getAnimTimer() - Gdx.graphics.getDeltaTime());
        this.render(entity, worldCamera, modelMat);
    }

    @Override
    public void updateAnimation(Entity entity) {;
        setAnimTimer(getAnimTimer() + Gdx.graphics.getDeltaTime());
    }
}
