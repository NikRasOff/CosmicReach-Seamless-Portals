package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.config.SeamlessPortalsConfig;
import com.nikrasoff.seamlessportals.extras.DirectionVector;
import com.nikrasoff.seamlessportals.extras.ExtraPortalUtils;
import com.nikrasoff.seamlessportals.extras.RaycastOutput;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
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
public class BlockSelectionMixin {
    @Shadow public static boolean enabled;

    @Shadow public static ShapeRenderer shapeRenderer;
    @Unique
    private Vector3 portalRaycastHitDebug = new Vector3();
    @Unique
    private Vector3 portalRaycastOriginDebug = new Vector3();
    @Unique
    private Vector3 portalRaycastNormalDebug = new Vector3();

    @Unique
    private static Vector3 tmpVectorForPortals = new Vector3();

    @Inject(method = "render", at = @At("HEAD"))
    private void debugPortalRender(Camera worldCamera, CallbackInfo ci){
        if (SeamlessPortalsConfig.INSTANCE.debugOutlines.value()){
            shapeRenderer.setProjectionMatrix(worldCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.line(portalRaycastOriginDebug, portalRaycastHitDebug);
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.line(portalRaycastHitDebug, tmpVectorForPortals.set(portalRaycastHitDebug).add(portalRaycastNormalDebug));
            shapeRenderer.end();
        }
    }

    private Vector3 getUpVectorForPortals(DirectionVector dv, Camera cam){
        switch (dv.getName()){
            case "posY" -> {
                Vector3 upDir = new Vector3(cam.direction);
                upDir.y = 0;
                upDir.nor();
                return upDir;
            }
            case "negY" -> {
                Vector3 upDir = new Vector3(cam.direction);
                upDir.y = 0;
                upDir.scl(-1);
                upDir.nor();
                return upDir;
            }
            default -> {
                return new Vector3(0 ,1, 0);
            }
        }
    }

    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    private void customRaycast(Zone zone, Camera worldCamera, CallbackInfo ci){
        if (InGame.getLocalPlayer() != null){
            ItemStack heldItem = UI.hotbar.getSelectedItemStack();
            if (heldItem != null && heldItem.getItem() != null && heldItem.getItem().getID().equals("seamlessportals:handheld_portal_generator")){
                enabled = false;

                PortalManager pm = SeamlessPortals.portalManager;
                if (Controls.attackBreakJustPressed()){
                    RaycastOutput result = ExtraPortalUtils.raycast(zone, worldCamera.position, worldCamera.direction, 100F);
                    if (result != null){
                        portalRaycastOriginDebug.set(worldCamera.position);
                        portalRaycastHitDebug.set(result.hitPos());
                        portalRaycastNormalDebug.set(result.hitNormal().getVector());

                        if (pm.primaryPortalId == -1){
                            Vector3 upDir = getUpVectorForPortals(result.hitNormal(), worldCamera);
                            Portal newPortal = new Portal(new Vector2(1, 2), result.hitNormal().getVector().cpy().scl(-1), upDir, result.hitPos(), zone);
                            pm.primaryPortalId = newPortal.getPortalID();
                            pm.primaryPortalChunkPos.x = Math.floorDiv((int) newPortal.position.x, 16);
                            pm.primaryPortalChunkPos.y = Math.floorDiv((int) newPortal.position.y, 16);
                            pm.primaryPortalChunkPos.z = Math.floorDiv((int) newPortal.position.z, 16);
                            if (SeamlessPortals.portalManager.secondaryPortalId != -1){
                                Portal secPortal = pm.getPortalWithGen(pm.secondaryPortalId, pm.secondaryPortalChunkPos, zone.zoneId);
                                if (secPortal == null){
                                    System.out.println("Fuck");
                                }
                                else{
                                    newPortal.linkPortal(secPortal);
                                    secPortal.linkPortal(newPortal);
                                }
                            }
                            zone.allEntities.add(newPortal);
                        }
                        else{
                            Portal prPortal = pm.getPortalWithGen(pm.primaryPortalId, pm.primaryPortalChunkPos, zone.zoneId);
                            if (prPortal == null){
                                System.out.println("Fuck");
                            }
                            else{
                                prPortal.setPosition(result.hitPos().cpy().add(result.hitNormal().getVector().cpy().scl(0.05F)));
                                prPortal.viewDirection = result.hitNormal().getVector().cpy().scl(-1);
                                prPortal.upVector = getUpVectorForPortals(result.hitNormal(), worldCamera);
                            }
                        }
                    }
                    ItemRenderer.swingHeldItem();
                }
                if (Controls.usePlaceJustPressed()){
                    RaycastOutput result = ExtraPortalUtils.raycast(zone, worldCamera.position, worldCamera.direction, 100F);
                    if (result != null){
                        portalRaycastOriginDebug.set(worldCamera.position);
                        portalRaycastHitDebug.set(result.hitPos());
                        portalRaycastNormalDebug.set(result.hitNormal().getVector());

                        if (pm.secondaryPortalId == -1){
                            Vector3 upDir = getUpVectorForPortals(result.hitNormal(), worldCamera);
                            Portal newPortal = new Portal(new Vector2(1, 2), result.hitNormal().getVector(), upDir, result.hitPos(), zone);
                            pm.secondaryPortalId = newPortal.getPortalID();
                            pm.secondaryPortalChunkPos.x = Math.floorDiv((int) newPortal.position.x, 16);
                            pm.secondaryPortalChunkPos.y = Math.floorDiv((int) newPortal.position.y, 16);
                            pm.secondaryPortalChunkPos.z = Math.floorDiv((int) newPortal.position.z, 16);
                            if (SeamlessPortals.portalManager.primaryPortalId != -1){
                                Portal prPortal = pm.getPortalWithGen(pm.primaryPortalId, pm.primaryPortalChunkPos, zone.zoneId);
                                if (prPortal == null){
                                    System.out.println("Fuck");
                                }
                                else{
                                    newPortal.linkPortal(prPortal);
                                    prPortal.linkPortal(newPortal);
                                }
                            }
                            zone.allEntities.add(newPortal);
                        }
                        else{
                            Portal secPortal = pm.getPortalWithGen(pm.secondaryPortalId, pm.secondaryPortalChunkPos, zone.zoneId);
                            if (secPortal == null){
                                System.out.println("Fuck");
                            }
                            else{
                                secPortal.setPosition(result.hitPos().cpy().add(result.hitNormal().getVector().cpy().scl(0.05F)));
                                secPortal.viewDirection = result.hitNormal().getVector();
                                secPortal.upVector = getUpVectorForPortals(result.hitNormal(), worldCamera);
                            }
                        }
                    }
                    ItemRenderer.swingHeldItem();
                }

                ci.cancel();
            }
        }
    }
}
