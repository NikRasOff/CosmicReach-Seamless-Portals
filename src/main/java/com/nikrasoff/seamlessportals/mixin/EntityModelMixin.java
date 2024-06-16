package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.crmodders.flux.assets.FluxGameAssetLoader;
import dev.crmodders.flux.tags.Identifier;
import dev.crmodders.flux.tags.ResourceLocation;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityModel.class)
public abstract class EntityModelMixin {
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
}
