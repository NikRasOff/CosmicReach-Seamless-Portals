package com.nikrasoff.seamlessportals.mixin.item_models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceableItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.items.ItemThingModel;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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

    @Override
    public void renderAsSlicedEntity(Vector3 position, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
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
        this.program.bindOptionalInt("isInSlot", 0);
        if (portal != null && position != null){
            this.program.shader.setUniformi("u_turnOnSlicing", 1);
            if (isDuplicate){
                this.program.bindOptionalUniform3f("u_portalOrigin", portal.linkedPortal.position);
                this.program.bindOptionalUniform3f("u_portalNormal", portal.linkedPortal.viewDirection);
                this.program.bindOptionalInt("u_invertPortalNormal", Math.max(-portal.getPortalSide(position), 0));
            }
            else {
                this.program.bindOptionalUniform3f("u_portalOrigin", portal.position);
                this.program.bindOptionalUniform3f("u_portalNormal", portal.viewDirection);
                this.program.bindOptionalInt("u_invertPortalNormal", Math.max(-portal.getPortalSide(position), 0));
            }
        }
        this.mesh.render(this.program.shader, 4);
        this.program.unbind();
        this.program.shader.setUniformi("u_turnOnSlicing", 0);
    }
}
