package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModel;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.WeakHashMap;

@Mixin(EntityModel.class)
public abstract class EntityModelMixin implements IModEntityModel {

    @Shadow public abstract void render(Entity entity, Camera worldCamera, Matrix4 modelMat);

    @Shadow
    WeakHashMap<Entity, EntityModelInstance> modelInstances;

    @Override
    public void renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat){
        EntityModelInstanceMixin e = (EntityModelInstanceMixin) this.modelInstances.get(entity);
        e.setAnimTimer(e.getAnimTimer() - Gdx.graphics.getDeltaTime());
        this.render(entity, worldCamera, modelMat);
    }

    @Override
    public void updateAnimation(Entity entity) {
        EntityModelInstanceMixin e = (EntityModelInstanceMixin) this.modelInstances.get(entity);
        e.setAnimTimer(e.getAnimTimer() + Gdx.graphics.getDeltaTime());
    }
}
