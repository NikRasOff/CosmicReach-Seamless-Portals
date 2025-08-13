package com.nikrasoff.seamlessportals.mixin.item_models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceableItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.items.ItemModelBlock;
import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemModelBlock.class)
public abstract class ItemModelBlockMixin implements ISliceableItemModel {
    @Shadow
    IGameMesh mesh;

    @Shadow
    GameShader shader;

    @Shadow @Final private static Color color;

    @Shadow @Final private static BlockPosition tmpBlockPos1;

    @Shadow @Final private static BlockPosition tmpBlockPos2;

    @Shadow
    Color blockLightEmitting;

    @Override
    public void renderAsSlicedEntity(Vector3 position, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
        if (this.mesh != null) {
            this.shader.bind(renderCamera);
            this.shader.bindOptionalMatrix4("u_projViewTrans", renderCamera.combined);
            this.shader.bindOptionalMatrix4("u_modelMat", modelMatrix);

            Zone zone = InGame.getLocalPlayer().getZone();
            if (position != null) {
                Entity.setLightingColor(zone, position, Sky.currentSky.currentAmbientColor, color, tmpBlockPos1, tmpBlockPos2);
            } else {
                color.set(Color.WHITE);
            }

            color.r = Math.max(color.r, this.blockLightEmitting.r);
            color.g = Math.max(color.g, this.blockLightEmitting.g);
            color.b = Math.max(color.b, this.blockLightEmitting.b);
            this.shader.bindOptionalUniform4f("tintColor", color);
            if (portal != null && position != null){
                this.shader.shader.setUniformi("u_turnOnSlicing", 1);
                if (isDuplicate){
                    this.shader.bindOptionalUniform3f("u_portalOrigin", portal.linkedPortal.position);
                    this.shader.bindOptionalUniform3f("u_portalNormal", portal.linkedPortal.viewDirection);
                    this.shader.bindOptionalInt("u_invertPortalNormal", Math.max(portal.getPortalSide(position), 0));
                }
                else {
                    this.shader.bindOptionalUniform3f("u_portalOrigin", portal.position);
                    this.shader.bindOptionalUniform3f("u_portalNormal", portal.viewDirection);
                    this.shader.bindOptionalInt("u_invertPortalNormal", Math.max(-portal.getPortalSide(position), 0));
                }
            }
            this.mesh.bind(this.shader.shader);
            this.mesh.render(this.shader.shader, 4);
            this.shader.bindOptionalBool("u_isItem", true);
            this.mesh.unbind(this.shader.shader);
            this.shader.shader.setUniformi("u_turnOnSlicing", 0);
            this.shader.unbind();
        }
    }
}
