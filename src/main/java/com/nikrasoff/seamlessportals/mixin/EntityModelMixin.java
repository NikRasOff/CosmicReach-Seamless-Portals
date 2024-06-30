package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.extras.IModEntityModel;
import dev.crmodders.flux.assets.FluxGameAssetLoader;
import dev.crmodders.flux.tags.Identifier;
import dev.crmodders.flux.tags.ResourceLocation;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.WeakHashMap;

@Mixin(EntityModel.class)
public abstract class EntityModelMixin implements IModEntityModel {

    @Shadow public abstract void render(Entity entity, Camera worldCamera, Matrix4 modelMat);

    @Shadow private WeakHashMap<Entity, EntityModelInstance> modelInstances;

    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/GameAssetLoader;loadAsset(Ljava/lang/String;)Lcom/badlogic/gdx/files/FileHandle;", ordinal = 0))
    private static FileHandle loadModModel(String fileName, Operation<FileHandle> original){
        String fn = fileName.replace("models/entities/", "");
        Identifier modelID = Identifier.fromString(fn);
        return FluxGameAssetLoader.locateAsset(new ResourceLocation(modelID.namespace, "models/entities/" + modelID.name));
    }

    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/GameAssetLoader;getTexture(Ljava/lang/String;)Lcom/badlogic/gdx/graphics/Texture;"))
    private static Texture getModTexture(String fileName, Operation<Texture> original){
        String fn = fileName.replace("textures/entities/", "");
        Identifier textureID = Identifier.fromString(fn);
        return original.call("%s:textures/entities/%s".formatted(textureID.namespace, textureID.name));
    }

    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/GameAssetLoader;loadAsset(Ljava/lang/String;)Lcom/badlogic/gdx/files/FileHandle;", ordinal = 1))
    private static FileHandle loadModAnimSet(String fileName, Operation<FileHandle> original){
        String fn = fileName.replace("animations/entities/", "");
        Identifier animSetID = Identifier.fromString(fn);
        return FluxGameAssetLoader.locateAsset(new ResourceLocation(animSetID.namespace, "animations/entities/" + animSetID.name));
    }

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
