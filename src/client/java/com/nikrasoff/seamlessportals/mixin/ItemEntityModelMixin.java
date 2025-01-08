package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.github.puzzle.core.loader.util.Reflection;
import com.github.puzzle.game.engine.items.model.ItemModelWrapper;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceableItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.rendering.items.ItemEntityModel;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityModel.class)
public abstract class ItemEntityModelMixin implements IModEntityModelInstance {
    @Shadow private ItemModel model;

    @Shadow public abstract void render(Entity entity, Camera worldCamera, Matrix4 modelMat);

    @Unique
    public void cosmicReach_Seamless_Portals$renderDuplicate(Entity entity, Camera renderCamera, Matrix4 modelMatrix, Portal portal){
        if (this.model instanceof ItemModelWrapper w){
            // Why does puzzle have its own item model system? That doesn't make any sense
            ((ISliceableItemModel) w).renderAsSlicedPuzzleEntity(entity.position, Reflection.getFieldContents(entity, "itemStack"), renderCamera, modelMatrix, portal);
        }
        else if (this.model instanceof ISliceableItemModel m){
            m.renderAsSlicedEntity(entity.position, renderCamera, modelMatrix, portal);
        }
        else this.render(entity, renderCamera, modelMatrix);
    }
}
