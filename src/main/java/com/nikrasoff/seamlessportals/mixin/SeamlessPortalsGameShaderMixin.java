package com.nikrasoff.seamlessportals.mixin;

import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameShader.class)
public abstract class SeamlessPortalsGameShaderMixin {
    // This exists solely because of whatever the fuck flux is doing with shaders
    @Redirect(method = "loadShaderFile", at = @At(value = "INVOKE", target = "Ljava/lang/String;replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private String fixShaderName(String instance, String regex, String replacement){

        return instance.replaceAll("[-/. ():]", replacement);
    }
}
