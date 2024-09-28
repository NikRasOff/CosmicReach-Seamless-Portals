package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.files.FileHandle;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.blocks.BlockStateGenerator;
import finalforeach.cosmicreach.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockStateGenerator.class)
public abstract class BlockStateGeneratorMixin {
    @Shadow
    private static void loadGeneratorsFromFile(FileHandle asset) {
    }

    @Inject(method = "<clinit>", at = @At(value = "RETURN"))
    private static void seamlessPortalsOnClInit(CallbackInfo ci){
        loadGeneratorsFromFile(GameAssetLoader.loadAsset(Identifier.of("seamlessportals", "block_state_generators/directional_blocks.json")));
    }
}
