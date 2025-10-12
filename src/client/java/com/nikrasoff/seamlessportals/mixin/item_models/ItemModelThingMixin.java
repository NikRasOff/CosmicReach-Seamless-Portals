package com.nikrasoff.seamlessportals.mixin.item_models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.extras.ClientPortalExtras;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceableItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.ClientSingletons;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.ItemThing;
import finalforeach.cosmicreach.rendering.items.IItemStackRenderParams;
import finalforeach.cosmicreach.rendering.items.ItemThingModel;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(ItemThingModel.class)
public abstract class ItemModelThingMixin implements ISliceableItemModel {
    @Shadow @Final
    private static Color tintColor;

    @Shadow @Final
    private static BlockPosition tmpBlockPos;

    @Shadow
    GameShader program;

    @Shadow
    Texture texture;

    @Shadow
    Mesh mesh;

    @Shadow
    @Final
    private static Color tmpColor;

    @Shadow
    private Texture hueMaskTexture;

    @Shadow
    private Item item;

    @Shadow
    @Final
    private static IItemStackRenderParams itemStackRenderParams;

    @Override
    public void renderAsSlicedEntity(Vector3 position, ItemStack stack, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
        itemStackRenderParams.setItemStack(stack);
        modelMatrix.translate(0.5F, 0.2F, 0.5F);
        modelMatrix.scale(0.7F, 0.7F, 0.7F);
        Zone zone = InGame.getLocalPlayer().getZone();

        try {
            Entity.setLightingColor(zone, position, Sky.currentSky.currentAmbientColor, tintColor, tmpBlockPos, tmpBlockPos);
        } catch (Exception var7) {
            tintColor.set(Color.WHITE);
        }

        this.program.bind(renderCamera);
        this.program.bindOptionalBool("u_isItem", true);
        this.program.bindOptionalMatrix4("u_projViewTrans", renderCamera.combined);
        this.program.bindOptionalMatrix4("u_modelMat", modelMatrix);
        this.program.bindOptionalUniform4f("tintColor", tintColor);
        this.program.bindOptionalTexture("texDiffuse", this.texture, 0);
        tmpColor.set(Color.WHITE);
        if (this.hueMaskTexture != null) {
            this.program.bindOptionalTexture("u_hueMask", this.hueMaskTexture, 1);
            Item var8 = this.item;
            if (var8 instanceof ItemThing) {
                ItemThing it = (ItemThing)var8;
                if (itemStackRenderParams instanceof IItemStackRenderParams) {
                    IItemStackRenderParams p = (IItemStackRenderParams)itemStackRenderParams;
                    String key = (String)it.getProperty("colorMaskKey");
                    String source = (String)it.getProperty("colorMaskSource");
                    ItemStack itemStack = p.getItemStack();
                    if (itemStack != null && Objects.equals(source, "stackMetadata")) {
                        if (itemStack.stackMetadata.has(key)) {
                            int colorMetadata = itemStack.stackMetadata.getInt(key, -1);
                            tmpColor.set(colorMetadata);
                        } else {
                            Color defaultColor = (Color)it.getProperty("colorMaskDefaultColor");
                            if (defaultColor != null) {
                                tmpColor.set(defaultColor);
                            }
                        }
                    }
                }
            }
        } else {
            this.program.bindOptionalTexture("u_hueMask", ClientSingletons.transparentPixel.get(), 1);
        }

        this.program.bindOptionalUniform4f("u_colorMask", tmpColor);
        this.program.bindOptionalInt("isInSlot", 0);
        if (portal != null && position != null){
            this.program.shader.setUniformi("u_turnOnSlicing", 1);
            if (isDuplicate){
                this.program.bindOptionalUniform3f("u_portalOrigin", ClientPortalExtras.getOriginPosForSlicing(portal, renderCamera, position, true));
                this.program.bindOptionalUniform3f("u_portalNormal", portal.linkedPortal.viewDirection);
                this.program.bindOptionalBool("u_invertPortalNormal", ClientPortalExtras.shouldInvertNormal(portal, position, true));
            }
            else {
                this.program.bindOptionalUniform3f("u_portalOrigin", ClientPortalExtras.getOriginPosForSlicing(portal, renderCamera, position, false));
                this.program.bindOptionalUniform3f("u_portalNormal", portal.viewDirection);
                this.program.bindOptionalBool("u_invertPortalNormal", ClientPortalExtras.shouldInvertNormal(portal, position, false));
            }
        }
        this.mesh.render(this.program.shader, 4);
        this.program.unbind();
        this.program.shader.setUniformi("u_turnOnSlicing", 0);
    }
}
