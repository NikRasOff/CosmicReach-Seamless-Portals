package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.items.data.DataTag;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.items.data.attributes.IntDataAttribute;
import com.github.puzzle.game.items.data.attributes.Vector3DataAttribute;
import com.github.puzzle.game.util.DataTagUtil;
import com.nikrasoff.seamlessportals.SeamlessPortals;
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
public abstract class BlockSelectionMixin {
    @Shadow public static boolean enabled;

    @Shadow public static ShapeRenderer shapeRenderer;
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
        if (SeamlessPortals.debugOutlines){
            shapeRenderer.setProjectionMatrix(worldCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.line(cosmicReach_Seamless_Portals$portalRaycastOriginDebug, cosmicReach_Seamless_Portals$portalRaycastHitDebug);
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.line(cosmicReach_Seamless_Portals$portalRaycastHitDebug, cosmicReach_Seamless_Portals$tmpVectorForPortals.set(cosmicReach_Seamless_Portals$portalRaycastHitDebug).add(cosmicReach_Seamless_Portals$portalRaycastNormalDebug));
            shapeRenderer.end();
        }
    }

    @Unique
    private Vector3 cosmicReach_Seamless_Portals$getUpVectorForPortals(DirectionVector dv, Camera cam){
        switch (dv.getName()){
            case "posY" -> {
                Vector3 upDir = new Vector3(cam.direction);
                upDir.y = 0;
                upDir.nor();
                DirectionVector closestVec = DirectionVector.getClosestHorizontalDirection(upDir);
                if (closestVec.getVector().dot(upDir) > 0.96) return closestVec.getVector().cpy();
                return upDir;
            }
            case "negY" -> {
                Vector3 upDir = new Vector3(cam.direction);
                upDir.y = 0;
                upDir.scl(-1);
                upDir.nor();
                DirectionVector closestVec = DirectionVector.getClosestHorizontalDirection(upDir);
                if (closestVec.getVector().dot(upDir) > 0.96) return closestVec.getVector().cpy();
                return upDir;
            }
            default -> {
                return new Vector3(0 ,1, 0);
            }
        }
    }

