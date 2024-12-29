package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zone.class)
public class PortalDespawnMixin {
    @Inject(method = "despawnEntity", at = @At("HEAD"))
    void onPortalDespawn(Entity entity, CallbackInfo ci){
        if (entity instanceof Portal p){
            SeamlessPortals.portalManager.removePortal(p);
        }
    }
}
