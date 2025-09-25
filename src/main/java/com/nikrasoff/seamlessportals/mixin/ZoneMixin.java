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
public abstract class ZoneMixin {
    @Inject(method = "removeEntity(Lfinalforeach/cosmicreach/entities/Entity;)V", at = @At("HEAD"))
    void portalDespawnMixin(Entity entity, CallbackInfo ci){
        if (entity instanceof Portal portal){
            SeamlessPortals.portalManager.removePortal(portal);
        }
    }

    @Inject(method = "addEntity", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/utils/ObjectMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    void portalRespawnMixin(Entity entity, CallbackInfo ci){
        if (entity instanceof Portal portal){
            if (SeamlessPortals.portalManager.getPortal(portal.uniqueId) == null){
                SeamlessPortals.portalManager.addPortal(portal);
            }
        }
    }
}
