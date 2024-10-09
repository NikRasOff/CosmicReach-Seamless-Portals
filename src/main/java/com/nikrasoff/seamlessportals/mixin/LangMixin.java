package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.interfaces.ILang;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Lang.class)
public abstract class LangMixin implements ILang {

    @Accessor("mappedStrings")
    public abstract Map<String, String> getMappedStrings();

    @Inject(method = "loadLang", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/GameAssetLoader;getAllNamespaces()Ljava/util/HashSet;"))
    private static void addModLang(Json json, String langTag, String langPath, FileHandle langGameFile, CallbackInfo ci, @Local Lang lang){
        String[] modLangFiles = {
                "portal_items"
        };
        for (String file : modLangFiles){
            Identifier fileIdentifier = Identifier.of(SeamlessPortals.MOD_ID, "lang/" + langTag + "/" + file + ".json");
            if (Gdx.files.classpath("assets/" + fileIdentifier.toPath()).exists()){
                JsonValue jv = GameAssetLoader.loadJson(GameAssetLoader.loadAsset(fileIdentifier));

                jv.forEach((child) -> {
                    ((ILang) lang).getMappedStrings().put(child.name, child.asString());
                });
            }
        }
    }
}
