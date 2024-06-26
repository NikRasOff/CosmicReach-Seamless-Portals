package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.nikrasoff.seamlessportals.effects.PulseEffect;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IPortalIngame;
import com.nikrasoff.seamlessportals.models.EntityItemModel;
import com.nikrasoff.seamlessportals.portals.PortalSaveSystem;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public abstract class InGameMixin implements IPortalIngame {
    @Shadow public abstract Camera getWorldCamera();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/BlockSelection;render(Lcom/badlogic/gdx/graphics/Camera;)V"))
    private void seamlessPortalsCustomRender(CallbackInfo ci){
        Camera renderFromCamera = getWorldCamera();

        PulseEffect.renderPulseEffects(renderFromCamera);
        EntityItemModel.advanceAnimations();
    }

    @Inject(method = "loadWorld(Lfinalforeach/cosmicreach/world/World;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/io/PlayerSaver;loadPlayers(Lfinalforeach/cosmicreach/world/World;)V"))
    private void loadPortals(World world, CallbackInfo ci){
        PortalSaveSystem.loadPortals(world);
    }

    @Accessor(value = "playerController")
    public abstract PlayerController getPlayerController();
}
