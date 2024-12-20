package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.PortalSaveSystem;
import finalforeach.cosmicreach.io.ChunkLoader;
import finalforeach.cosmicreach.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLoader.class)
public abstract class ChunkLoaderMixin {
    @Inject(method = "loadWorld", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/utils/Json;fromJson(Ljava/lang/Class;Ljava/io/InputStream;)Ljava/lang/Object;"))
    static private void loadPortals(String worldFolderName, CallbackInfoReturnable<World> cir){
        SeamlessPortals.LOGGER.info("Loading portal data");
        PortalSaveSystem.loadPortals(worldFolderName);
    }
}
