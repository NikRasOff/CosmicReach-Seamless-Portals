package com.nikrasoff.seamlessportals.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.nikrasoff.seamlessportals.PortalSaveSystem;
import finalforeach.cosmicreach.io.ChunkLoader;
import finalforeach.cosmicreach.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLoader.class)
public abstract class ChunkLoaderMixin {
    @Inject(method = "loadWorld", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/io/EntitySaveSystem;loadPlayers(Lfinalforeach/cosmicreach/world/World;)V"))
    private static void loadPortals(String worldFolderName, CallbackInfoReturnable<World> cir, @Local World world){
        PortalSaveSystem.loadPortals(world);
    }
}
