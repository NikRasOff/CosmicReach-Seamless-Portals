package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.nikrasoff.seamlessportals.effects.PulseEffect;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalIngame;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalablePlayerController;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.portals.PortalSaveSystem;
import com.nikrasoff.seamlessportals.rendering.models.ObjItemModel;
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

    @Shadow private static PlayerController playerController;

    @Unique
    private float cosmicReach_Seamless_Portals$tempFovForPortals = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void startRenderContext(CallbackInfo ci){
        SeamlessPortalsRenderUtil.renderContext.begin();
        ObjItemModel.updateAnimations();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/utils/viewport/Viewport;apply()V"))
    private void storeTempFOV(CallbackInfo ci){
        this.cosmicReach_Seamless_Portals$tempFovForPortals = ((PerspectiveCamera)getWorldCamera()).fieldOfView;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/items/ItemRenderer;renderHeldItem(Lcom/badlogic/gdx/math/Vector3;Lfinalforeach/cosmicreach/items/ItemStack;Lcom/badlogic/gdx/graphics/PerspectiveCamera;)V"))
    private void seamlessPortalsCustomRender(CallbackInfo ci){
        Camera renderFromCamera = getWorldCamera();

        PulseEffect.renderPulseEffects(renderFromCamera);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void resetPlayerCamera(CallbackInfo ci){
        SeamlessPortalsRenderUtil.renderContext.end();
        ((IPortalablePlayerController) playerController).cosmicReach_Seamless_Portals$resetPlayerCameraUp();
    }

    @Inject(method = "loadWorld(Lfinalforeach/cosmicreach/world/World;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/io/PlayerSaver;loadPlayers(Lfinalforeach/cosmicreach/world/World;)V"))
    private void loadPortals(World world, CallbackInfo ci){
        PortalSaveSystem.loadPortals(world);
    }

    @Accessor(value = "playerController")
    public abstract PlayerController getPlayerController();

    @Override
    public float cosmicReach_Seamless_Portals$getTempFovForPortals() {
        return cosmicReach_Seamless_Portals$tempFovForPortals;
    }
}
