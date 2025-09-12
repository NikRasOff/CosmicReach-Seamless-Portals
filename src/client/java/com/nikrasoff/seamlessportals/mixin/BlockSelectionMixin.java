package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.ExtraPortalUtils;
import com.nikrasoff.seamlessportals.extras.RaycastOutput;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.items.UnstableHandheldPortalGen;
import com.nikrasoff.seamlessportals.networking.packets.HpgFiredPacket;
import com.nikrasoff.seamlessportals.networking.packets.PortalClearPacket;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.audio.SoundManager;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSelection.class)
public abstract class BlockSelectionMixin {
    @Shadow public static boolean enabled;

    @Shadow
    private static ShapeRenderer shapeRenderer;
    @Unique
    private Vector3 cosmicReach_Seamless_Portals$portalRaycastHitDebug = new Vector3();
    @Unique
    private Vector3 cosmicReach_Seamless_Portals$portalRaycastOriginDebug = new Vector3();
    @Unique
    private Vector3 cosmicReach_Seamless_Portals$portalRaycastNormalDebug = new Vector3();

    @Unique
    private static Vector3 cosmicReach_Seamless_Portals$tmpVectorForPortals = new Vector3();

    @Inject(method = "render", at = @At("HEAD"))
    private void debugPortalRender(Camera worldCamera, CallbackInfo ci){
        if (UI.renderDebugInfo){
            try {
                shapeRenderer.setProjectionMatrix(worldCamera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.line(cosmicReach_Seamless_Portals$portalRaycastOriginDebug, cosmicReach_Seamless_Portals$portalRaycastHitDebug);
                shapeRenderer.setColor(Color.BLUE);
                shapeRenderer.line(cosmicReach_Seamless_Portals$portalRaycastHitDebug, cosmicReach_Seamless_Portals$tmpVectorForPortals.set(cosmicReach_Seamless_Portals$portalRaycastHitDebug).add(cosmicReach_Seamless_Portals$portalRaycastNormalDebug));
                shapeRenderer.end();
            } catch (Exception ignore) {
                shapeRenderer.end();
            }
        }
    }

    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    private void customRaycast(Zone zone, Camera worldCamera, Player player, CallbackInfo ci){
        if (InGame.getLocalPlayer() != null){
            ItemStack heldItemStack = UI.hotbar.getSelectedItemStack();
            if (heldItemStack != null && heldItemStack.getItem() != null && (heldItemStack.getItem().getID().equals(HandheldPortalGen.hpgID) || heldItemStack.getItem().getID().equals(UnstableHandheldPortalGen.hpgID))){
                enabled = false;
                if (!UI.isInventoryOpen()){
                    if (UI.renderDebugInfo){
                        if (cosmicReach_Seamless_Portals$portalRaycastOriginDebug == null){
                            // No idea why this would be null but sometimes for some reason it just is
                            cosmicReach_Seamless_Portals$portalRaycastHitDebug = new Vector3();
                            cosmicReach_Seamless_Portals$portalRaycastOriginDebug = new Vector3();
                            cosmicReach_Seamless_Portals$portalRaycastNormalDebug = new Vector3();
                        }
                    }
                    if (Controls.attackBreakJustPressed()){
                        if (ClientNetworkManager.isConnected()){
                            ClientNetworkManager.sendAsClient(new HpgFiredPacket(false, UI.hotbar.getSelectedSlotNum()));
                        }
                        else {
                            ExtraPortalUtils.fireHpg(InGame.getLocalPlayer(), false, heldItemStack);
                        }
                        if (UI.renderDebugInfo){
                            RaycastOutput result = ExtraPortalUtils.raycast(zone, worldCamera.position, worldCamera.direction, 1000F);
                            if (result != null){
                                cosmicReach_Seamless_Portals$portalRaycastOriginDebug.set(worldCamera.position);
                                cosmicReach_Seamless_Portals$portalRaycastHitDebug.set(result.hitPos());
                                cosmicReach_Seamless_Portals$portalRaycastNormalDebug.set(result.hitNormal().getVector());
                            }
                        }
                        SoundManager.INSTANCE.playSound("seamlessportals:sounds/portals/hpg_fire.ogg", 0.5f, 1.0f, 0.3f);
                        ItemRenderer.swingHeldItem();
                    }
                    if (Controls.usePlaceJustPressed()){
                        if (ClientNetworkManager.isConnected()){
                            ClientNetworkManager.sendAsClient(new HpgFiredPacket(true, UI.hotbar.getSelectedSlotNum()));
                        }
                        else{
                            ExtraPortalUtils.fireHpg(InGame.getLocalPlayer(), true, heldItemStack);
                        }
                        if (UI.renderDebugInfo){
                            RaycastOutput result = ExtraPortalUtils.raycast(zone, worldCamera.position, worldCamera.direction, 1000F);
                            if (result != null){
                                cosmicReach_Seamless_Portals$portalRaycastOriginDebug.set(worldCamera.position);
                                cosmicReach_Seamless_Portals$portalRaycastHitDebug.set(result.hitPos());
                                cosmicReach_Seamless_Portals$portalRaycastNormalDebug.set(result.hitNormal().getVector());
                            }
                        }
                        SoundManager.INSTANCE.playSound("seamlessportals:sounds/portals/hpg_fire.ogg", 0.5f, 1.0f, 0.3f);
                        ItemRenderer.swingHeldItem();
                    }
                    if (Controls.pickBlockPressed()){
                        if (ClientNetworkManager.isConnected()){
                            ClientNetworkManager.sendAsClient(new PortalClearPacket(UI.hotbar.getSelectedSlotNum()));
                        }
                        else{
                            ExtraPortalUtils.clearPortals(InGame.getLocalPlayer(), heldItemStack);
                        }
                    }
                }

                ci.cancel();
            }
        }
    }
}
