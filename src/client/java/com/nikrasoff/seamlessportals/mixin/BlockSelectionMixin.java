package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.ExtraPortalUtils;
import com.nikrasoff.seamlessportals.extras.RaycastOutput;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalBlockSelection;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.items.UnstableHandheldPortalGen;
import com.nikrasoff.seamlessportals.networking.packets.HpgFiredPacket;
import com.nikrasoff.seamlessportals.networking.packets.PortalClearPacket;
import finalforeach.cosmicreach.BlockRaycasts;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.shaders.EntityShader;
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
public abstract class BlockSelectionMixin implements IPortalBlockSelection {
    @Shadow public static boolean enabled;

    @Shadow
    private static ShapeRenderer shapeRenderer;
    @Shadow
    private BlockState selectedBlockState;
    @Shadow
    private BlockRaycasts blockRaycasts;
    @Shadow
    private MeshBuilder breakMeshBuilder;
    @Shadow
    public float breakingTime;
    @Shadow
    static Texture[] breakTex;

    @Unique
    private static BlockState cosmicReach_Seamless_Portals$lastSelectedPortalBlockState;
    @Unique
    private static BlockPosition cosmicReach_Seamless_Portals$lastSelectedPortalBlockPos;
    @Unique
    private Array<BoundingBox> cosmicReach_Seamless_Portals$portalBlockBoundingBoxes;
    @Unique
    private Mesh cosmicReach_Seamless_Portals$portalBreakMesh;


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
                if (cosmicReach_Seamless_Portals$portalRaycastOriginDebug == null){
                    cosmicReach_Seamless_Portals$portalRaycastHitDebug = new Vector3();
                    cosmicReach_Seamless_Portals$portalRaycastOriginDebug = new Vector3();
                    cosmicReach_Seamless_Portals$portalRaycastNormalDebug = new Vector3();
                }
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

    @Override
    public void cosmicReach_Seamless_Portals$renderThroughPortal(Camera worldCamera) {
        // Yet again redoing a base game method to make it work with portals
        if (enabled && UI.renderUI) {
            if (this.selectedBlockState != null) {
                EntityShader breakMeshShader = EntityShader.ENTITY_SHADER;
                BlockPosition selectedBlockPos = this.blockRaycasts.selectedBlockPos;
                if (cosmicReach_Seamless_Portals$lastSelectedPortalBlockPos == null) cosmicReach_Seamless_Portals$lastSelectedPortalBlockPos = new BlockPosition();
                if (this.selectedBlockState != cosmicReach_Seamless_Portals$lastSelectedPortalBlockState || !selectedBlockPos.equals(cosmicReach_Seamless_Portals$lastSelectedPortalBlockPos)) {
                    if (cosmicReach_Seamless_Portals$portalBlockBoundingBoxes == null){
                        // Why oh why won't you just work normally...
                        cosmicReach_Seamless_Portals$portalBlockBoundingBoxes = new Array<>(false, 4, BoundingBox.class);
                    }
                    this.selectedBlockState.getAllBoundingBoxes(this.cosmicReach_Seamless_Portals$portalBlockBoundingBoxes, selectedBlockPos);
                    Array.ArrayIterator<BoundingBox> var7 = this.cosmicReach_Seamless_Portals$portalBlockBoundingBoxes.iterator();

                    while(var7.hasNext()) {
                        BoundingBox bb = (BoundingBox)var7.next();
                        bb.min.sub(0.001F);
                        bb.max.add(0.001F);
                        bb.update();
                    }

                    cosmicReach_Seamless_Portals$lastSelectedPortalBlockState = this.selectedBlockState;
                    cosmicReach_Seamless_Portals$lastSelectedPortalBlockPos.set(selectedBlockPos);
                    this.breakMeshBuilder.begin(breakMeshShader.allVertexAttributesObj, 4);
                    var7 = this.cosmicReach_Seamless_Portals$portalBlockBoundingBoxes.iterator();

                    while(var7.hasNext()) {
                        BoundingBox bb = (BoundingBox)var7.next();
                        BoxShapeBuilder.build(this.breakMeshBuilder, bb);
                    }

                    if (this.cosmicReach_Seamless_Portals$portalBreakMesh != null && this.cosmicReach_Seamless_Portals$portalBreakMesh.getMaxVertices() < this.breakMeshBuilder.getNumVertices()) {
                        this.cosmicReach_Seamless_Portals$portalBreakMesh.dispose();
                        this.cosmicReach_Seamless_Portals$portalBreakMesh = null;
                    }

                    if (this.cosmicReach_Seamless_Portals$portalBreakMesh == null) {
                        this.cosmicReach_Seamless_Portals$portalBreakMesh = this.breakMeshBuilder.end();
                    } else {
                        this.breakMeshBuilder.end(this.cosmicReach_Seamless_Portals$portalBreakMesh);
                    }

                    this.breakingTime = 0.0F;
                }

                shapeRenderer.setProjectionMatrix(worldCamera.combined);
                Gdx.gl.glBlendEquationSeparate(32778, 32774);
                Gdx.gl.glBlendFuncSeparate(775, 775, 1, 1);
                Gdx.gl.glEnable(3042);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.WHITE);
                Gdx.gl.glLineWidth(2.0F);
                this.selectedBlockState.getAllBoundingBoxes(this.cosmicReach_Seamless_Portals$portalBlockBoundingBoxes, selectedBlockPos);
                Array.ArrayIterator<BoundingBox> var10 = this.cosmicReach_Seamless_Portals$portalBlockBoundingBoxes.iterator();

                while(var10.hasNext()) {
                    BoundingBox bb = (BoundingBox)var10.next();
                    bb.min.sub(0.002F);
                    bb.max.add(0.002F);
                    bb.update();
                }

                var10 = this.cosmicReach_Seamless_Portals$portalBlockBoundingBoxes.iterator();

                while(var10.hasNext()) {
                    BoundingBox bb = (BoundingBox)var10.next();
                    shapeRenderer.box(bb.min.x, bb.min.y, bb.min.z, bb.getWidth(), bb.getHeight(), -bb.getDepth());
                }

                shapeRenderer.end();
                Gdx.gl.glBlendEquationSeparate(32774, 32774);
                Gdx.gl.glBlendFunc(770, 771);
                Gdx.gl.glLineWidth(1.0F);
                if (this.breakingTime > 0.0F && this.cosmicReach_Seamless_Portals$portalBreakMesh != null) {
                    breakMeshShader.useFog = false;
                    breakMeshShader.bind(worldCamera);
                    int i = MathUtils.clamp((int)Math.floor((double)(this.breakingTime * (float)breakTex.length)), 0, breakTex.length - 1);
                    breakTex[i].bind(0);
                    this.cosmicReach_Seamless_Portals$portalBreakMesh.render(breakMeshShader.shader, 4);
                    breakMeshShader.unbind();
                }
            }

        }
    }
}
