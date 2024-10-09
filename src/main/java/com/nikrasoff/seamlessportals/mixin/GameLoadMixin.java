package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.BlockGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockGame.class)
public abstract class GameLoadMixin {
    @Inject(method = "create", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;initShaders()V"))
    void initMod(CallbackInfo ci){
        SeamlessPortals.extraInit();
    }
}
