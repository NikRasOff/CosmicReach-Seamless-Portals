package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.portals.PortalSaveSystem;
import finalforeach.cosmicreach.io.ChunkSaver;
import finalforeach.cosmicreach.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkSaver.class)
public abstract class ChunkSaverMixin {
    @Inject(method = "saveWorld", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/io/EntitySaveSystem;savePlayers(Lfinalforeach/cosmicreach/savelib/IWorld;)V"))
    private static void savePortals(World world, CallbackInfo ci){
        PortalSaveSystem.savePortals(world);
    }
}