    @Unique
    private Vector3 cosmicReach_Seamless_Portals$getPositionForPortals(Vector3 pos, DirectionVector normal){
        switch (normal.getName()){
            case "posY", "negY" -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.04F));
                newPos.x = (float) (Math.round(newPos.x * 2) / 2.0);
                newPos.z = (float) (Math.round(newPos.z * 2) / 2.0);
                return newPos;
            }
            case "posX", "negX" -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.05F));
                newPos.y = (float) Math.round(newPos.y);
                newPos.z = (float) (Math.round(newPos.z * 2) / 2.0);
                return newPos;
            }
            case "posZ", "negZ" -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.05F));
                newPos.y = (float) Math.round(newPos.y);
                newPos.x = (float) (Math.round(newPos.x * 2) / 2.0);
                return newPos;
            }
            default -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.05F));
                newPos.y = (float) Math.floor(newPos.y + 0.5);
                return newPos;
            }
        }
    }

    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    private void customRaycast(Zone zone, Camera worldCamera, CallbackInfo ci){
        if (InGame.getLocalPlayer() != null){
            ItemStack heldItemStack = UI.hotbar.getSelectedItemStack();
            if (heldItemStack != null && heldItemStack.getItem() != null && heldItemStack.getItem().getID().equals("seamlessportals:handheld_portal_generator")){
                enabled = false;
                DataTagManifest hpgManifest = DataTagUtil.getManifestFromStack(heldItemStack);

                if (!hpgManifest.hasTag("portal1Chunk")){
                    hpgManifest.setTag("portal1Chunk", new DataTag<>("p1Chunk", new Vector3DataAttribute(new Vector3())));
                }
                if (!hpgManifest.hasTag("portal1Id")){
                    hpgManifest.setTag("portal1Id", new DataTag<>("p1Id", new IntDataAttribute(-1)));
                }
                DataTag<Vector3> primaryPortalChunkPos = hpgManifest.getTag("portal1Chunk");
                DataTag<Integer> primaryPortalId = hpgManifest.getTag("portal1Id");

                if (!hpgManifest.hasTag("portal2Chunk")){
                    hpgManifest.setTag("portal2Chunk", new DataTag<>("p2Chunk", new Vector3DataAttribute(new Vector3())));
                }
                if (!hpgManifest.hasTag("portal2Id")){
                    hpgManifest.setTag("portal2Id", new DataTag<>("p2Id", new IntDataAttribute(-1)));
                }
                DataTag<Vector3> secondaryPortalChunkPos = hpgManifest.getTag("portal2Chunk");
                DataTag<Integer> secondaryPortalId = hpgManifest.getTag("portal2Id");

                PortalManager pm = SeamlessPortals.portalManager;
                if (!UI.isInventoryOpen()){
                    if (Controls.attackBreakJustPressed()){
                        RaycastOutput result = ExtraPortalUtils.raycast(zone, worldCamera.position, worldCamera.direction, 1000F);
                        if (result != null){
                            cosmicReach_Seamless_Portals$portalRaycastOriginDebug.set(worldCamera.position);
                            cosmicReach_Seamless_Portals$portalRaycastHitDebug.set(result.hitPos());
                            cosmicReach_Seamless_Portals$portalRaycastNormalDebug.set(result.hitNormal().getVector());

                            Portal prPortal = pm.getPortalWithGen(primaryPortalId.getValue(), primaryPortalChunkPos.getValue(), zone.zoneId);
                            Portal secPortal = pm.getPortalWithGen(secondaryPortalId.getValue(), secondaryPortalChunkPos.getValue(), zone.zoneId);
                            if (prPortal == null){
                                Vector3 upDir = cosmicReach_Seamless_Portals$getUpVectorForPortals(result.hitNormal(), worldCamera);
                                Portal newPortal = new Portal(new Vector2(1, 2), result.hitNormal().getVector().cpy().scl(-1), upDir, cosmicReach_Seamless_Portals$getPositionForPortals(result.hitPos(), result.hitNormal()), zone);
                                primaryPortalId.attribute.setValue(newPortal.getPortalID());
                                primaryPortalChunkPos.attribute.getValue().set(Math.floorDiv((int) newPortal.position.x, 16), Math.floorDiv((int) newPortal.position.y, 16), Math.floorDiv((int) newPortal.position.z, 16));
                                if (secondaryPortalId.getValue() != -1){
                                    if (secPortal == null){
                                        secondaryPortalId.attribute.setValue(-1);
                                    }
                                    else{
                                        secPortal.playAnimation("rebind");
                                        newPortal.linkPortal(secPortal);
                                        secPortal.linkPortal(newPortal);
                                    }
                                }
                                zone.allEntities.add(newPortal);
                            }
                            else{
                                if (secPortal != null){
                                    secPortal.playAnimation("rebind");
                                }
                                prPortal.playAnimation("start");
                                prPortal.setPosition(cosmicReach_Seamless_Portals$getPositionForPortals(result.hitPos(), result.hitNormal()));
                                prPortal.viewDirection = result.hitNormal().getVector().cpy().scl(-1);
                                prPortal.upVector = cosmicReach_Seamless_Portals$getUpVectorForPortals(result.hitNormal(), worldCamera);
                            }
                        }
                        ItemRenderer.swingHeldItem();
                    }
                    if (Controls.usePlaceJustPressed()){
                        RaycastOutput result = ExtraPortalUtils.raycast(zone, worldCamera.position, worldCamera.direction, 1000F);
                        if (result != null){
                            cosmicReach_Seamless_Portals$portalRaycastOriginDebug.set(worldCamera.position);
                            cosmicReach_Seamless_Portals$portalRaycastHitDebug.set(result.hitPos());
                            cosmicReach_Seamless_Portals$portalRaycastNormalDebug.set(result.hitNormal().getVector());
                            Portal secPortal = pm.getPortalWithGen(secondaryPortalId.getValue(), secondaryPortalChunkPos.getValue(), zone.zoneId);
                            Portal prPortal = pm.getPortalWithGen(primaryPortalId.getValue(), primaryPortalChunkPos.getValue(), zone.zoneId);

                            if (secPortal == null){
                                Vector3 upDir = cosmicReach_Seamless_Portals$getUpVectorForPortals(result.hitNormal(), worldCamera);
                                Portal newPortal = new Portal(new Vector2(1, 2), result.hitNormal().getVector(), upDir, cosmicReach_Seamless_Portals$getPositionForPortals(result.hitPos(), result.hitNormal()), zone);
                                secondaryPortalId.attribute.setValue(newPortal.getPortalID());
                                secondaryPortalChunkPos.attribute.getValue().set(Math.floorDiv((int) newPortal.position.x, 16), Math.floorDiv((int) newPortal.position.y, 16), Math.floorDiv((int) newPortal.position.z, 16));
                                if (primaryPortalId.getValue() != -1){
                                    if (prPortal == null){
                                        primaryPortalId.attribute.setValue(-1);
                                    }
                                    else{
                                        prPortal.playAnimation("rebind");
                                        newPortal.linkPortal(prPortal);
                                        prPortal.linkPortal(newPortal);
                                    }
                                }
                                zone.allEntities.add(newPortal);
                            }
                            else{
                                if (prPortal != null){
                                    prPortal.playAnimation("rebind");
                                }
                                secPortal.playAnimation("start");
                                secPortal.setPosition(cosmicReach_Seamless_Portals$getPositionForPortals(result.hitPos(), result.hitNormal()));
                                secPortal.viewDirection = result.hitNormal().getVector();
                                secPortal.upVector = cosmicReach_Seamless_Portals$getUpVectorForPortals(result.hitNormal(), worldCamera);
                            }
                        }
                        ItemRenderer.swingHeldItem();
                    }
                    if (Controls.pickBlockPressed()){
                        if (primaryPortalId.getValue() != -1){
                            Portal primaryPortal = pm.getPortalWithGen(primaryPortalId.getValue(), primaryPortalChunkPos.getValue(), zone.zoneId);
                            if (primaryPortal != null){
                                primaryPortal.startDestruction();
                            }
                        }
                        if (secondaryPortalId.getValue() != -1){
                            Portal secondaryPortal = pm.getPortalWithGen(secondaryPortalId.getValue(), secondaryPortalChunkPos.getValue(), zone.zoneId);
                            if (secondaryPortal != null){
                                secondaryPortal.startDestruction();
                            }
                        }

                        primaryPortalId.attribute.setValue(-1);
                        secondaryPortalId.attribute.setValue(-1);
                    }
                }

                ci.cancel();
            }
        }
    }
}
