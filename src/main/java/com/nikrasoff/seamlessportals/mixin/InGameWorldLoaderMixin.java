package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.portals.PortalSaveSystem;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public abstract class InGameWorldLoaderMixin {
    @Inject(method = "loadWorld(Lfinalforeach/cosmicreach/world/World;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/io/EntitySaveSystem;loadPlayers(Lfinalforeach/cosmicreach/world/World;)V"))
    private void loadPortals(World world, CallbackInfo ci){
        PortalSaveSystem.loadPortals(world);
    }
}
