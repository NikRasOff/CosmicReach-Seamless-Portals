package com.nikrasoff.seamlessportals.mixin.item_models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.SPClientConstants;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceablePuzzleModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.api.data.point.IDataPointManifest;
import io.github.puzzle.cosmic.api.item.IItem;
import io.github.puzzle.cosmic.api.util.DataPointUtil;
import io.github.puzzle.cosmic.impl.client.item.CosmicItemModel;
import io.github.puzzle.cosmic.impl.data.point.DataPointManifest;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import io.github.puzzle.cosmic.item.ItemDataPointSpecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CosmicItemModel.class)
public abstract class ExperimentalItemModelMixin implements ISliceablePuzzleModel {

    @Shadow @Final
    static Color tintColor;

    @Shadow @Final
    static BlockPosition tmpBlockPos;

    @Shadow
    GameShader program;

    @Shadow public abstract Texture getTextureFromIndex(int i);

    @Shadow public abstract Mesh getMeshFromIndex(int i);

    @Shadow
    IItem item;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void overrideShader(IItem item, CallbackInfo ci){ // Kinda had to do this one
        if (SPClientConstants.OVERRIDE_ITEM_SHADER != null){
            this.program = SPClientConstants.OVERRIDE_ITEM_SHADER;
        }
    }

//    @WrapOperation(method = "renderGeneric", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/items/ItemStack;getPointManifest()Lio/github/puzzle/cosmic/api/data/point/IDataPointManifest;"))
//    private IDataPointManifest fixPuzzleTextureBug(ItemStack instance, Operation<IDataPointManifest> original){
//        SeamlessPortals.LOGGER.info("Caught issue");
//        return null;
//    }

    @Override
    public void renderAsSlicedEntity(Vector3 position, ItemStack stack, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
        modelMatrix.translate(0.5F, 0.2F, 0.5F);
        modelMatrix.scale(0.7F, 0.7F, 0.7F);
        DataPointManifest stackManifest;
        try {
            stackManifest = (DataPointManifest) stack.getPointManifest();
        } catch (Exception var11) {
            stackManifest = null;
        }

        int currentEntry;
        if (stackManifest != null) {
            currentEntry = stackManifest.has(ItemDataPointSpecs.TEXTURE_INDEX) ? (Integer)stackManifest.get(ItemDataPointSpecs.TEXTURE_INDEX).getValue() : 0;
            currentEntry = currentEntry >= AbstractCosmicItem.getTextures(this.item).size() ? 0 : currentEntry;
        } else {
            currentEntry = 0;
        }

        Zone zone = InGame.getLocalPlayer().getZone();

        try {
            Entity.setLightingColor(zone, position, Sky.currentSky.currentAmbientColor, tintColor, tmpBlockPos, tmpBlockPos);
        } catch (Exception var10) {
            tintColor.set(Color.WHITE);
        }

        this.program.bind(renderCamera);
        this.program.bindOptionalMatrix4("u_projViewTrans", renderCamera.combined);
        this.program.bindOptionalMatrix4("u_modelMat", modelMatrix);
        this.program.bindOptionalUniform4f("tintColor", tintColor);
        this.program.bindOptionalInt("isInSlot", 0);
        this.program.bindOptionalTexture("texDiffuse", this.getTextureFromIndex(currentEntry), 0);
        if (portal != null && position != null){
            this.program.shader.setUniformi("u_turnOnSlicing", 1);
            if (isDuplicate){
                this.program.bindOptionalUniform3f("u_portalOrigin", portal.linkedPortal.position);
                this.program.bindOptionalUniform3f("u_portalNormal", portal.linkedPortal.viewDirection);
                this.program.bindOptionalInt("u_invertPortalNormal", Math.max(portal.getPortalSide(position), 0));
            }
            else {
                this.program.bindOptionalUniform3f("u_portalOrigin", portal.position);
                this.program.bindOptionalUniform3f("u_portalNormal", portal.viewDirection);
                this.program.bindOptionalInt("u_invertPortalNormal", Math.max(-portal.getPortalSide(position), 0));
            }
        }
        if (this.getMeshFromIndex(currentEntry) != null) {
            this.getMeshFromIndex(currentEntry).render(this.program.shader, 4);
        }

        this.program.unbind();
        this.program.shader.setUniformi("u_turnOnSlicing", 0);
    }
}
